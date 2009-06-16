package org.spagic3.integration.api;

import java.util.Map;

public interface IElementWithProperties {
	public void setProperties(Map<String, String> properties);
	public Map<String,String> getProperties(Map<String, String> properties);
}
