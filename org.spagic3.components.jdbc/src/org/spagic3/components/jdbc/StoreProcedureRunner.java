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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.servicemix.nmr.api.Message;
import org.spagic3.components.jdbc.config.JDBCParameterFactory;
import org.spagic3.components.jdbc.config.QueryParameterConfig;


/**
 * Store Procedure runner. This runner use a simplistic assumption where
 * the store procedure is declared with all out parameters first and input
 * ones after.
 * <pre>
 * (i.e.: {&lt;out(1)> = call &lt;functionname>(&lt;out(2)>, ..., &lt;out(n)>, &lt;in(1)>, ..., &lt;in(m)> ) })
 * </pre>
 *
 */

/*
 * Propose this code to DBUtils maintainer.
 */
public class StoreProcedureRunner extends QueryRunner {
	
	
	public Object call(Connection conn, String call,
			Message in, 
			List<QueryParameterConfig> queryParametersConfig,
			List<String> orderedParameterNamesList,
			ResultSetHandler rsh
			) throws SQLException {
		
        CallableStatement stmt = null;
        ResultSet rs = null;
        Object result = null;
        Object[] inputParams = null;
        try {
            stmt = conn.prepareCall(call);
            
            inputParams = this.fillStatement(stmt, queryParametersConfig, orderedParameterNamesList, in);
            stmt.execute();
            return retrieveOutValues(queryParametersConfig,orderedParameterNamesList, stmt, rsh);
        } catch (SQLException e) {

            this.rethrow(e, call, inputParams);

        } finally {
            try {
                close(rs);
            } finally {
                close(stmt);
            }
        }

        return result;		
	}

    protected Object[] retrieveOutValues(List<QueryParameterConfig> queryParametersConfig, 
    									List<String> orderedParameterNamesList,
    									 CallableStatement stmt, 
    									 ResultSetHandler rsh)
    	throws SQLException {
    	ArrayList<Object> list = new ArrayList<Object>();
    	try{
    		
    			String paramName;
    			for(int i=0; i < orderedParameterNamesList.size(); i++){
    				paramName = orderedParameterNamesList.get(i);
    				for(QueryParameterConfig qpc : queryParametersConfig)
    					if((qpc.getPlaceHolder().compareToIgnoreCase(paramName) == 0) && qpc.isOutputParam()){
    						Object elem = stmt.getObject(findParameterPosition(qpc.getPlaceHolder(), orderedParameterNamesList));
    	    				if (elem instanceof ResultSet) {
    	    					list.add(rsh.handle((ResultSet)elem));
    	    					break;
    	    				} else {
    	    					list.add(elem);
    	    					break;
    	    				}
    					}
    			}
    	}catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
        return list.toArray();
    }
    
	/**
	 * 
	 */
    protected Object[] fillStatement(CallableStatement stmt, 
    		List<QueryParameterConfig> queryParametersConfig,
    		List<String> orderedParameterNamesList,
    		Message in)
	    throws SQLException {
    	List<Object> inputParams = new ArrayList<Object>();
    	try{
    		JDBCParameterFactory factory = JDBCParameterFactory.getInstance();
    		Map<String, QueryParameterConfig> tempQueryParMap = new HashMap<String, QueryParameterConfig>();
    		for (QueryParameterConfig queryParameterConfig : queryParametersConfig) {
					tempQueryParMap.put(queryParameterConfig.getPlaceHolder(), queryParameterConfig);
				}
    		for (String paramName : orderedParameterNamesList){
    			QueryParameterConfig aQueryParameterConfig = tempQueryParMap.get(paramName);
    			int parameterPosition = findParameterPosition(aQueryParameterConfig.getPlaceHolder(), orderedParameterNamesList);
    			if (aQueryParameterConfig.isOutputParam()){
    				stmt.registerOutParameter(parameterPosition,
	    								  TypesUtils.getSqlTypeByName(aQueryParameterConfig.getParamType()));
    			}else{
    				Object param = null;
	    		
    				param = factory.initializeParameter(in, aQueryParameterConfig);
	    		
    				inputParams.add(param);
    				if (param != null) {
    					stmt.setObject(parameterPosition, param);
    				} else {
    					// VARCHAR works with many drivers regardless
    					// of the actual column type.  Oddly, NULL and 
    					// OTHER don't work with Oracle's drivers.
    					stmt.setNull(parameterPosition, Types.VARCHAR);
    				}
    			}
    		}
    	}catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	    return inputParams.toArray();
	}	
    
    public int findParameterPosition(String parameterName, List<String> orderedParameterNames) throws Exception {
    	int inList =  orderedParameterNames.indexOf(parameterName);
    	if (inList != -1)
    		return inList + 1;
    	throw new Exception( " Parameter ["+parameterName+"] not found in parameter List");
    }
}
