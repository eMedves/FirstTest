package org.spagic3.components.jdbc;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;



public class TypesUtils {
	static Map<String, Integer> sqlTypesNameToInt;
	static Map<Integer, String> sqlTypesIntToName;
	
	
	static {
	    initializeMap();
	}
	
    public static void  initializeMap() {
       sqlTypesIntToName = new HashMap<Integer, String>();
       sqlTypesNameToInt = new HashMap<String, Integer>();
      
            // Get all field in java.sql.Types
            Field[] fields = java.sql.Types.class.getFields();
            for (int i=0; i<fields.length; i++) {
                try {
                    // Get field name
                    String name = fields[i].getName();
    
                    // Get field value
                    Integer value = (Integer)fields[i].get(null);
    
                    add(name, value);
                             
                } catch (IllegalAccessException e) {
                }
            }
            
        add("ORA.TIMESTAMPNS", -100);
		add("ORA.TIMESTAMPTZ", -101);
		add("ORA.TIMESTAMPLTZ", -102);
		add("ORA.INTERVALYM", -103);
		add("ORA.INTERVALDS", -104);
		add("ORA.ROWID", -8);
		add("ORA.CURSOR", -10);
		add("ORA.BFILE", -13);
		add("ORA.OPAQUE", 2007);
		add("ORA.JAVA_STRUCT", 2008);
		add("ORA.PLSQL_INDEX_TABLE", -14);
		add("ORA.BINARY_FLOAT", 100);
		add("ORA.BINARY_DOUBLE", 101);
		add("ORA.NUMBER", 2);
		add("ORA.RAW", -2);
		add("ORA.FIXED_CHAR", 999);
            
            
         
    }
    
    public static int getSqlTypeByName(String name){
    	return sqlTypesNameToInt.get(name);
    }
    
    public static void add(String name, Integer value) {
    	 sqlTypesIntToName.put(value, name);
         sqlTypesNameToInt.put(name, value);
    }
}
