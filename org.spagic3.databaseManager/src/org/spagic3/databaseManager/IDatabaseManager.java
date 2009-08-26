package org.spagic3.databaseManager;

import org.spagic.metadb.model.Service;

public interface IDatabaseManager {
	
	/**
	 * Retrieves the latest definition of a specified Service
	 * @param serviceId
	 * @return The service found
	 */
	public Service getServiceById(String serviceId);
	
	/**
	 * Retrieves the definition of a specified Service with a specific version
	 * @param serviceId
	 * @param version
	 * @return The service found
	 */
	public Service getServiceByIdAndVersion(String serviceId, String version);

}
