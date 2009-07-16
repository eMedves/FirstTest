package org.spagic3.components.bpm.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic.workflow.api.Variable;
import org.spagic.workflow.api.jbpm.ProcessEngine;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class SetVariableAndContinueProcessUpdater implements IWorkflowContextUpdater {

	


	@Override
	public void updateWorkflowContext(Object workflowContex, Exchange exchange) {
		Long tokenId =(Long) exchange.getProperty(BPMContextSingleton.TOKEN_ID_PROPERTY);
		
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
		ProcessEngine.signalToken(tokenId, varArray);
		
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
