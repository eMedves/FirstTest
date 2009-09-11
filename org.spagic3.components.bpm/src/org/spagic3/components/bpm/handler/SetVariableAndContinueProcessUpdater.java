package org.spagic3.components.bpm.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.jbpm.graph.exe.ExecutionContext;
import org.spagic.workflow.api.Variable;
import org.spagic.workflow.api.jbpm.ProcessEngine;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class SetVariableAndContinueProcessUpdater implements IWorkflowContextUpdater {


	@Override
	public void updateWorkflowContext(Object workflowContex, Exchange exchange) {
		if (workflowContex == null)
			prepareVariablesAndSignal(exchange);
		else
			updateWorkflowContextAndSignal(workflowContex,exchange);
	}
	
	public void updateWorkflowContextAndSignal(Object workflowContex,Exchange exchange) {
		
		ExecutionContext context = (ExecutionContext)workflowContex;
		org.jbpm.graph.exe.ProcessInstance pi = context.getToken().getProcessInstance();
		
		Variable[] vars = extractVariablesFromExchange(exchange);
		for (int i = 0; (vars != null && i < vars.length); i++) {
			pi.getContextInstance().setVariable(vars[i].getName(), vars[i].getValue(), context.getToken());
		}
		context.getJbpmContext().save(pi);
		context.getToken().signal();
	}
	
	public Variable[] extractVariablesFromExchange(Exchange exchange) {
		List<Variable> vars = new ArrayList<Variable>();
		
		
		String noUpdateToXMLMessage = (String) exchange.getProperty(SpagicConstants.WF_NO_UPDATE_XML_MESSAGE);
		
		boolean noUpdate = noUpdateToXMLMessage != null ? Boolean.valueOf(noUpdateToXMLMessage) : false;
		
		if (!noUpdate){
			Variable var = new Variable();
			var.setName(BPMContextSingleton.XML_MESSAGE);
			// no Update is false proceed
			String responseXMLMessage = (String)exchange.getOut(true).getBody();
			var.setValue(responseXMLMessage);
			vars.add(var);
		}else{
			Variable var = new Variable();
			var.setName(SpagicConstants.WF_NO_UPDATE_XML_MESSAGE);
			var.setValue(noUpdateToXMLMessage);
			vars.add(var);
		}
		
		
		Map<String, Object> exchangeProperties = exchange.getProperties();
		
		for ( String exchangePropertiesKey : exchangeProperties.keySet()){
			if (exchangePropertiesKey.startsWith(SpagicConstants.WF_VARIABLE_PREFIX)){
				String realWfvarName = exchangePropertiesKey.substring(exchangePropertiesKey.lastIndexOf(".")+1);
				Variable v = new Variable();
				v.setName(realWfvarName);
				v.setValue(exchangeProperties.get(exchangePropertiesKey));
				vars.add(v);
			}
		}
		Variable[] varArray = new Variable[vars.size()];
		varArray = vars.toArray(varArray);
		return varArray;
	}
	public void prepareVariablesAndSignal(Exchange exchange) {
	
			// If Exchange is Async we must signal with static method
			Long tokenId =(Long) exchange.getProperty(BPMContextSingleton.TOKEN_ID_PROPERTY);
			ProcessEngine.signalToken(tokenId, extractVariablesFromExchange(exchange));
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
