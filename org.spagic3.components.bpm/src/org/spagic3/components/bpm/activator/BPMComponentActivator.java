package org.spagic3.components.bpm.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spagic3.components.bpm.invoker.IServiceInvoker;
import org.spagic3.components.bpm.invoker.InvokerServiceTracker;

public class BPMComponentActivator implements BundleActivator {

	
	private static InvokerServiceTracker invokerServiceTracker = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
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
	
}
