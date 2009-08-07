package org.spagic3.core;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.spagic3.core.resources.IResource;
import org.spagic3.core.resources.Resource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;

public class PropertyConfigurator {
	
	//private Dictionary<String, String> properties;

	
	private Properties properties;
	
	public PropertyConfigurator(Properties properties) {
		this.properties = properties;
	}
	
	public PropertyConfigurator(Dictionary<String, String> propertiesDictionary) {
		super();
		this.properties = new Properties();
		
		if (propertiesDictionary != null && propertiesDictionary.size() > 0){
			Enumeration<String> keysOfDictionary = propertiesDictionary.keys();
			String key = null;
			while (keysOfDictionary.hasMoreElements()){
				key = keysOfDictionary.nextElement();
				this.properties.put(key, propertiesDictionary.get(key));
			}
		}
		
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
		return properties.getProperty(propertyName);
	}
	
	public Properties asProperties(){
		
		return properties;
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
	
	
	public Map<String, Properties> getXMapProperty(String propertyName){
		String o = getPropertyGeneric(propertyName);
		
		if (o == null){
			throw new MandatoryPropertyNotFoundException(propertyName);
		}else {
			XStream xStream = new XStream(new Dom4JDriver());
			return (Map<String, Properties>) xStream.fromXML(o);
		}
	}
	
	
	
	public List<Properties> getXListProperty(String propertyName){
		String o = getPropertyGeneric(propertyName);
		
		if (o == null){
			throw new MandatoryPropertyNotFoundException(propertyName);
		}else {
			XStream xStream = new XStream();
			return (List<Properties>) xStream.fromXML(o);
		}
	}
}
