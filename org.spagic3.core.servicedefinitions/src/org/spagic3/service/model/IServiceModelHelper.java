package org.spagic3.service.model;

public interface IServiceModelHelper {
	
	/**
	 * Retrieves the component name starting from the factory name.
	 * @param factoryName
	 * @return Component name or null if no match
	 */
	public String getComponentName(String factoryName);

	/**
	 * Retrieves the component type starting from the factory name.
	 * @param factoryName
	 * @return SERVICE_TYPE_CONNECTOR, SERVICE_TYPE_SERVICE or SERVICE_TYPE_UNDEFINED
	 */
	public String getComponentType(String factoryName);

}
