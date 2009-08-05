/*******************************************************************************
 * Copyright (c) 2005, 2008 Cognos Incorporated, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Cognos Incorporated - initial API and implementation
 *     IBM Corporation - bug fixes and enhancements
 *******************************************************************************/
package org.eclipse.equinox.servletbridge;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.spagic3.client.api.Client;

/**
 * The SpagicServlet is a customized BridgeServlet, and mantains BridgeServlet features
 * with the following add/modifications
 *
 * - Add the method to register the org.spagic3.client.api.Client
 * - Disable the service method usage/except that for sp_ command
 * 
 * Unforunately to do that i've done a small patch
 * rendering protected some field/method of Bridge Servlet
 */
public class SpagicServlet extends BridgeServlet {

	private static final String SPAGIC_DELEGATE = "SPAGIC_DELEGATE";

	/**
	 * service is called by the Servlet Container and will first determine if the request is a
	 * framework control and will otherwise try to delegate to the registered servlet delegate
	 *  
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();

		if (req.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) == null) {
			if (enableFrameworkControls) {
				if (pathInfo != null && pathInfo.startsWith("/sp_")) { //$NON-NLS-1$
					if (serviceFrameworkControls(req, resp)) {
						return;
					}
				}
			}
		}

		throw new ServletException("Spagic Servlet must not be used to serve request");

		// In your application code use as

		/*
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(instance.getFramework().getFrameworkContextClassLoader());

			Client client = (Client) getServletContext().getAttribute(SPAGIC_DELEGATE);
			if (client != null) {
				Map properties = new HashMap();
				properties.put("p1", "value of p1");
				ClientMessage msg = new ClientMessage("id1", "<ANDREA><ZOPPELLO>qqqq</ZOPPELLO></ANDREA>");
				ClientMessage response = client.invokeAndWait("groovy1", message);
				System.out.println("Message As Body" + response.getBody());
			}
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
		*/
	}

	public static synchronized void registerSpagicClient(Client spagicClientDelegate) {
		if (instance == null) {
			// shutdown already
			return;
		}

		if (spagicClientDelegate == null)
			throw new NullPointerException("cannot register a null spagic delegate"); //$NON-NLS-1$

		synchronized (instance) {
			ServletContext context = instance.getServletContext();
			if (context.getAttribute(SPAGIC_DELEGATE) != null)
				throw new IllegalStateException("A SpagicDelegate is already registered"); //$NON-NLS-1$

			context.setAttribute(SPAGIC_DELEGATE, spagicClientDelegate);
		}
	}

	public static synchronized void unregisterSpagicClient(Client spagicDelegate) {

		if (instance == null) {
			// shutdown already
			return;
		}

		synchronized (instance) {
			ServletContext context = instance.getServletContext();
			context.removeAttribute(SPAGIC_DELEGATE);
		}
	}
}