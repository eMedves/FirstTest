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
import org.spagic3.deployer.IDeploymentService;

public class DirWatcherService {
	
	protected Logger logger = LoggerFactory.getLogger(DirWatcherService.class); 
	private ConcurrentHashMap<String, String> filesToIDMap = new ConcurrentHashMap<String, String>();
	
	protected void activate(ComponentContext componentContext){
		final IDeploymentService deploymentService = (IDeploymentService)componentContext.locateService("deploymentService");
	
		TimerTask task = new DirWatcher("c:/temp/spagic3", "service") {
			protected void onChange(File file, String action) {
				try{
					String filePath = file.getCanonicalPath();
					logger.info("DirWatcher Service File ["+filePath+"] --> Action ["+action+"]");
			
						if (action.equalsIgnoreCase("delete")){
							
							String spagicId = filesToIDMap.get(filePath);
							
							if (spagicId != null){
								logger.warn("DirWatcher -> No component deployed for this file");
								deploymentService.undeploy(spagicId);
							}
							filesToIDMap.remove(filePath);
							
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
							
							deploymentService.deploy(spagicId, factoryName, properties);
							
							filesToIDMap.put(filePath, spagicId);
							
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

		Timer timer = new Timer();
		timer.schedule(task, new Date(), 5000);
	}
	
	protected void deactivate(ComponentContext componentContext){
	
	}
	
}
