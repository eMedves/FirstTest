package org.spagic3.components.bpm.invoker;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class InvokerServiceTracker extends ServiceTracker {
	
	public InvokerServiceTracker(BundleContext context){
		super(context, IServiceInvoker.class.getName(), null);
	}
}
