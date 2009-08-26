package org.spagic3.monitoring;

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.model.Service;
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
		System.out.println("####################");
		System.out.println("Received an event !!!!!!");
		System.out.println(event);
		for (String propertyName : event.getPropertyNames())
			System.out.println(propertyName + " = " + event.getProperty(propertyName));
		
		String exchangeID = (String) event.getProperty(SpagicConstants.EXCHANGE_ID);
		String status = (String) event.getProperty(SpagicConstants.EXCHANGE_STATUS);
		String sender = (String) event.getProperty(SpagicConstants.SPAGIC_SENDER);
		String target = (String) event.getProperty(SpagicConstants.SPAGIC_TARGET);
		
		

		IDatabaseManager dbManager = getDatabaseManager();
		Service senderService = dbManager.getServiceById(sender);
		Service targetService = dbManager.getServiceById(target);
		
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
