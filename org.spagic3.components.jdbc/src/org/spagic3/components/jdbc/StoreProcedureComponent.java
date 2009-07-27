/**

    Copyright 2007, 2008 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
**/
package org.spagic3.components.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.components.jdbc.config.JDBCQueryConfig;
import org.spagic3.components.jdbc.config.XmlResultSetHandler;



/**
 * Jdbc component that use the input NormalizedMessage to fill the store
 * procedure call statement.
 * <br>
 * The result is optionally inserted into a copy of the incoming message
 * otherwise a new message is create as output. The output should be formed
 * of more results in this case the call statement placeHolders are used
 * to rappresents output values on output message.

 *
 */
public class StoreProcedureComponent extends JDBCComponent {
	
	private static final Logger log = LoggerFactory.getLogger(StoreProcedureComponent.class);

	public boolean run(Exchange exchange, Message in, Message out)
	throws Exception {

		log.info("StoreProcedureComponent Component ["+getSpagicId()+"] process Exchange Method");	

		List<String> orderedParameterNameList = getStringParameterNames(queryConfig.getQuery(), queryConfig.getMatchParamRegexp());

		String jdbcQuery = queryConfig.getQuery().replaceAll(queryConfig.getMatchParamRegexp(), "?");
		
		List<org.spagic3.components.jdbc.config.QueryParameterConfig> queryParametersConfig = queryConfig.getQueryParams();
		
		
		Object[] results = null;
		Connection conn = null;
		try {
				conn = ds.getConnection();
				StoreProcedureRunner spRunner = new StoreProcedureRunner();
				results = (Object[])spRunner.call(conn, 
												  jdbcQuery, 
												  in, 
												  queryParametersConfig,
												  orderedParameterNameList,
												  new XmlResultSetHandler(
														  	queryConfig.getRowsXmlEnvelope(),
														  	queryConfig.getRowXmlEnvelope())
					);
		} catch (SQLException sqle) {
			if (JDBCQueryConfig.FAULT_FLOW.equals(queryConfig.getFaultManagement())) {
				Message fault = exchange.getFault(true);
				Element faultElem = DocumentHelper.createElement("Fault");
				Element sqlStateElem = DocumentHelper.createElement("sql-state");
				sqlStateElem.setText(sqle.getSQLState());
				Element errorCodeElem = DocumentHelper.createElement("error-code");
				errorCodeElem.setText("" + sqle.getErrorCode());
				faultElem.add(sqlStateElem);
				faultElem.add(errorCodeElem);
				fault.setBody(faultElem.asXML());
				out.setBody(fault.getBody());
				return true;
			}
			throw sqle;
		} finally {
			DbUtils.close(conn);
		}
		
		//
		// compose out response. you need to manage single values and resultset
		// values (last ones were manages by ResultSetHandler)
		//
		out.setBody(in.getBody());
		String strContent = (String)out.getBody();
		Document docContent = DocumentHelper.parseText(strContent);
		Node contentEnvelop = null;
		
		if (isValidStr(queryConfig.getXmlEnvelope())){
			contentEnvelop = docContent.selectSingleNode(
					"/"+queryConfig.getXmlEnvelope());
			if (contentEnvelop == null) {
				log.warn("Envelope to enrich not found ["+"/"+queryConfig.getXmlEnvelope()+"]: " +
				"the result will be the original message");
			}
		}else{
			log.info("Xml Envelope is null we will keep the original message ");
		}
		
		if (queryConfig.isEnrichMessage() && (contentEnvelop != null)) {
			
			
			if (queryConfig.isEnrichMessage() &&  results.length < 2) {
				Element tmpResult = DocumentHelper.createElement("tmpResults");
				getResultsNodes(results, tmpResult, orderedParameterNameList);
				((Element)contentEnvelop).appendContent( tmpResult );
			} else {
				getResultsNodes(results, (Element)contentEnvelop, orderedParameterNameList);
			}
			out.setBody(docContent.asXML());
		} if (queryConfig.isEnrichMessage() && (contentEnvelop == null)) {
			out.setBody(docContent.asXML());
		} else {
			docContent = new DOMDocument();
			String resultEnvelop = queryConfig.getXmlEnvelope();
			if (!isValidStr(resultEnvelop)){
				resultEnvelop = "ENVELOPE";
			}
			docContent.setRootElement(DocumentHelper.createElement(resultEnvelop));
			getResultsNodes(results, docContent.getRootElement(), orderedParameterNameList);
			out.setBody(docContent.asXML());
		}
		
		
		return true;
	}
	
	
	
	/**
	 * Return all results in dom4j elements where the element name is the
	 * placeHolder configured for the output parameter.
	 * 
	 * @param results
	 * @param parent
	 */
	private void getResultsNodes(Object[] results, Element parent, List<String> orderedParameterNameList) {
		Object elemObj = null;
		List<String> output = setOutputParameterNameList(orderedParameterNameList);
		for (int i = 0; i < results.length; i++) {
			elemObj = results[i];
			if (elemObj instanceof Element) {
				parent.add((Element)elemObj);
			} else {
				Element elem = DocumentHelper.createElement(output.get(i));
				elem.setText(elemObj != null? elemObj.toString() : "");
				parent.add(elem);
			}
		}
	}
	
	
	
	private List<String> setOutputParameterNameList(List<String> orderedParameterNameList){
		List<String> orderedOutputParameterNameList = new ArrayList<String>();
		String paramName;
		for(int i=0; i < orderedParameterNameList.size(); i++){
			paramName = orderedParameterNameList.get(i);
			for(org.spagic3.components.jdbc.config.QueryParameterConfig qpc : queryConfig.getQueryParams())
				if((qpc.getPlaceHolder().compareToIgnoreCase(paramName) == 0) && qpc.isOutputParam()){
					orderedOutputParameterNameList.add(paramName);
					break;
				}				
		}
		return orderedOutputParameterNameList;
	}

}
