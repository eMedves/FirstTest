package org.spagic3.deployer;

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

	private static Logger logger = LoggerFactory
			.getLogger(DeploymentService.class);

	private ConcurrentHashMap<String, ServiceReference> factories = new ConcurrentHashMap<String, ServiceReference>();
	private ConcurrentHashMap<String, ComponentInstance> componentInstances = new ConcurrentHashMap<String, ComponentInstance>();
	
	private ComponentContext componentContext = null;

	protected void activate(ComponentContext componentContext) {
		System.out.println(" Deployment Service Activated ");
		this.componentContext = componentContext;
	}

	protected void deactivate(ComponentContext componentContext) {
		System.out.println(" Deployment Service DeActivated ");
	}

	public void addComponentFactory(ServiceReference componentFactoryReference) {
		String[] propKeys = componentFactoryReference.getPropertyKeys();
		String componentFactoryIdentifier = (String) componentFactoryReference
				.getProperty("component.factory");
		for (String prop : propKeys) {
			System.out.println(prop + "->"
					+ componentFactoryReference.getProperty(prop));
		}

		this.factories.put(componentFactoryIdentifier,
				componentFactoryReference);

	}

	public void removeComponentFactory(
			ServiceReference componentFactoryReference) {
		String componentFactoryIdentifier = (String) componentFactoryReference
				.getProperty("component.factory");
		factories.remove(componentFactoryIdentifier);
	}

	public void deploy(String spagicId, String factoryName, Hashtable properties) {

		ServiceReference sr = factories.get(factoryName);

		if (sr == null) {
			logger.error("Cannot Find a component factory [" + factoryName
					+ "]");
		} else {
			ComponentFactory cf = (ComponentFactory) componentContext
					.locateService("cf", sr);

			String topicToSubscribe = SpagicConstants.SPAGIC_BASE_TOPIC+ spagicId;
			
			topicToSubscribe = SpagicUtils.normalizeTopic(topicToSubscribe);
			properties.put(EventConstants.EVENT_TOPIC, topicToSubscribe);
			
			ComponentInstance ci = cf.newInstance(properties);
			componentInstances.put(spagicId, ci);
		}
	}
	
	

	public void undeploy(String spagicId) {
		ComponentInstance ci = componentInstances.get(spagicId);
		
		if (ci != null){
			componentInstances.remove(spagicId);
			ci.dispose();
		}

	}
}
