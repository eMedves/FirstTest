package org.spagic3.components.bpm.invoker;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.nmr.api.Exchange;
import org.jbpm.graph.exe.Token;
import org.spagic.workflow.api.Variable;
import org.spagic.workflow.api.jbpm.ProcessEngine;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.core.AbstractSpagicService;
import org.spagic3.core.SpagicConstants;
import org.spagic3.integration.api.IExchangeProvider;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class OSGiServiceInvoker extends AbstractSpagicService implements IServiceInvoker{

	private ConcurrentHashMap<String, Exchange> storedExchanges = new ConcurrentHashMap<String, Exchange>();
	
	@Override
	public void invokeService(String serviceID, Exchange exchange) {
		try{
			storedExchanges.put(exchange.getId(), exchange);
			exchange.setProperty(SpagicConstants.SPAGIC_SENDER, this.getSpagicId());
			exchange.setProperty(SpagicConstants.SPAGIC_TARGET, serviceID);
			send(exchange);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(Exchange responseExchange) throws Exception {
			String id  = responseExchange.getId();
			Exchange storedExchange = storedExchanges.remove(id);
			
			Long tokenId =(Long) storedExchange.getProperty(BPMContextSingleton.TOKEN_ID_PROPERTY);
			responseExchange.setProperty(BPMContextSingleton.TOKEN_ID_PROPERTY, tokenId);
			String workflowContextUpdaterClass = (String) storedExchange.getProperty(BPMContextSingleton.WORKFLOW_UPDATER_CLASS);
			
			IWorkflowContextUpdater updater = (IWorkflowContextUpdater)Class.forName(workflowContextUpdaterClass).newInstance();;
			updater.updateWorkflowContext(null, responseExchange);
	} 
	
	
	
	

}
