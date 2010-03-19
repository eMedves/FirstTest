/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.internal.event.mapper;

import org.osgi.framework.*;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Main class for redeliver special events like FrameworkEvents via EventAdmin.
 * 
 * @version $Revision: 1.2 $
 */
public class EventRedeliverer implements FrameworkListener, BundleListener, ServiceListener {
	private ServiceTracker eventAdminTracker;
	private final static boolean DEBUG = false;
	private BundleContext bc;

	public EventRedeliverer(BundleContext bc) {
		this.bc = bc;
	}

	public void close() {
		if (eventAdminTracker != null) {
			eventAdminTracker.close();
			eventAdminTracker = null;
		}
		bc.removeFrameworkListener(this);
		bc.removeBundleListener(this);
		bc.removeServiceListener(this);
	}

	/**
	 * prepare any service trackers and register event listeners which are
	 * necessary to obtain events to be mapped
	 */
	public void open() {
		// open ServiceTracker for EventAdmin
		eventAdminTracker = new ServiceTracker(bc, EventAdmin.class.getName(), null);
		eventAdminTracker.open();

		// add legacy event listener for framework level event
		bc.addFrameworkListener(this);
		bc.addBundleListener(this);
		bc.addServiceListener(this);
	}

	private EventAdmin getEventAdmin() {
		if (eventAdminTracker == null)
			return null;
		return (EventAdmin) eventAdminTracker.getService();
	}

	/**
	 * @param event
	 * @see org.osgi.framework.FrameworkListener#frameworkEvent(org.osgi.framework.FrameworkEvent)
	 */
	public void frameworkEvent(FrameworkEvent event) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin != null) {
			(new FrameworkEventAdapter(event, eventAdmin)).redeliver();
		} else {
			printNoEventAdminError();
		}
	}

	private void printNoEventAdminError() {
		if (DEBUG) {
			System.out.println(this.getClass().getName() + ": Cannot find the EventAdmin."); //$NON-NLS-1$
		}
	}

	/**
	 * @param event
	 * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
	 */
	public void bundleChanged(BundleEvent event) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin != null) {
			(new BundleEventAdapter(event, eventAdmin)).redeliver();
		} else {
			printNoEventAdminError();
		}
	}

	/**
	 * @param event
	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin != null) {
			(new ServiceEventAdapter(event, eventAdmin)).redeliver();
		} else {
			printNoEventAdminError();
		}
	}

}