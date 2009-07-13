package org.spagic3.deployer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.SpagicConstants;
import org.spagic3.core.SpagicUtils;

public class DeploymentService implements IDeploymentService {

	private static Logger logger = LoggerFactory.getLogger(DeploymentService.class);
	private static final String DATASOURCE_FACTORY_IDENTIFIER ="spagic3.datasourcefactory";

	private ConcurrentHashMap<String, ServiceReference> factories = new ConcurrentHashMap<String, ServiceReference>();
	private ConcurrentHashMap<String, ComponentInstance> componentInstances = new ConcurrentHashMap<String, ComponentInstance>();
	private ConcurrentHashMap<String, HashMap<String, Hashtable>> pendingDeployments = new ConcurrentHashMap<String, HashMap<String,Hashtable>>();
	
	private ComponentContext componentContext = null;

	protected void activate(ComponentContext componentContext) {
		logger.info(" Deployment Service - ACTIVATED");
		this.componentContext = componentContext;
	}

	protected void deactivate(ComponentContext componentContext) {
		logger.info(" Deployment Service - DEACTIVATED");
	}

	public void addComponentFactory(ServiceReference componentFactoryReference) {
		
		String componentFactoryIdentifier = (String) componentFactoryReference
				.getProperty("component.factory");
		
		logger.info(" Component Factory ["+componentFactoryIdentifier+"] -- REGISTERED");
		this.factories.put(componentFactoryIdentifier,
				componentFactoryReference);
		
		logger.info(" Revamp Pending Deployment for ["+componentFactoryIdentifier+"]");
		HashMap<String, Hashtable> pendingDeploymentForFactory = pendingDeployments.remove(componentFactoryIdentifier);
		Hashtable pendingProp = null;
		if (pendingDeploymentForFactory != null){
			for ( String id : pendingDeploymentForFactory.keySet()){
				pendingProp = pendingDeploymentForFactory.get(id);
				String depType = (String)pendingProp.get(IDeploymentService.SPAGIC_TYPE);
				if (depType.equalsIgnoreCase(IDeploymentService.SPAGIC_DATASOURCE))
					internalDeploy(id, depType, componentFactoryIdentifier, pendingProp, false);
				else
					internalDeploy(id, depType, componentFactoryIdentifier, pendingProp, true);
			}
		}
	}

	public void removeComponentFactory(
			ServiceReference componentFactoryReference) {
		String componentFactoryIdentifier = (String) componentFactoryReference
				.getProperty("component.factory");
		
		logger.info(" Component Factory ["+componentFactoryIdentifier+"] -- UNREGISTERED");
		factories.remove(componentFactoryIdentifier);
	}

	private void internalDeploy(String id, 
								String spagicType,
								String factoryName, 
								Hashtable properties, 
								boolean subcribeOnBus) {
		
		logger.info("Deploying ["+spagicType+"] With ID ["+id+"] using ["+factoryName+"]");
				
		ServiceReference sr = factories.get(factoryName);
		properties.put(IDeploymentService.SPAGIC_TYPE, spagicType);
		
		if (sr == null) {
			logger.warn("Cannot Find A Component Factory [" + factoryName + "] for ["+spagicType+"] With ID ["+id+"]");
			if (pendingDeployments.get(factoryName) == null)
				pendingDeployments.put(factoryName, new HashMap<String, Hashtable>());
			pendingDeployments.get(factoryName).put(id, properties);
		} else {
			ComponentFactory cf = (ComponentFactory) componentContext
					.locateService("cf", sr);

			if (subcribeOnBus){
				String topicToSubscribe = SpagicConstants.SPAGIC_BASE_TOPIC+ id;
				topicToSubscribe = SpagicUtils.normalizeTopic(topicToSubscribe);
				properties.put(EventConstants.EVENT_TOPIC, topicToSubscribe);
			}
			
			ComponentInstance ci = cf.newInstance(properties);
			componentInstances.put(id, ci);
			logger.info("["+spagicType+"] With ID ["+id+"] DEPLOYED ==");
		}
	}
	
	

	protected void internalUndeploy(String id) {
		ComponentInstance ci = componentInstances.get(id);
		
		if (ci != null){
			componentInstances.remove(id);
			ci.dispose();
		}

	}
	
	public void deployDatasource(String dataSourceId,  Hashtable properties) {		
		internalDeploy(dataSourceId, IDeploymentService.SPAGIC_DATASOURCE, DeploymentService.DATASOURCE_FACTORY_IDENTIFIER, properties, false);
	}

	@Override
	public void deployConnector(String spagicId, String factoryName,
			Hashtable properties) {
		internalDeploy(spagicId, IDeploymentService.SPAGIC_CONNECTOR, factoryName, properties, true);
		
	}

	@Override
	public void deployService(String spagicId, String factoryName,
			Hashtable properties) {
		internalDeploy(spagicId, IDeploymentService.SPAGIC_SERVICE, factoryName, properties, true);
		
	}

	@Override
	public void undeployConnector(String spagicId) {
		internalUndeploy(spagicId);
		
	}

	@Override
	public void undeployDatasource(String dataSourceID) {
		internalUndeploy(dataSourceID);
		
	}

	@Override
	public void undeployService(String spagicId) {
		internalUndeploy(spagicId);
		
	}
}
