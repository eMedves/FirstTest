package org.spagic3.components.bpm.handler;

import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Pattern;
import org.jbpm.graph.exe.ExecutionContext;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.integration.api.IExchangeProvider;

public class VariableIntoMessageExchangeProvider implements IExchangeProvider {

	
	public Exchange createExchangeFromWorkflowContext(Object workflowContext) {
		ExecutionContext context = (ExecutionContext)workflowContext;
		
		Exchange exchange = ExchangeUtils.createExchange(Pattern.InOut);
		String xmlMessage = (String)context.getVariable(BPMContextSingleton.XML_MESSAGE);
		exchange.getIn(true).setBody(xmlMessage);
		exchange.setProperty(BPMContextSingleton.TOKEN_ID_PROPERTY, context.getToken().getId());
		return exchange;
	}

	
	public Map<String, String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		
	}

	

}
