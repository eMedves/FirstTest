package org.spagic3.dirwatcher;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.SpagicConstants;
import org.spagic3.deployer.IDeploymentService;

public class DirWatcherService {
	
	protected Logger logger = LoggerFactory.getLogger(DirWatcherService.class); 
	private ConcurrentHashMap<String, String> servicesFileToIdMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> connectorsFileToIdMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> datasourcesFileToIdMap = new ConcurrentHashMap<String, String>();
	
	protected void activate(ComponentContext componentContext){
		final IDeploymentService deploymentService = (IDeploymentService)componentContext.locateService("deploymentService");
	
		String spagicHome = System.getProperty("spagic.home");
		
		if (spagicHome == null){
			logger.warn(" Spagic HOME NOT SETTED ");
			return;
		}
			
		String servicesHome = spagicHome + File.separator + SpagicConstants.SERVICES_FOLDER;
		String connectorsHome = spagicHome + File.separator + SpagicConstants.CONNECTORS_FOLDER;
		String dsHome = spagicHome + File.separator + SpagicConstants.DATASOURCES_FOLDER;

		
		TimerTask servicePollingTask = new DirWatcher(servicesHome, SpagicConstants.SERVICE_DEPLOYMENTS_EXTENSIONS ) {
			protected void onChange(File file, String action) {
				try{
					String filePath = file.getCanonicalPath();
					logger.info("DirWatcher Service File ["+filePath+"] --> Action ["+action+"]");
			
						if (action.equalsIgnoreCase("delete")){
							
							String spagicId = servicesFileToIdMap.get(filePath);
							
							if (spagicId != null){
								logger.warn("DirWatcher -> No component deployed for this file");
								deploymentService.undeployService(spagicId);
							}
							servicesFileToIdMap.remove(filePath);
							
						}else if (action.equalsIgnoreCase("add")){
							SAXReader reader = new SAXReader();
							Document doc = reader.read(file);
							
							Node n = doc.selectSingleNode("/spagic:component");
							String spagicId = n.valueOf("@spagic.id");
							String factoryName =  n.valueOf("@factory.name");
							
							List<Node> propertiesNode = doc.selectNodes("/spagic:component/spagic:property");
							
							Hashtable<String, String> properties = new Hashtable<String, String>();
							properties.put("spagic.id", spagicId);
							properties.put("factory.name", factoryName);
							for (Node pn : propertiesNode){
								properties.put(pn.valueOf("@name"), pn.valueOf("@value"));
							}
							
							deploymentService.deployService(spagicId, factoryName, properties);
							
							servicesFileToIdMap.put(filePath, spagicId);
							
						}else if (action.equalsIgnoreCase("modify")){
							logger.warn("DirWatcher Service action ["+action+"] not supported");
						}else{
							logger.warn("DirWatcher Service action ["+action+"] unknown");
						}
				}catch(Exception e){
					logger.error(e.getMessage(), e);
				}		
			}
		};

		
		TimerTask connectorsPollingTask = new DirWatcher(connectorsHome, SpagicConstants.CONNECTOR_DEPLOYMENTS_EXTENSIONS) {
			protected void onChange(File file, String action) {
				try{
					String filePath = file.getCanonicalPath();
					logger.info("DirWatcher Connector File ["+filePath+"] --> Action ["+action+"]");
			
						if (action.equalsIgnoreCase("delete")){
							
							String spagicId = connectorsFileToIdMap.get(filePath);
							
							if (spagicId != null){
								logger.warn("DirWatcher -> No component deployed for this file");
								deploymentService.undeployConnector(spagicId);
							}
							connectorsFileToIdMap.remove(filePath);
							
						}else if (action.equalsIgnoreCase("add")){
							SAXReader reader = new SAXReader();
							Document doc = reader.read(file);
							
							Node n = doc.selectSingleNode("/spagic:component");
							String spagicId = n.valueOf("@spagic.id");
							String factoryName =  n.valueOf("@factory.name");
							
							List<Node> propertiesNode = doc.selectNodes("/spagic:component/spagic:property");
							
							Hashtable<String, String> properties = new Hashtable<String, String>();
							properties.put("spagic.id", spagicId);
							properties.put("factory.name", factoryName);
							for (Node pn : propertiesNode){
								properties.put(pn.valueOf("@name"), pn.valueOf("@value"));
							}
							
							deploymentService.deployConnector(spagicId, factoryName, properties);
							
							connectorsFileToIdMap.put(filePath, spagicId);
							
						}else if (action.equalsIgnoreCase("modify")){
							logger.warn("DirWatcher Service action ["+action+"] not supported");
						}else{
							logger.warn("DirWatcher Service action ["+action+"] unknown");
						}
				}catch(Exception e){
					logger.error(e.getMessage(), e);
				}		
			}
		};
		
		
		TimerTask dataSourcesPollingTask = new DirWatcher(dsHome, SpagicConstants.DATASOURCE_DEPLOYMENTS_EXTENSIONS) {
			protected void onChange(File file, String action) {
				try{
					String filePath = file.getCanonicalPath();
					logger.info("DirWatcher Datasource File ["+filePath+"] --> Action ["+action+"]");
			
						if (action.equalsIgnoreCase("delete")){
							
							String dataSourceId = datasourcesFileToIdMap.get(filePath);
							
							if (dataSourceId != null){
								logger.warn("DirWatcher -> No component deployed for this file");
								deploymentService.undeployDatasource(dataSourceId);
							}
							datasourcesFileToIdMap.remove(filePath);
							
						}else if (action.equalsIgnoreCase("add")){
							
							SAXReader reader = new SAXReader();
							Document doc = reader.read(file);
							
							Node n = doc.selectSingleNode("/spagic:ds");
							String dataSourceId = n.valueOf("@id");
							
							List<Node> propertiesNode = doc.selectNodes("/spagic:ds/spagic:property");
							
							Hashtable<String, String> properties = new Hashtable<String, String>();
							properties.put("id", dataSourceId);

							for (Node pn : propertiesNode){
								properties.put(pn.valueOf("@name"), pn.valueOf("@value"));
							}
							
							deploymentService.deployDatasource(dataSourceId, properties);
							datasourcesFileToIdMap.put(filePath, dataSourceId);
					
							
						}else if (action.equalsIgnoreCase("modify")){
							logger.warn("DirWatcher DataSource action ["+action+"] not supported");
						}else{
							logger.warn("DirWatcher DataSource action ["+action+"] unknown");
						}
				}catch(Exception e){
					logger.error(e.getMessage(), e);
				}		
			}
		};
		
		Timer timerService = new Timer();
		timerService.schedule(servicePollingTask, new Date(), 5000);
		
		Timer timerConnectors = new Timer();
		timerConnectors.schedule(connectorsPollingTask, new Date(), 5000);
		
		Timer timerDataSources = new Timer();
		timerDataSources.schedule(dataSourcesPollingTask, new Date(), 5000);
	}
	
	protected void deactivate(ComponentContext componentContext){
	
	}
	
}
