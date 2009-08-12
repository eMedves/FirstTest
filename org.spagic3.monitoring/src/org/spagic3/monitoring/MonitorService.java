package org.spagic3.monitoring;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitorService implements EventHandler {

	private static Logger logger = LoggerFactory.getLogger(MonitorService.class);
	private ComponentContext componentContext = null;

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
	}

}
