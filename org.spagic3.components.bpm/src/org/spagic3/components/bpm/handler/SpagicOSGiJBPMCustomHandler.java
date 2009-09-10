package org.spagic3.components.bpm.handler;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.integration.api.IExchangeProvider;
import org.spagic3.integration.api.IServiceRunner;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class SpagicOSGiJBPMCustomHandler implements ActionHandler {

	private String serviceId;
	private String serviceRunnerClass;
	

	private String exchangeProviderClass;
	private String workflowContextUpdaterClass;
	
	
	
	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	
	
	public String getServiceRunnerClass() {
		return serviceRunnerClass;
	}

	public void setServiceRunnerClass(String serviceRunnerClass) {
		this.serviceRunnerClass = serviceRunnerClass;
	}
	
	public void execute(ExecutionContext ctx) throws Exception {
		
		IExchangeProvider exchangeProvider = getExchangeProvider();
		IServiceRunner runner = getServiceRunner();
		
		Exchange exchange = exchangeProvider.createExchangeFromWorkflowContext(ctx);
		exchange.setProperty(BPMContextSingleton.WORKFLOW_UPDATER_CLASS, workflowContextUpdaterClass);
		runner.run(serviceId, exchange);
		
		if (ExchangeUtils.isSync(exchange)){
			String workflowContextUpdaterClass = (String) exchange.getProperty(BPMContextSingleton.WORKFLOW_UPDATER_CLASS);
			
			IWorkflowContextUpdater updater = (IWorkflowContextUpdater)Class.forName(workflowContextUpdaterClass).newInstance();;
			updater.updateWorkflowContext(ctx, exchange);
			

		}
	}
	
	public IServiceRunner getServiceRunner() throws Exception {
		return (IServiceRunner) Class.forName(serviceRunnerClass).newInstance();
	}
	
	public IExchangeProvider getExchangeProvider()  throws Exception {
		return (IExchangeProvider) Class.forName(exchangeProviderClass).newInstance();
	}
	
	public String getExchangeProviderClass() {
		return exchangeProviderClass;
	}

	public void setExchangeProviderClass(String exchangeProviderClass) {
		this.exchangeProviderClass = exchangeProviderClass;
	}

	public String getWorkflowContextUpdaterClass() {
		return workflowContextUpdaterClass;
	}

	public void setWorkflowContextUpdaterClass(String workflowContextUpdaterClass) {
		this.workflowContextUpdaterClass = workflowContextUpdaterClass;
	}
}
