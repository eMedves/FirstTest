package org.spagic3.databaseManager;

import org.spagic.metadb.model.Service;
import org.spagic.metadb.model.ServiceInstance;

public interface IDatabaseManager {
	
	/**
	 * Retrieves the latest definition of a specified Service
	 * @param serviceId
	 * @return The service found, or null if not found
	 */
	public Service getServiceById(String serviceId);
	
	/**
	 * Retrieves the definition of a specified Service with a specific version
	 * @param serviceId
	 * @param version
	 * @return The service found, or null if not found
	 */
	public Service getServiceByIdAndVersion(String serviceId, String version);
	
	/**
	 * Retrieves a specified Service instance
	 * @param serviceId 
	 * @param exchangeID
	 * @return The service instance found, or null if not found
	 */
	public ServiceInstance getServiceInstance(String serviceId, String exchangeID);

	/**
	 * Creates a new service instance
	 * @param serviceId Service Id
	 * @param exchangeID Exchange Id
	 * @param request Request sent to the service instance (can be null)
	 * @param response Response sent by the service instance (can be null)
	 * @return The new service instance
	 */
	public ServiceInstance createServiceInstance(String serviceId, String exchangeID, String request, String response);

	/**
	 * Updates an existing service instance
	 * @param serviceInstance The instance to update
	 * @param response The response to update
	 */
	public void updateServiceInstance(ServiceInstance serviceInstance, String response);

}
