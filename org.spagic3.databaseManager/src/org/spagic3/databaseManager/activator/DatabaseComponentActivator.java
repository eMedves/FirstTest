package org.spagic3.databaseManager.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spagic3.databaseManager.datasource.IDatasourceManagerTracker;
import org.spagic3.datasource.IDataSourceManager;

public class DatabaseComponentActivator implements BundleActivator {

	
	private static IDatasourceManagerTracker iDatasourceManagerTracker = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.setProperty("SPAGIC_HIBERNATE_OSGI_STRATEGY", "org.spagic3.components.bpm.OSGiSessionFactoryInitializer");
		iDatasourceManagerTracker = new IDatasourceManagerTracker(context);
		iDatasourceManagerTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		iDatasourceManagerTracker.close();
	}
		
	public static IDataSourceManager getDataSourceManager(){
		return (IDataSourceManager)iDatasourceManagerTracker.getService();
	}
	
}
