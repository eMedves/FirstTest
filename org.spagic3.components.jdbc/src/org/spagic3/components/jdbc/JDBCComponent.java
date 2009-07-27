package org.spagic3.components.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import org.dom4j.tree.DefaultDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.components.jdbc.config.JDBCQueryConfig;
import org.spagic3.components.jdbc.config.QueryParameterConfig;
import org.spagic3.components.jdbc.config.XmlResultSetHandler;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.routing.IMessageRouter;
import org.spagic3.datasource.IDataSourceManager;


public class JDBCComponent extends BaseSpagicService {
	
	Logger log = LoggerFactory.getLogger(JDBCComponent.class);
	
	protected JDBCQueryConfig queryConfig = null;
	protected DataSource ds = null;
	
	protected final AtomicReference<IDataSourceManager> datasourceManager = new AtomicReference<IDataSourceManager>(null); 
	
	public IDataSourceManager getDataSourceManager() {
		return this.datasourceManager.get();
	}

	
	public void unsetMessageRouter(IDataSourceManager dsManager) {
		this.datasourceManager.compareAndSet(dsManager, null);
	}
	
	@Override
	public void init() {
		
		try{
			JDBCQueryConfig queryConfig = new JDBCQueryConfig();
			
			queryConfig.setQuery(propertyConfigurator.getString("query"));
			String dsIdentifier = propertyConfigurator.getString("datasource");
			this.ds = getDataSourceManager().getDataSource(dsIdentifier);
			queryConfig.setColumnNameAsAttribute(propertyConfigurator.getBoolean("result.columnAsAttribute", false));
			queryConfig.setRowsXmlEnvelope(propertyConfigurator.getString("result.rowsXmlEnvelope", "rows"));
			queryConfig.setRowXmlEnvelope(propertyConfigurator.getString("result.rowsXmlEnvelope", "row"));
			queryConfig.setEnrichMessage(propertyConfigurator.getBoolean("result.enrichInputMessage", false));
			queryConfig.setValueAsAttribute(propertyConfigurator.getBoolean("result.valueAsAttribute", false));
			queryConfig.setXmlEnvelope(propertyConfigurator.getString("result.xmlEnvelope", ""));
			queryConfig.setFaultManagement(propertyConfigurator.getString("faultManagement", JDBCQueryConfig.FAULT_SYSTEM));

			
			
			
//
//			<property label="SqlQueryParameters" 
//				  name="SqlQueryParameters" 
//				  propertyEditor="map" 
//				  mapKey="ParameterName" 
//				  mapFields="ParameterName,ParameterType,XPathExpression"
//				  mapFieldsEditors="ParameterType=combo(ParameterTypeComboProvider)"  
//				  editable="true"/>	
//
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		}


	}

	public JDBCQueryConfig getQueryConfig() {
		return queryConfig;
	}

	public void setQueryConfig(JDBCQueryConfig config) {
		this.queryConfig = config;
	}

	@Override
	public boolean run(Exchange exchange, Message in, Message out)
			throws Exception {
		
		log.info("JDBC Component ["+getSpagicId()+"] process Exchange Method");	
		
	    List<String> orderedParameterNameList = getStringParameterNames(queryConfig.getQuery(), queryConfig.getMatchParamRegexp());

	    String jdbcQuery = queryConfig.getQuery().replaceAll(queryConfig.getMatchParamRegexp(), "?");

	    List<QueryParameterConfig> queryParametersConfig = queryConfig.getQueryParams();

	    Map<String, QueryParameterConfig> parameterConfigMap = buildParameterConfigMap(queryParametersConfig);

	    Object[] params = queryConfig.getParamsFromInXml(in, orderedParameterNameList, parameterConfigMap);
	 // execute query
		// get connection
		Connection conn = ds.getConnection();
		Element results = null;
		try {
			QueryRunner runner = new QueryRunner();
			log.debug("Executing query: " + jdbcQuery);
			try {
				if (jdbcQuery.toLowerCase().indexOf("select") != -1) {
					results = (Element) runner.query(conn, jdbcQuery, params, 
							new XmlResultSetHandler(queryConfig.getRowsXmlEnvelope(), 
									queryConfig.getRowXmlEnvelope(), 
									queryConfig.getColumnNameAsAttribute(),
									queryConfig.getValueAsAttribute()));
				} else {
					int intResult = runner.update(conn, jdbcQuery, params);
					Element updateResult = DocumentHelper.createElement("affected-rows");
					updateResult.setText("" + intResult);
					results = updateResult;
				}
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
			}
			log.debug("Query results: " + results);
		} finally {
			
			DbUtils.close(conn);
		
		}

		// compose out response
		// 1) the results is a single row. The elements putted in some other dom
		// element
		// 2) more rows. Must be setted as is

		out.setBody(in.getBody());
		
		String strContent = (String)out.getBody();
		Document docContent = DocumentHelper.parseText(strContent);
		// search the envelop where to put the result
		Node contentEnvelop = null;

		if (isValidStr(queryConfig.getXmlEnvelope())) {
			contentEnvelop = docContent.selectSingleNode("/" + queryConfig.getXmlEnvelope());
			if (contentEnvelop == null) {
				log.warn("Envelope to enrich not found [" + "/" + queryConfig.getXmlEnvelope() + "]: "
						+ "the result will be the original message");
			}
		} else {
			log.info("Xml Envelope is null we will keep the original message ");
		}

		if (contentEnvelop != null && queryConfig.isEnrichMessage() && isValidStr(queryConfig.getXmlEnvelope())) {
			//
			// Case 1: enrich message = true, xmlenvelop != null
			// Result is Original message + result attached as child of
			// (queryConfig.getXmlEnvelope())
			//
			log.debug("Enriching Original Message, JDBC Result are children of queryConfig.getXmlEnvelope()");
			if (results.elements().size() == 1) {
				log.debug("Result has exactly 1 row");
				// the xpath on Element (results) didn't work (I do not know why)
				StringBuffer rowXpath = new StringBuffer("/").append(queryConfig.getRowsXmlEnvelope()).append("/").append(
						queryConfig.getRowXmlEnvelope());
				Node row = new DefaultDocument(results).selectSingleNode(rowXpath.toString());

				if (row != null) {
					log.debug("Adding result to envelope: " + row.getPath());
					((Element) contentEnvelop).appendContent((Element) row);
				} else {
					log.warn("Envelope with single row result not found [" + rowXpath + "]: "
							+ "all the results will be putted in: " + contentEnvelop.getPath());

					((Element) contentEnvelop).add(results);
				}
			} else {
				log.debug("Result has more rows; adding entire result to: " + contentEnvelop.getPath());
				((Element) contentEnvelop).add(results);
			}
			out.setBody(docContent.asXML());

		} else if (contentEnvelop == null && queryConfig.isEnrichMessage()) {
			//
			// Case 2: enrich message = true, xmlenvelop == null
			// Result will be the original message
			//
			log.debug("Enriching =  true, xmlEnvelope is null -> Keep The Original Message");
			out.setBody(docContent.asXML());
		} else {
			//
			// That is the case in which we do not keep original message but we create
			// a new
			//
			log.debug("Not enriching... Keep only the output result");
			docContent = new DOMDocument();
			docContent.add(results);
			out.setBody(docContent.asXML());
		}

		return true;
	}	






	public List<String> getStringParameterNames(String propertyValue, String matchRegexp) {
        Pattern pattern = Pattern.compile(matchRegexp);
        Matcher matcher = 
        	pattern.matcher(propertyValue);
        String placeHolder;
        List<String> toReturn = new ArrayList<String>();
		for (int idx = 1; matcher.find(); idx++) {
			placeHolder = matcher.group();
			placeHolder = placeHolder.substring(1);
			toReturn.add(placeHolder);
		}
		return toReturn;
	}

	private Map<String, QueryParameterConfig> buildParameterConfigMap(
			List<QueryParameterConfig> queryParametersConfig) {
		Map<String, QueryParameterConfig> mapParamConfig = new HashMap<String, QueryParameterConfig>();
		for (QueryParameterConfig q : queryParametersConfig) {
			mapParamConfig.put(q.getPlaceHolder(), q);
		}
		return mapParamConfig;
	}

	protected boolean isValidStr(String s) {
		return s != null && s.trim().length() > 0;
	}

	
}
