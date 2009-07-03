package org.spagic3.datasource;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataSourceManager implements IDataSourceManager {
	
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
		
		

		
	}

	public void removeDataSource(
			ServiceReference dataSourceServiceReference) {

		String dsIdentifier = (String) dataSourceServiceReference.getProperty("id");
		
		logger.info(" DataSource ["+dsIdentifier+"] -- UNREGISTERED");
		datasources.remove(dsIdentifier);

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
}
