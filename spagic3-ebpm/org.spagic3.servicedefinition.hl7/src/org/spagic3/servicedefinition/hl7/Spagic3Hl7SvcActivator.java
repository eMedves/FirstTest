package org.spagic3.servicedefinition.hl7;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Spagic3Hl7SvcActivator implements BundleActivator {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.spagic3.servicedefinition.hl7";

	public static final String HL72XML = "plain2xml";
	public static final String XML2HL7 = "xml2plain";

	// The shared instance
	private static Spagic3Hl7SvcActivator plugin;

	private BundleContext context;
	
	/**
	 * The constructor
	 */
	public Spagic3Hl7SvcActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		plugin = this;

	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Spagic3Hl7SvcActivator getDefault() {
		return plugin;
	}

	public URL getFileURL(String path){
		Bundle bundle = context.getBundle();
		URL url = bundle.getEntry(path);
		return url;
	}
	
}
