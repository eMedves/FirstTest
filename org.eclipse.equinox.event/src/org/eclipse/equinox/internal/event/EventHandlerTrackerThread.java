package org.eclipse.equinox.internal.event;

import java.security.Permission;

import org.osgi.service.event.Event;

public class EventHandlerTrackerThread implements Runnable {

	public EventHandlerWrapper evHandler = null;
	public Event event = null;
	public Permission listenerObject = null;

	public EventHandlerTrackerThread(EventHandlerWrapper evHandler,
			Event event, Permission listenerObject) {
		super();
		this.evHandler = evHandler;
		this.event = event;
		this.listenerObject = listenerObject;
	}

	public void run() {
			evHandler.handleEvent(event, listenerObject);
	}

}
