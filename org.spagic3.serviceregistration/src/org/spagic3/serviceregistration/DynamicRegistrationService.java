package org.spagic3.serviceregistration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.model.Component;
import org.spagic.metadb.model.Service;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.databaseManager.IDatabaseManager;
import org.spagic3.service.model.IServiceModelHelper;

public class DynamicRegistrationService {
	
	protected Logger log = LoggerFactory.getLogger(DynamicRegistrationService.class);

	private AtomicReference<IDatabaseManager> databaseManager = new AtomicReference<IDatabaseManager>();
	private AtomicReference<IServiceModelHelper> serviceModelHelper = new AtomicReference<IServiceModelHelper>();

	public static final List<String> SERVICE_TRANSIENT_PROPERTIES = Arrays.asList(SpagicConstants.SPAGIC_FACTORYNAME,
			  SpagicConstants.SPAGIC_TYPE,
			  EventConstants.EVENT_TOPIC);

	/**
	 * Dinamically register a new service definition on the database.
	 * @param reference Reference to the new Spagic service
	 */
	public void addService(ServiceReference reference) {
		// Try to register the service into the database
		String serviceId = (String) reference.getProperty(SpagicConstants.SPAGIC_ID_PROPERTY);
		String factoryName = (String) reference.getProperty(SpagicConstants.SPAGIC_FACTORYNAME);
		
		log.debug("Checking service: " + serviceId);
		System.out.println("Checking service: " + serviceId);
		
		if (factoryName == null) {
			return;
		}

		Map<String, String> properties = new HashMap<String, String>(reference.getPropertyKeys().length);
		for (String propKey : reference.getPropertyKeys()) {
			Object propValue = reference.getProperty(propKey);
			log.debug(propKey + "=" + propValue);
			
			if ((propValue instanceof String) && (!SERVICE_TRANSIENT_PROPERTIES.contains(propKey))) {
				properties.put(propKey, (String) propValue);
			}
		}
				
		String componentName = getServiceModelHelper().getComponentName(factoryName);
		if ((componentName != null) && (componentName.length() != 0)) {
			@SuppressWarnings("unused")
			Service service = getDbManager().registerService(serviceId, componentName, properties);				
		}
	}

	public void removeService(ServiceReference reference) {
		// Do nothing
	}
	
	public void addComponent(ServiceReference reference) {
		String componentFactory = (String) reference.getProperty(ComponentConstants.COMPONENT_FACTORY);

		log.debug("Checking component factory: " + componentFactory);
		
		if (componentFactory == null) {
			return;
		}

		Map<String, String> properties = new HashMap<String, String>(reference.getPropertyKeys().length);
		for (String propKey : reference.getPropertyKeys()) {
			Object propValue = reference.getProperty(propKey);
			log.debug("#### " + propKey + "=" + propValue);
			System.out.println("#### " + propKey + "=" + propValue);
			
			if (propValue instanceof String) {
				properties.put(propKey, (String) propValue);
			}
		}
		
		String componentName = getServiceModelHelper().getComponentName(componentFactory);
		String componentType = getServiceModelHelper().getComponentType(componentFactory);
		if ((componentName != null) && (componentName.length() != 0)) {
			@SuppressWarnings("unused")
			Component component = getDbManager().registerComponent(componentName, componentType, properties);				
		}
	}
	
	public void removeComponent(ServiceReference reference) {
		// Do nothing
	}

	public IDatabaseManager getDbManager() {
		return this.databaseManager.get();
	}
	
	public IServiceModelHelper getServiceModelHelper() {
		return this.serviceModelHelper.get();
	}
	
	public void bindDatabaseManager(IDatabaseManager dbManager) {
		this.databaseManager.set(dbManager);
	}

	public void unbindDatabaseManager(IDatabaseManager dbManager) {
		this.databaseManager.compareAndSet(dbManager, null);		
	}
	
	public void bindServiceModelHelper(IServiceModelHelper serviceModelHelper) {
		this.serviceModelHelper.set(serviceModelHelper);
	}

	public void unbindServiceModelHelper(IServiceModelHelper serviceModelHelper) {
		this.serviceModelHelper.compareAndSet(serviceModelHelper, null);		
	}
	
}
