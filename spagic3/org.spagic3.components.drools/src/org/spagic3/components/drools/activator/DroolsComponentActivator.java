package org.spagic3.components.drools.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spagic3.components.drools.invoker.IServiceInvoker;
import org.spagic3.components.drools.invoker.InvokerServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class DroolsComponentActivator implements BundleActivator {

	
	private static InvokerServiceTracker invokerServiceTracker = null;
	private static BundleContext ctx = null;
	
	

	@Override
	public void start(BundleContext context) throws Exception {
		ctx = context;
		System.setProperty("SPAGIC_HIBERNATE_OSGI_STRATEGY", "org.spagic3.components.bpm.OSGiSessionFactoryInitializer");
		invokerServiceTracker = new InvokerServiceTracker(context);
		invokerServiceTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		invokerServiceTracker.close();
	}
	
	public static IServiceInvoker getServiceInvoker(){
		return (IServiceInvoker)invokerServiceTracker.getService();
	}
	
	public static BundleContext getCtx() {
		return ctx;
	}
	
}