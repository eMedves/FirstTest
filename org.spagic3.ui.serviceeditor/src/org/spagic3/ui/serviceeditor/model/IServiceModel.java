package org.spagic3.ui.serviceeditor.model;

import java.util.Map;
import java.util.Properties;

public interface IServiceModel {
	

	public String getSpagicId();
	public void setSpagicId(String spagicId);
	public String getFactoryName();
	public void setFactoryName(String factoryName);

	public Properties getProperties();
	public Map<String, Map<String, Properties>> getMapProperties();
	
	public String get(String propertyName);
	
	// Simple Properties Management
	public void addProperty(String propertyName, String propertyValue);
	public void removeProperty(String propertyName);
	
	
	public void addPropertyMap(String mapPropertyName);
	public void removePropertyMap(String mapPropertyName);

	public void addEntryToPropertyMap(String mapPropertyName, String key, Properties prop);
	public void removeEntryFromPropertyMap(String mapPropertyName, String key);
	public Properties getEntryForPropertyMap(String mapPropertyName, String key);
	
}
