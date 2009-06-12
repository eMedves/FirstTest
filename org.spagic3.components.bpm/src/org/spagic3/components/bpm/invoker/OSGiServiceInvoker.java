package org.spagic3.components.bpm.invoker;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic3.core.AbstractSpagicService;
import org.spagic3.core.SpagicConstants;

public class OSGiServiceInvoker extends AbstractSpagicService implements IServiceInvoker{

	private HashMap<String, Exchange> pendingExchanges = new HashMap<String, Exchange>();
	private HashMap<String, Exchange> readyExhanges = new HashMap<String, Exchange>();
	private HashMap<String, Thread> threadMap = new HashMap<String, Thread>();
	
	@Override
	public void invokeService(String serviceID, Exchange exchange) {
		synchronized (this) {
			pendingExchanges.put(exchange.getId(), exchange);
			threadMap.put(exchange.getId(), Thread.currentThread());
		}
		try{
			exchange.setProperty(SpagicConstants.SPAGIC_SENDER, this.getSpagicId());
			exchange.setProperty(SpagicConstants.SPAGIC_TARGET, serviceID);
			getMessageRouter().send(exchange);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		synchronized (this) {
			String id = exchange.getId();
			pendingExchanges.remove(id);
			readyExhanges.put(id, exchange);
			threadMap.remove(id).notify();
		}
		
	} 
	
	public boolean isReady(String exchangeId){
		return readyExhanges.containsKey(exchangeId);
	}

	@Override
	public Exchange getReadyExchange(String exchangeId) {
		
		return readyExhanges.remove(exchangeId);
	}

}
