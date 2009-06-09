package org.spagic3.core;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  abstract class AbstractSpagicConnector extends AbstractSpagicService implements IConnector {
	private static Logger logger = LoggerFactory.getLogger(AbstractSpagicConnector.class);
	
	protected void activate(ComponentContext componentContext){
		super.activate(componentContext);
		try{
			start();
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected void deactivate(ComponentContext componentContext){
		try{
			stop();	
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		super.deactivate(componentContext);
	}
}
