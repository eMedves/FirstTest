package org.spagic3.core;

import java.util.Dictionary;

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
}
