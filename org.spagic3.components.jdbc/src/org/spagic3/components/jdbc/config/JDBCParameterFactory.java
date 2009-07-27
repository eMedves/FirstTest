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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.servicemix.nmr.api.Message;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that initialize query parameter based on configuration and on
 * values retrived from input message.
 * 
 * @author buso
 *
 */
public class JDBCParameterFactory {
	private static final Logger log = LoggerFactory.getLogger(JDBCParameterFactory.class);
	private static JDBCParameterFactory singleton;
	// Map of namespaces uri for evaluating XPath expressions
	public static Map<String, String> namespaceURI;
	
	public static JDBCParameterFactory getInstance() {
		if (singleton == null) {
			singleton = new JDBCParameterFactory();
			initNameSpaceURI();
		}
		return singleton;
	}
	
	public synchronized static void initNameSpaceURI(){
		java.sql.Connection aConnection = null; 
		try {
			namespaceURI = new HashMap<String, String>();
			String sql = "SELECT prefix, value FROM xml_namespace x"; 
			InitialContext initialContext = new InitialContext();
	        String completeJndiDataSourceName = "java:comp/env/jdbc/metadb";
	        DataSource ds  = (DataSource)initialContext.lookup(completeJndiDataSourceName); 
			aConnection = ds.getConnection();
	        QueryRunner runQuery = new QueryRunner();
	        List<Map> res = (List) runQuery.query(aConnection,sql,new MapListHandler());
	        Iterator it = res.iterator();
           while (it.hasNext()){
               Map m = (Map)it.next();
               namespaceURI.put((String)m.get("prefix"),(String)m.get("value"));
           } 
		}catch(Throwable t){
			t.printStackTrace();
		}finally{
	           DbUtils.rollbackAndCloseQuietly(aConnection);
	    }
	}
	
	public Object initializeParameter(Message msg,
		QueryParameterConfig config) throws Exception {
		
		
		Object xmlValue = retrieveMessageInEnvelope(msg,
			config.getXpath());
		
		log.debug("Converting value: "+xmlValue+" to type: "+getClassFromConfig(config));
		Object value = ConvertUtils
			.convert((String)xmlValue, getClassFromConfig(config));
		
		if ( isBase64(config) ) {
			return Base64.decodeBase64(value.toString().getBytes());
		}
		
		return value;
	}
	
    private Object retrieveMessageInEnvelope(Message in, String xpathString)
		throws Exception {

        Document doc =DocumentHelper.parseText((String)in.getBody());
        if (xpathString.startsWith("node(")){
        	String nodeSelectionXPath = xpathString.substring(xpathString.indexOf("(")+1,xpathString.indexOf(")"));
        	Node n = doc.selectSingleNode(nodeSelectionXPath);
        	if (n == null){
        		log.warn("Node Selected by expression ["+nodeSelectionXPath+"] is NULL");
        		return "";
        	}
        	return n.asXML();
        }else{
        	DefaultXPath xPath = new DefaultXPath(xpathString); 
        	String nmEnvelope = null;
		
        	xPath.setNamespaceURIs(namespaceURI);
        	
        	// Check if the expression selects an element
        	Object selectedObj = xPath.selectObject(doc);
        	if (selectedObj instanceof List) {
				List objList = (List) selectedObj;
				if (objList.size() != 0) {
					// At least one object was selected
		        	nmEnvelope = xPath.valueOf(doc);
				} else {
	        		log.debug("No nodes were selected by expression ["+xpathString+"]");
				}
        	} else {
            	nmEnvelope = xPath.valueOf(doc);
        	}
        	
        	return nmEnvelope;
        }
        
	}

	private boolean isBase64(QueryParameterConfig config) {
		return QueryParameterConfig.BINARY_BASE64_TYPE
			.equals(config.getParamType());
	}
	
	private Class getClassFromConfig(QueryParameterConfig config)
		throws ClassNotFoundException {
		
		if ( isBase64(config) ) {
			
			return String.class;
		}
		return Class.forName(config.getParamType());
	}
}
