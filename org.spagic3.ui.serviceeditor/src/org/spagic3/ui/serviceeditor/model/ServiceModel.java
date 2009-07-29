package org.spagic3.ui.serviceeditor.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;


public class ServiceModel implements IServiceModel {
	
	private Properties properties = null;
	private Map<String, Map<String, Properties>> mapProperties = null;
	
	public ServiceModel(){
		properties = new Properties();
		mapProperties = new LinkedHashMap<String, Map<String,Properties>>();
	}
	
	@Override
	public String get(String propertyName) {
		return (String)properties.get(propertyName);
	}
	
	//
	// Handling simple Properties 
	// 
	@Override
	public void addProperty(String propertyName, String propertyValue) {
		this.properties.put(propertyName, propertyValue);
		
	}
	
	@Override
	public void removeProperty(String propertyName) {
		this.properties.remove(propertyName);
		
	}

	//
	// Handling Map Properties
	// !!!! ATTENTION - CREATING A MAP PROPERTY DOES NOT IMPLY TO HAVE ENTRIES 
	//      FOR EXAMPLE - CREATE A QUERY PARAMETER MAP DOES NOT MEAN I'VE A PARAMETER
	//
	@Override
	public void addPropertyMap(String mapPropertyName) {
			mapProperties.put(mapPropertyName, new LinkedHashMap<String, Properties>());
	}
	
	@Override
	public void removePropertyMap(String mapPropertyName) {
		mapProperties.remove(mapPropertyName);
		
	}
	
	@Override
	public void addEntryToPropertyMap(String mapPropertyName, String key,
			Properties props) {
		Map<String, Properties> toUpdate = mapProperties.get(mapPropertyName);
		toUpdate.put(key, props);	
	}
	
	@Override
	public void removeEntryFromPropertyMap(String mapPropertyName, String key) {
		Map<String, Properties> toUpdate = mapProperties.get(mapPropertyName);
		toUpdate.remove(key);
	}

	
	

	@Override
	public Properties getEntryForPropertyMap(String mapPropertyName, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, Properties>> getMapProperties() {
		return mapProperties;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	

	

	
	
}
