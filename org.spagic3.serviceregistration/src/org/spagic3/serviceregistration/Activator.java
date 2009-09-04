package org.spagic3.serviceregistration;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.spagic.metadb.model.Service;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ISpagicService;
import org.spagic3.databaseManager.IDatabaseManager;
import org.spagic3.service.model.ServiceModelHelper;

public class Activator implements BundleActivator {

	private ServiceTracker spagicServiceTracker;
	private ServiceTracker dbServiceTracker;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {

		dbServiceTracker = new ServiceTracker(context, IDatabaseManager.class
				.getName(), null);
		dbServiceTracker.open();

		spagicServiceTracker = new ServiceTracker(context, ISpagicService.class
				.getName(), new SpagicServiceCustomizer());
		spagicServiceTracker.open();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {		
		// close the service tracker
		spagicServiceTracker.close();
		spagicServiceTracker = null;

		dbServiceTracker.close();
		dbServiceTracker = null;		
	}
	
	public IDatabaseManager getDbManager(){
		return (IDatabaseManager)dbServiceTracker.getService();
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
				System.out.println("########### " + propKey + "=" + reference.getProperty(propKey));
			}
			
//			String componentName = ServiceModelHelper.getInstance().getComponentName(factoryName);
//			Service service = getDbManager().registerService(serviceId, componentName, properties);

			return reference;
		}

		@Override
		public void modifiedService(ServiceReference reference, Object service) {
			// Try to update the service into the database
//			Service service = registerService(String serviceId, String componentName,
//					Map<String, String> properties);
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
		}
		
	}

}
