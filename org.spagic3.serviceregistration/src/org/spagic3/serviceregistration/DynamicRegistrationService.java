package org.spagic3.serviceregistration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.model.Service;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.databaseManager.IDatabaseManager;
import org.spagic3.service.model.IServiceModelHelper;

public class DynamicRegistrationService {
	
	protected Logger log = LoggerFactory.getLogger(DynamicRegistrationService.class);

	private AtomicReference<IDatabaseManager> databaseManager = new AtomicReference<IDatabaseManager>();
	private AtomicReference<IServiceModelHelper> serviceModelHelper = new AtomicReference<IServiceModelHelper>();

	public static final List<String> TRANSIENT_PROPERTIES = Arrays.asList(SpagicConstants.SPAGIC_FACTORYNAME,
			  SpagicConstants.SPAGIC_TYPE,
			  EventConstants.EVENT_TOPIC);

	public void addService(ServiceReference reference) {
		// Try to register the service into the database
		String serviceId = (String) reference.getProperty(SpagicConstants.SPAGIC_ID_PROPERTY);
		String factoryName = (String) reference.getProperty(SpagicConstants.SPAGIC_FACTORYNAME);
		String type = (String) reference.getProperty(SpagicConstants.SPAGIC_TYPE);
		
		log.debug("Checking service: " + serviceId);
		System.out.println("Checking service: " + serviceId);
		
		Map<String, String> properties = new HashMap<String, String>(reference.getPropertyKeys().length);
		for (String propKey : reference.getPropertyKeys()) {
			Object propValue = reference.getProperty(propKey);
			log.debug(propKey + "=" + propValue);
			
			if ((propValue instanceof String) && (!TRANSIENT_PROPERTIES.contains(propKey))) {
				properties.put(propKey, (String) propValue);
			}
		}
		
		if (factoryName == null) {
			return;
		}
		
		String componentName = getServiceModelHelper().getComponentName(factoryName);
		if ((componentName != null) && (componentName.length() != 0)) {
			Service service = getDbManager().registerService(serviceId, componentName, properties);				
		}
	}

	public void removeService(ServiceReference reference) {
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
