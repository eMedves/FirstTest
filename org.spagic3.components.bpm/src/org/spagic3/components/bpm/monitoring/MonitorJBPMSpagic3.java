package org.spagic3.components.bpm.monitoring;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.log.ProcessInstanceEndLog;
import org.spagic.metadb.base.ProcessInstanceStateConstants;
import org.spagic.metadb.base.TransitionStateConstants;
import org.spagic.metadb.dbutils.AuditDBUtils;
import org.spagic.metadb.model.Process;
import org.spagic.metadb.model.ProcessInstance;
import org.spagic.metadb.model.Step;
import org.spagic.metadb.model.TransitionInstance;
import org.spagic.monitoring.core.TransitionStateCache;
import org.spagic.monitoring.jbpm.MonitorServiceJBPM;
import org.spagic.workflow.api.jbpm.ProcessEngine;
import org.spagic3.components.bpm.BPMComponent;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.constants.SpagicConstants;

public class MonitorJBPMSpagic3 extends MonitorServiceJBPM {

	@Override
	protected void processProcessInstanceEndLog(ProcessInstanceEndLog processLog) {
		super.processProcessInstanceEndLog(processLog);
		
		// A quale istanza di processo di riferisce questo log ?
		Token token = processLog.getToken();
		org.jbpm.graph.exe.ProcessInstance jBPMProcessInstance = token.getProcessInstance();
		String xmlMessage = null;
		String orchestrationServiceId = null;
	
		ProcessInstance spagicProcessInstance = null;
		Session aSession = null;
		Transaction tx = null;
		boolean isProcessTerminated = false;
		try {
			// Retrieve current session
			aSession = getSession();
			tx = aSession.beginTransaction();

			// Retrieve Spagic process instance
			String correlationID = ProcessEngine.createSpagicCorrelationId(jBPMProcessInstance);
			spagicProcessInstance = AuditDBUtils.getProcessInstanceByIdCorrelation(aSession, correlationID);
			
			xmlMessage = (String)jBPMProcessInstance.getContextInstance().getVariable(BPMContextSingleton.XML_MESSAGE);
			orchestrationServiceId = (String)jBPMProcessInstance.getContextInstance().getVariable(BPMContextSingleton.ORCHESTRATION_SERVICE_ID);
			
			String processTerminated = (String)jBPMProcessInstance.getContextInstance().getVariable(SpagicConstants.WF_IS_PROCESS_TERMINATED);
			
			
			if (processTerminated != null)
				isProcessTerminated = Boolean.valueOf(processTerminated);
			
			aSession.flush();
			tx.commit();
			
		} catch (Exception e) {
			tx.rollback();
        	log.error("traceExchange ", e);
//        	!!!
		} finally {
			closeSession(aSession);
			BPMContextSingleton.callBack(orchestrationServiceId, spagicProcessInstance.getIdProcessInstance(), xmlMessage, isProcessTerminated);
		}
	}


}
