package org.spagic3.component.bpm.monitoring;

import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;


public class Spagic3MonitoringServiceFactory implements ServiceFactory {

	public void close() {
	}

	public Service openService() {
		return new Spagic3MonitoringService();
	}

}
