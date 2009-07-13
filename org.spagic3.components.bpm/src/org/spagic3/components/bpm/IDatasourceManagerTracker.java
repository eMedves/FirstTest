package org.spagic3.components.bpm;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.spagic3.datasource.IDataSourceManager;

public class IDatasourceManagerTracker extends ServiceTracker {
	
	public IDatasourceManagerTracker(BundleContext context){
		super(context, IDataSourceManager.class.getName(), null);
	}
}
