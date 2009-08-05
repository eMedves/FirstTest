package org.spagic3.client.osgi;

import org.eclipse.equinox.servletbridge.SpagicServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spagic3.client.api.Client;

public class OSGiSideClientActivator implements BundleActivator {

	Client client = null;
	public void start(BundleContext context) throws Exception {
		client = new OSGiClientImpl(context);
		SpagicServlet.registerSpagicClient(client);
	}

	
	public void stop(BundleContext context) throws Exception {
		SpagicServlet.unregisterSpagicClient(client);
		client = null;
	}

}
