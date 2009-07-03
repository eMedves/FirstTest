package org.spagic3.components.bpm;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class IDatasourceManagerTracker extends ServiceTracker {
	
	public IDatasourceManagerTracker(BundleContext context){
		super(context, IDatasourceManagerTracker.class.getName(), null);
	}
}
