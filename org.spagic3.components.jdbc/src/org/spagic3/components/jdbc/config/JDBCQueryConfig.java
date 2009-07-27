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
package org.spagic3.components.jdbc.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicemix.nmr.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for the JDBCQueryComponent
 *
 */
public class JDBCQueryConfig {
	
	private static final Logger log = LoggerFactory.getLogger(JDBCQueryConfig.class);
	
	public static final String FAULT_FLOW = "FAULT_FLOW".intern();
	public static final String FAULT_SYSTEM = "FAULT_SYSTEM".intern();
	/**
	 * Regular expression used to match placeHolder into the query string.
	 * <br/><br/>
	 * (i.e.: "\\$\\w+" match a placeHolder composed of a $ and a word)
	 * 
	 */
	private String matchParamRegexp = "\\$\\w+";
	
	/**
	 * The query to execute with place holder for parameter substitutions
	 */
	private String query;
	
	/**
	 * Query parameter identified by a placeholder, the value,
	 * the parameter type and the output param flag used in store procedure
	 * invocations.
	 */
	private ArrayList<QueryParameterConfig> queryParams;
	
	/**
	 * Say if the result of the query may be added to the input message or have
	 * to be putted in a new xml envelop.
	 */
	private boolean enrichMessage;
	
	/**
	 * Message envelop used to put the results in.
	 */
	private String xmlEnvelope;
	
	private String rowsXmlEnvelope = "rows";
	private String rowXmlEnvelope = "row";
	private String faultManagement;
	private Boolean columnNameAsAttribute = false;
	private Boolean valueAsAttribute = false;
	
	
	public boolean isEnrichMessage() {
		return enrichMessage;
	}


	public ArrayList<QueryParameterConfig> getQueryParams() {
		return queryParams;
	}


	public void setEnrichMessage(boolean enrichMessage) {
		this.enrichMessage = enrichMessage;
	}


	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public void setQueryParams(ArrayList<QueryParameterConfig> queryParams) {
		this.queryParams = queryParams;
	}

	public String getXmlEnvelope() {
		return xmlEnvelope;
	}


	public void setXmlEnvelope(String xmlEnvelope) {
		this.xmlEnvelope = xmlEnvelope;
	}

	
	
	public Object[] getParamsFromInXml(Message in, List<String> orderedParameterNamesList, Map<String, QueryParameterConfig> paramConfigMap) throws Exception {
		
		log.debug(" Filling Parameters from Normalized Message");
		Object[] values = new Object[orderedParameterNamesList.size()];
	
		JDBCParameterFactory factory = JDBCParameterFactory.getInstance();
		int i = 0;
		for ( String parameterName : orderedParameterNamesList){
			
			QueryParameterConfig pConfig = paramConfigMap.get(parameterName);
			values[i] = factory.initializeParameter(in, pConfig);
			log.debug("Parameter["+i+"] Name["+parameterName+"] Values["+i+"] ( Type: "+pConfig.getParamType()+") XPath ("+pConfig.getXpath()+")");
			i++;
		}
		
		return values;
	}


	public String getMatchParamRegexp() {
		return matchParamRegexp;
	}


	public void setMatchParamRegexp(String matchParamRegexp) {
		this.matchParamRegexp = matchParamRegexp;
	}


	public String getRowsXmlEnvelope() {
		return rowsXmlEnvelope;
	}


	public void setRowsXmlEnvelope(String rowsXmlEnvelope) {
		if (rowsXmlEnvelope != null && !rowsXmlEnvelope.trim().equals("")) {
			this.rowsXmlEnvelope = rowsXmlEnvelope;
		}
	}


	public String getRowXmlEnvelope() {
		return rowXmlEnvelope;
	}


	public void setRowXmlEnvelope(String rowXmlEnvelope) {
		if (rowXmlEnvelope != null && !rowXmlEnvelope.trim().equals("")) {
			this.rowXmlEnvelope = rowXmlEnvelope;
		}
	}


	public String getFaultManagement() {
		return faultManagement;
	}


	public void setFaultManagement(String faultManangement) {
		this.faultManagement = faultManangement;
	}


	public Boolean getColumnNameAsAttribute() {
		return columnNameAsAttribute;
	}


	public void setColumnNameAsAttribute(Boolean columnNameAsAttribute) {
		this.columnNameAsAttribute = columnNameAsAttribute;
	}


	public Boolean getValueAsAttribute() {
		return valueAsAttribute;
	}


	public void setValueAsAttribute(Boolean valueAsAttribute) {
		this.valueAsAttribute = valueAsAttribute;
	}
}
