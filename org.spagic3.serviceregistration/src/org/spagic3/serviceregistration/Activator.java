package org.spagic3.serviceregistration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.model.Service;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ISpagicService;
import org.spagic3.databaseManager.IDatabaseManager;
import org.spagic3.service.model.IServiceModelHelper;

public class Activator implements BundleActivator {

	private ServiceTracker spagicServiceTracker;
	private ServiceTracker dbServiceTracker;
	private ServiceTracker serviceModelTracker;
	
	protected Logger log = LoggerFactory.getLogger(Activator.class);

	public static final List<String> TRANSIENT_PROPERTIES = Arrays.asList(SpagicConstants.SPAGIC_FACTORYNAME,
																		  SpagicConstants.SPAGIC_TYPE,
																		  EventConstants.EVENT_TOPIC);


	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {

//		dbServiceTracker = new ServiceTracker(context, IDatabaseManager.class
//				.getName(), null);
//		dbServiceTracker.open();
//
//		serviceModelTracker = new ServiceTracker(context, IServiceModelHelper.class
//				.getName(), null);
//		serviceModelTracker.open();
//
//		spagicServiceTracker = new ServiceTracker(context, ISpagicService.class
//				.getName(), new SpagicServiceCustomizer());
//		spagicServiceTracker.open();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {		
		// close the service tracker
//		spagicServiceTracker.close();
//		spagicServiceTracker = null;
//
//		dbServiceTracker.close();
//		dbServiceTracker = null;
//		
//		serviceModelTracker.close();
//		serviceModelTracker = null;
	}
	
	public IDatabaseManager getDbManager(){
		System.out.println("##########" + dbServiceTracker);
		System.out.println("##########" + dbServiceTracker.getService());
		return (IDatabaseManager)dbServiceTracker.getService();
	}
	
	public IServiceModelHelper getServiceModelHelper(){
		return (IServiceModelHelper)serviceModelTracker.getService();
	}
	
	public class DatabaseServiceTracker extends ServiceTracker {
		
		public DatabaseServiceTracker(BundleContext context){
			super(context, IDatabaseManager.class.getName(), null);
		}
	}

	public class SpagicServiceCustomizer implements ServiceTrackerCustomizer {
		
		@Override
		public Object addingService(ServiceReference reference) {
			// Try to register the service into the database
			String serviceId = (String) reference.getProperty(SpagicConstants.SPAGIC_ID_PROPERTY);
			String factoryName = (String) reference.getProperty(SpagicConstants.SPAGIC_FACTORYNAME);
			String type = (String) reference.getProperty(SpagicConstants.SPAGIC_TYPE);
			
			Map<String, String> properties = new HashMap<String, String>(reference.getPropertyKeys().length);
			for (String propKey : reference.getPropertyKeys()) {
				Object propValue = reference.getProperty(propKey);
				log.debug(propKey + "=" + propValue);
				
				if ((propValue instanceof String) && (!TRANSIENT_PROPERTIES.contains(propKey))) {
					properties.put(propKey, (String) propValue);
				}
			}
			
			if (factoryName == null) {
				return reference;
			}
			
			String componentName = getServiceModelHelper().getComponentName(factoryName);
			if ((componentName != null) && (componentName.length() != 0)) {
				Service service = getDbManager().registerService(serviceId, componentName, properties);				
			}

			return reference;
		}

		@Override
		public void modifiedService(ServiceReference reference, Object service) {}

		@Override
		public void removedService(ServiceReference reference, Object service) {}
		
	}

}
