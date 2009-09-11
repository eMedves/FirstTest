package org.spagic3.service.model;

public interface IServiceModelHelper {
	
	public static final String TYPE_UNDEFINED = "UNDEFINED";
	public static final String TYPE_CONNECTOR = "CONNECTOR";
	public static final String TYPE_SERVICE = "SERVICE";

	/**
	 * Retrieves the component name starting from the factory name.
	 * @param factoryName
	 * @return Component name or null if no match
	 */
	public String getComponentName(String factoryName);

	/**
	 * Retrieves the component type starting from the factory name.
	 * @param factoryName
	 * @return TYPE_CONNECTOR, TYPE_SERVICE or TYPE_UNDEFINED
	 */
	public String getComponentType(String factoryName);

}
