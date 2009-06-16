package org.spagic3.components.bpm.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Pattern;
import org.jbpm.graph.exe.ExecutionContext;
import org.spagic.workflow.api.Variable;
import org.spagic.workflow.api.jbpm.ProcessEngine;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.integration.api.IExchangeProvider;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class SetVariableAndContinueProcessUpdater implements IWorkflowContextUpdater {

	


	@Override
	public void updateWorkflowContext(Object workflowContex, Exchange exchange) {
		Long tokenId =(Long) exchange.getProperty(BPMContextSingleton.TOKEN_ID_PROPERTY);
		
		Variable[] vars = new Variable[1];
		vars[0] = new Variable();
		vars[0].setName(BPMContextSingleton.XML_MESSAGE);
		
		String responseXMLMessage = (String)exchange.getOut(true).getBody();
		vars[0].setValue(responseXMLMessage);
		ProcessEngine.signalToken(tokenId, vars);
		
	}

	@Override
	public Map<String, String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		
	}

	

}
