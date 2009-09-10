package org.spagic3.monitoring;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.model.Service;
import org.spagic.metadb.model.ServiceInstance;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.databaseManager.IDatabaseManager;



public class MonitorService implements EventHandler {

	private static Logger logger = LoggerFactory.getLogger(MonitorService.class);
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
		
		Boolean internalEvent = (Boolean) event.getProperty(SpagicConstants._IS_INTERNAL_EVENT);
		String exchangeID = (String) event.getProperty(SpagicConstants.EXCHANGE_ID);
		String status = (String) event.getProperty(SpagicConstants.EXCHANGE_STATUS);
		String sender = (String) event.getProperty(SpagicConstants.EXCHANGE_PROPERTY + "." + SpagicConstants.SPAGIC_SENDER);
		String target = (String) event.getProperty(SpagicConstants.EXCHANGE_PROPERTY + "." + SpagicConstants.SPAGIC_TARGET);
		String inBody  = (String) event.getProperty(SpagicConstants.INBODY);
		String outBody  = (String) event.getProperty(SpagicConstants.OUTBODY);
		
		if (internalEvent != null) {
			return;
		}
		
		if (status == SpagicConstants.STATUS_DONE) {
			return;
		}

		IDatabaseManager dbManager = getDatabaseManager();
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
		ServiceInstance senderServiceInstance = dbManager.getServiceInstance(sender, exchangeID);
		ServiceInstance targetServiceInstance = dbManager.getServiceInstance(target, exchangeID);
		
		if (status == SpagicConstants.STATUS_ACTIVE) {
			
			if (outBody != null) {
				// Response message
				if ((senderServiceInstance == null) || (targetServiceInstance == null)) {
					logger.warn("Service instances not found");
					return;
				}
				
				// Save the service response
				senderServiceInstance.setResponse(outBody);
				dbManager.updateServiceInstance(senderServiceInstance);
				
			} else {
				// Request message
				if (senderServiceInstance == null) {
					// Input message not available, if not provided by the component itself
					senderServiceInstance = dbManager.createServiceInstance(sender, exchangeID, targetServiceInstance, null, null);				
				}
				
				if (targetServiceInstance == null) {
					targetServiceInstance = dbManager.createServiceInstance(target, exchangeID, null, inBody, null);

					// Update the start service instance with the target service instance
					senderServiceInstance.setTargetServiceInstance(targetServiceInstance);
					dbManager.updateServiceInstance(senderServiceInstance);
				}
			}
			
			
		} else if (status == SpagicConstants.STATUS_ERROR) {
			
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
