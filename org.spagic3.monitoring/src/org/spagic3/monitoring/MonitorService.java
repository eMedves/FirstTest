package org.spagic3.monitoring;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.base.ServiceInstanceStateConstants;
import org.spagic.metadb.model.Service;
import org.spagic.metadb.model.ServiceInstance;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.databaseManager.IDatabaseManager;



public class MonitorService implements EventHandler {

	private static Logger logger = LoggerFactory.getLogger(MonitorService.class);
	@SuppressWarnings("unused")
	private ComponentContext componentContext = null;
	private final AtomicReference<IDatabaseManager> dbManager = new AtomicReference<IDatabaseManager>(null); 

	protected void activate(ComponentContext componentContext) {
		logger.info(" Monitor Service - ACTIVATED");
		this.componentContext = componentContext;
	}

	protected void deactivate(ComponentContext componentContext) {
		logger.info(" Monitor Service - DEACTIVATED");
	}

	@Override
	public void handleEvent(Event event) {
		for (String propertyName : event.getPropertyNames())
			System.out.println(propertyName + " = " + event.getProperty(propertyName));
		
		IDatabaseManager dbManager = getDatabaseManager();

		Boolean internalEvent = (Boolean) event.getProperty(SpagicConstants._IS_INTERNAL_EVENT);
		String exchangeID = (String) event.getProperty(SpagicConstants.EXCHANGE_ID);
		String correlationID = (String) event.getProperty(SpagicConstants.EXCHANGE_PROPERTY + "." + SpagicConstants.CORRELATION_ID);
		String status = (String) event.getProperty(SpagicConstants.EXCHANGE_STATUS);
		String sender = (String) event.getProperty(SpagicConstants.EXCHANGE_PROPERTY + "." + SpagicConstants.SPAGIC_SENDER);
		String target = (String) event.getProperty(SpagicConstants.EXCHANGE_PROPERTY + "." + SpagicConstants.SPAGIC_TARGET);
		String inBody  = (String) event.getProperty(SpagicConstants.INBODY);
		String outBody  = (String) event.getProperty(SpagicConstants.OUTBODY);
		
		if ((internalEvent != null) && internalEvent) {
			
			// Check if the internal event is of interest for the monitoring
			if (event.getProperty(SpagicConstants._INTERNAL_EVENT_TYPE).equals(SpagicConstants._INTERNAL_EVENT_PROCESS_STARTED)) {
				String exchangeId = (String) event.getProperty(SpagicConstants._INTERNAL_EVENT_REFERRING_ID);
				Long processId = (Long) event.getProperty(SpagicConstants._INTERNAL_EVENT_PROCESS_ID);

				
				
				ServiceInstance serviceInstance = dbManager.getServiceInstanceByMessageId(exchangeId);

//				senderServiceInstance.setState(ServiceInstanceStateConstants.SERVICE_DONE);
//				dbManager.updateServiceInstance(serviceInstance);
			}
			
			return;
		}
		
		Service senderService = dbManager.getServiceById(sender);
		if (senderService == null) {
			logger.info("Sender service: " + sender + " not found");
			return;
		}
		
		Service targetService = dbManager.getServiceById(target);
		if (targetService == null) {
			logger.info("Target service: " + target + " not found");
			return;
		}
		
		if (inBody == null) {
			logger.warn("Input body not found");
			return;
		}
		
		// Search for the service instance with specified exchange id
		ServiceInstance senderServiceInstance = dbManager.getServiceInstanceByCorrelationId(sender, correlationID);
		ServiceInstance targetServiceInstance = dbManager.getServiceInstanceByCorrelationId(target, correlationID);
		
		if (status == SpagicConstants.STATUS_DONE) {

			Date endDate = new Date();
			
			if (senderServiceInstance != null){
				senderServiceInstance.setEnddate(endDate);
				senderServiceInstance.setState(ServiceInstanceStateConstants.SERVICE_DONE);
				dbManager.updateServiceInstance(senderServiceInstance);
			}
			if (targetServiceInstance != null){
				targetServiceInstance.setEnddate(endDate);
				targetServiceInstance.setState(ServiceInstanceStateConstants.SERVICE_DONE);
				dbManager.updateServiceInstance(targetServiceInstance);
			}
			
		} else if (status == SpagicConstants.STATUS_ACTIVE) {
			
			if (outBody != null) {
				// Response message
				if ((senderServiceInstance == null) || (targetServiceInstance == null)) {
					logger.warn("Service instances not found");
					return;
				}
				
				// Save the service response and change the process state
				senderServiceInstance.setResponse(outBody);
				senderServiceInstance.setState(ServiceInstanceStateConstants.SERVICE_DONE);
				dbManager.updateServiceInstance(senderServiceInstance);
				
			} else {
				// Request message
				if (senderServiceInstance == null) {
					
					// Create the service instance only if monitored
					if (senderService.getMonitorEnabled()) {
						// Input message not available, if not provided by the component itself
						senderServiceInstance = dbManager.createServiceInstance(sender, exchangeID, correlationID, targetServiceInstance, null, null);				
					} else {
						logger.info("Sender service: " + sender + " not monitored");
					}
					
				}
				
				if (targetServiceInstance == null) {

					// Create the service instance only if monitored
					if (targetService.getMonitorEnabled()) {
						targetServiceInstance = dbManager.createServiceInstance(target, exchangeID, correlationID, null, inBody, null);
					} else {
						logger.info("Target service: " + target + " not monitored");
					}

					// Update the start service instance with the target service instance
					if (senderServiceInstance != null) {
						senderServiceInstance.setTargetServiceInstance(targetServiceInstance);
						dbManager.updateServiceInstance(senderServiceInstance);
					}
				}
			}			
			
		} else if (status == SpagicConstants.STATUS_ERROR) {
			if ((senderServiceInstance == null) || (targetServiceInstance == null)) {
				logger.error("Service instances not found");
				return;
			}
			
			senderServiceInstance.setState(ServiceInstanceStateConstants.SERVICE_FAULTED);
			dbManager.updateServiceInstance(senderServiceInstance);
			targetServiceInstance.setState(ServiceInstanceStateConstants.SERVICE_FAULTED);
			dbManager.updateServiceInstance(targetServiceInstance);
			
		} else {
			logger.warn("Unknown state: " + status);
		}
		
	}
	
	public void setDatabaseManager(IDatabaseManager dbManager) {
		this.dbManager.set(dbManager);
	}

	public void unsetDatabaseManager(IDatabaseManager dbManager) {
		this.dbManager.compareAndSet(dbManager, null);		
	}
	
	public IDatabaseManager getDatabaseManager() {
		return this.dbManager.get();
	}
	

}
