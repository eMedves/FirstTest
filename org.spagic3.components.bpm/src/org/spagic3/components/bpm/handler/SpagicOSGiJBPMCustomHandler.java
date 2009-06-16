package org.spagic3.components.bpm.handler;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.integration.api.IServiceRunner;

public class SpagicOSGiJBPMCustomHandler implements ActionHandler {

	private String serviceId;
	private String inputVariableName;
	private String outputVariableName;
	private String serviceRunnerClass;

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getInputVariableName() {
		return inputVariableName;
	}

	public void setInputVariableName(String inputVariableName) {
		this.inputVariableName = inputVariableName;
	}

	public String getOutputVariableName() {
		return outputVariableName;
	}

	public void setOutputVariableName(String outputVariableName) {
		this.outputVariableName = outputVariableName;
	}
	
	public String getServiceRunnerClass() {
		return serviceRunnerClass;
	}

	public void setServiceRunnerClass(String serviceRunnerClass) {
		this.serviceRunnerClass = serviceRunnerClass;
	}
	
	public void execute(ExecutionContext ctx) throws Exception {
		//get service id
		String xmlMessage = (String) ctx.getVariable(getInputVariableName());

		Exchange exchange = ExchangeUtils.createExchange(Pattern.InOut);
		exchange.getIn(true).setBody(xmlMessage);
		
		exchange.setProperty("Token", ctx.getToken().getId());
		IServiceRunner runner = getServiceRunner();
		
		runner.run(serviceId, exchange);
		
		
	}
	
	public IServiceRunner getServiceRunner() throws Exception {
		return (IServiceRunner) Class.forName(serviceRunnerClass).newInstance();
	}
	
}
