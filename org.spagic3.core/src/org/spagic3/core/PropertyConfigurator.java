package org.spagic3.core;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.spagic3.core.resources.IResource;
import org.spagic3.core.resources.Resource;

public class PropertyConfigurator {
	
	private Dictionary<String, String> properties;

	public PropertyConfigurator(Dictionary<String, String> properties) {
		super();
		this.properties = properties;
	}
	
	public Integer getInteger(String propertyName) throws MandatoryPropertyNotFoundException {
		String o = getPropertyGeneric(propertyName);
		if (o != null)
			return Integer.valueOf(o);
		else
			throw new MandatoryPropertyNotFoundException(propertyName);
	}
	
	public Long getLong(String propertyName) throws MandatoryPropertyNotFoundException {
		String o = getPropertyGeneric(propertyName);
		if (o != null)
			return Long.valueOf(o);
		else
			throw new MandatoryPropertyNotFoundException(propertyName);
	}
	
	public String getString(String propertyName) throws MandatoryPropertyNotFoundException{
		String o = getPropertyGeneric(propertyName);
		if (o == null)
			throw new MandatoryPropertyNotFoundException(propertyName);
		return o;
	}
	public Boolean getBoolean(String propertyName) throws MandatoryPropertyNotFoundException{
		String o = getPropertyGeneric(propertyName);
		if (o != null)
			return Boolean.valueOf(o);
		else
			throw new MandatoryPropertyNotFoundException(propertyName);
	}
	
	public Integer getInteger(String propertyName, Integer defaultInteger){
		String o = getPropertyGeneric(propertyName);
		if (o != null)
			return Integer.valueOf(o);
		else
			return defaultInteger;
	}
	
	public Long getLong(String propertyName, Long defaultLong){
		String o = getPropertyGeneric(propertyName);
		if (o != null)
			return Long.valueOf(o);
		else
			return defaultLong;
	}
	
	public String getString(String propertyName, String defaultString){
		String o = getPropertyGeneric(propertyName);
		if (o == null)
			return defaultString;
		return o;
	}
	
	public Boolean getBoolean(String propertyName, Boolean defaultBoolean){
		String o = getPropertyGeneric(propertyName);
		if (o != null)
			return Boolean.valueOf(o);
		else
			return defaultBoolean;
	}
	
	
	protected String getPropertyGeneric(String propertyName){
		return properties.get(propertyName);
	}
	
	public Properties asProperties(){
		Properties prop = new Properties();
		Enumeration<String> eKeys = properties.keys();
		String key = null;
		while (eKeys.hasMoreElements()){
			key = eKeys.nextElement();
			prop.put(key, properties.get(key));
			
		}
		return prop;
	}
	
	public  IResource getResource(String propertyName){
		String o = getPropertyGeneric(propertyName);
		
		if (o == null){
			throw new MandatoryPropertyNotFoundException(propertyName);
		}
		return new Resource(o);
	}
	
	public  IResource getResource(String propertyName, String defaultValue){
		String o = getPropertyGeneric(propertyName);
		
		if (o != null)
			return new Resource(o);
		else if (defaultValue != null)
			return new Resource(defaultValue);
		else
			return null;
	}
	
}
