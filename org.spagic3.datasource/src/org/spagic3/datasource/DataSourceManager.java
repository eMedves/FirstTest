package org.spagic3.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.constants.SpagicConstants;


public class DataSourceManager implements IDataSourceManager {
	
	private EventAdmin ea = null;
	
	private static Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
	private ConcurrentHashMap<String, ServiceReference> datasources = new ConcurrentHashMap<String, ServiceReference>();
	private ComponentContext componentContext = null;
	
	protected void activate(ComponentContext componentContext) {
		logger.info(" DataSourceManager - ACTIVATED");
		this.componentContext = componentContext;
	}

	protected void deactivate(ComponentContext componentContext) {
		logger.info(" DataSourceManager - DEACTIVATED");
		
	}

	public void addDataSource(ServiceReference dataSourceServiceReference) {
		
		String dsIdentifier = (String) dataSourceServiceReference
				.getProperty("id");
		
		logger.info(" DataSource ["+dsIdentifier+"] -- REGISTERED");
		
		datasources.put(dsIdentifier, dataSourceServiceReference);
		notify(dsIdentifier, SpagicConstants._INTERNAL_EVENT_DS_DEPLOYED);
		

		
	}

	public void removeDataSource(
			ServiceReference dataSourceServiceReference) {

		String dsIdentifier = (String) dataSourceServiceReference.getProperty("id");
		
		logger.info(" DataSource ["+dsIdentifier+"] -- UNREGISTERED");
		datasources.remove(dsIdentifier);
		notify(dsIdentifier, SpagicConstants._INTERNAL_EVENT_DS_UNDEPLOYED);

	}

	@Override
	public DataSource getDataSource(String datasourceIdentifier) throws Exception {
		ServiceReference sr = datasources.get(datasourceIdentifier);
		
		if (sr == null){
			logger.error("DataSourceManager -> No datasource registered with id ["+datasourceIdentifier+"]");
			throw new Exception("DataSourceManager -> No datasource registered with id ["+datasourceIdentifier+"]");
		}
		DataSource ds = (DataSource)componentContext.locateService("datasources", sr);
		return ds;
		
		
	}
	
	
	public void bind(EventAdmin ea){
		this.ea = ea;
	}

	public void unbind(EventAdmin ea){
		this.ea = null;
	}
	
	
	public void notify(String id, String eventType) {
		
		Map<String, Object> internalEventProperties = new HashMap<String, Object>();
		internalEventProperties.put(SpagicConstants._IS_INTERNAL_EVENT, true);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_TYPE, eventType);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_REFERRING_ID, id);
		Event ev = new Event(SpagicConstants.SPAGIC_GENERIC_TOPIC, internalEventProperties);
		if (this.ea != null){
			ea.postEvent(ev);
		}
	}
}
