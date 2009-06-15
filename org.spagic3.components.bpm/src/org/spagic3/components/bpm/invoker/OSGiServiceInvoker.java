package org.spagic3.components.bpm.invoker;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.nmr.api.Exchange;
import org.jbpm.graph.exe.Token;
import org.spagic.workflow.api.jbpm.ProcessEngine;
import org.spagic3.core.AbstractSpagicService;
import org.spagic3.core.SpagicConstants;

public class OSGiServiceInvoker extends AbstractSpagicService implements IServiceInvoker{

	private ConcurrentHashMap<String, Long> tokens = new ConcurrentHashMap<String, Long>();
	
	@Override
	public void invokeService(String serviceID, Exchange exchange) {
		try{
			tokens.put(exchange.getId(), (Long)exchange.getProperty("Token"));
			exchange.setProperty(SpagicConstants.SPAGIC_SENDER, this.getSpagicId());
			exchange.setProperty(SpagicConstants.SPAGIC_TARGET, serviceID);
			send(exchange);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(Exchange exchange) throws Exception {
			String id = exchange.getId();
			Long tid = tokens.remove(id);
			ProcessEngine.signalToken(tid);
	} 
	
	

}
