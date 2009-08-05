package org.spagic3.components.bpm.invoker;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.AbstractSpagicService;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class OSGiServiceInvoker extends AbstractSpagicService implements IServiceInvoker{

	private ConcurrentHashMap<String, Exchange> storedExchanges = new ConcurrentHashMap<String, Exchange>();
	
	@Override
	public void invokeService(String serviceID, Exchange exchange) {
		try{
			storedExchanges.put(exchange.getId(), exchange);
			System.out.println(" Storing exchange ["+exchange.getId()+"] Associated to ["+this.getSpagicId()+"] -> ["+serviceID+"]");
			exchange.setProperty(SpagicConstants.SPAGIC_SENDER, this.getSpagicId());
			exchange.setProperty(SpagicConstants.SPAGIC_TARGET, serviceID);
			send(exchange);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(Exchange responseExchange) {
		try{
			System.out.println("============== ["+ responseExchange.getProperty(SpagicConstants.SPAGIC_SENDER) + "] -> ["+responseExchange.getProperty(SpagicConstants.SPAGIC_TARGET) +"]");
			String id  = responseExchange.getId();
			System.out.println(" Removing ["+responseExchange.getId()+"]");
			Exchange storedExchange = storedExchanges.remove(id);
			
			Long tokenId =(Long) storedExchange.getProperty(BPMContextSingleton.TOKEN_ID_PROPERTY);
			responseExchange.setProperty(BPMContextSingleton.TOKEN_ID_PROPERTY, tokenId);
			String workflowContextUpdaterClass = (String) storedExchange.getProperty(BPMContextSingleton.WORKFLOW_UPDATER_CLASS);
			
			IWorkflowContextUpdater updater = (IWorkflowContextUpdater)Class.forName(workflowContextUpdaterClass).newInstance();;
			updater.updateWorkflowContext(null, responseExchange);
			
		}catch (ClassNotFoundException cnfe) {
				throw new IllegalStateException(cnfe.getMessage(), cnfe);
		}catch (IllegalAccessException iae) {
				throw new IllegalStateException(iae.getMessage(), iae);
		}catch (InstantiationException ie) {
			throw new IllegalStateException(ie.getMessage(), ie);
		}
	} 
	
	
	
	

}
