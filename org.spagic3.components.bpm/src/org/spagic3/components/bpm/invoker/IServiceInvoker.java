package org.spagic3.components.bpm.invoker;

import org.apache.servicemix.nmr.api.Exchange;

public interface IServiceInvoker {
	
	public void invokeService(String serviceID, Exchange exchange);
	public boolean isReady(String exchangeId);
	public Exchange getReadyExchange(String exchangeId);
}
