package org.spagic3.component.bpm.monitoring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.logging.LoggingService;
import org.jbpm.logging.log.ProcessLog;
import org.spagic.monitoring.jbpm.MonitorServiceJBPM;
import org.spagic.monitoring.jbpm.message.JBPMMessage;


public class Spagic3MonitoringService implements LoggingService {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(Spagic3MonitoringService.class);

	private MonitorServiceJBPM monitorService = null;
	
	public Spagic3MonitoringService() {
		super();
    	monitorService = new MonitorServiceJBPM(/*jbpmMessage*/);
	}

	public void log(ProcessLog processLog) {
		JBPMMessage jbpmMessage = new JBPMMessage(processLog, log);
		monitorService.traceMessage(jbpmMessage);
	}

	public void close() {
	}

}
