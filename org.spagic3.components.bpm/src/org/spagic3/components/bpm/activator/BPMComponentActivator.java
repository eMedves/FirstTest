package org.spagic3.components.bpm.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spagic3.components.bpm.IDatasourceManagerTracker;
import org.spagic3.components.bpm.invoker.IServiceInvoker;
import org.spagic3.components.bpm.invoker.InvokerServiceTracker;
import org.spagic3.datasource.IDataSourceManager;

public class BPMComponentActivator implements BundleActivator {

	
	private static InvokerServiceTracker invokerServiceTracker = null;
	private static IDatasourceManagerTracker iDatasourceManagerTracker = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.setProperty("SPAGIC_HIBERNATE_OSGI_STRATEGY", "org.spagic3.components.bpm.OSGiSessionFactoryInitializer");
		invokerServiceTracker = new InvokerServiceTracker(context);
		iDatasourceManagerTracker = new IDatasourceManagerTracker(context);
		iDatasourceManagerTracker.open();
		invokerServiceTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		iDatasourceManagerTracker.close();
		invokerServiceTracker.close();
	}
	
	
	public static IServiceInvoker getServiceInvoker(){
		return (IServiceInvoker)invokerServiceTracker.getService();
	}
	
	public static IDataSourceManager getDataSourceManager(){
		return (IDataSourceManager)iDatasourceManagerTracker.getService();
	}
	
}
