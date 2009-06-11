package org.spagic3.components.bpm.monitoring;

import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;


public class Spagic3MonitoringServiceFactory implements ServiceFactory {

	public void close() {
	}

	public Service openService() {
		return new Spagic3MonitoringService();
	}

}
