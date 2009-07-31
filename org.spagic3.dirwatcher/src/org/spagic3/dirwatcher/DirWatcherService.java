package org.spagic3.dirwatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.deployer.IDeploymentService;

public class DirWatcherService {
	
	protected Logger logger = LoggerFactory.getLogger(DirWatcherService.class); 
	private ConcurrentHashMap<String, String> servicesFileToIdMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> connectorsFileToIdMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> datasourcesFileToIdMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, List<String>> routesForFileMap = new ConcurrentHashMap<String, List<String>>();
	
	
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
		String routesHome = spagicHome + File.separator + SpagicConstants.ROUTES_FOLDER;
		
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
							List<Node>  xpropertiesNode = doc.selectNodes("/spagic:component/spagic:xproperty");
							
							Hashtable<String, String> properties = new Hashtable<String, String>();
							properties.put("spagic.id", spagicId);
							properties.put("factory.name", factoryName);
							for (Node pn : propertiesNode){
								properties.put(pn.valueOf("@name"), pn.valueOf("@value"));
							}
							
							for (Node pn : xpropertiesNode){
								Iterator<Element> elIterator = ((Element)pn).elementIterator();
								if (elIterator.hasNext()){
									String value = elIterator.next().asXML();
									properties.put(pn.valueOf("@name"), value);
								}else{
									throw new IllegalStateException("XProperties error - an XProperty Must Have a Child Element");
								}
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
							List<Node>  xpropertiesNode = doc.selectNodes("/spagic:component/spagic:xproperty");
							
							Hashtable<String, String> properties = new Hashtable<String, String>();
							properties.put("spagic.id", spagicId);
							properties.put("factory.name", factoryName);
							for (Node pn : propertiesNode){
								properties.put(pn.valueOf("@name"), pn.valueOf("@value"));
							}
							
							for (Node pn : xpropertiesNode){
								Iterator<Element> elIterator = ((Element)pn).elementIterator();
								if (elIterator.hasNext()){
									String value = elIterator.next().asXML();
									properties.put(pn.valueOf("@name"), value);
								}else{
									throw new IllegalStateException("XProperties error - an XProperty Must Have a Child Element");
								}
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
		
		TimerTask routesPollingTask = new DirWatcher(routesHome, SpagicConstants.ROUTES_DEPLOYMENTS_EXTENSIONS ) {
			protected void onChange(File file, String action) {
				try{
					String filePath = file.getCanonicalPath();
					logger.info("DirWatcher Routes File ["+filePath+"] --> Action ["+action+"]");
			
						if (action.equalsIgnoreCase("delete")){
							
							List<String> routesForFile = routesForFileMap.get(filePath);
							deploymentService.deployRoutes(null, routesForFile);
							routesForFileMap.remove(filePath);
							
						}else {
							SAXReader reader = new SAXReader();
							Document doc = reader.read(file);
							
							List<Node> routesNode  = doc.selectNodes("/spagic:routes/spagic:route");
							
							
							List<String> updateRoutesForFile = new ArrayList<String>();
							for (Node pn : routesNode){
								updateRoutesForFile.add(pn.valueOf("@from")+";"+ pn.valueOf("@to"));
							}
							if (action.equalsIgnoreCase("add")){
								deploymentService.deployRoutes(updateRoutesForFile, null);
							} else if (action.equalsIgnoreCase("modify")){
								List<String> oldRoutes = routesForFileMap.get(filePath);
								deploymentService.deployRoutes(updateRoutesForFile, oldRoutes);
							}
							routesForFileMap.put(filePath, updateRoutesForFile);
							
							logger.warn("DirWatcher Service action ["+action+"] ");
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
		
		Timer timerRouterService = new Timer();
		timerRouterService.schedule(routesPollingTask, new Date(), 5000);
	}
	
	protected void deactivate(ComponentContext componentContext){
	
	}
	
}
