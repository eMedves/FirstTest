package org.spagic3.components.bpm;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic3.components.bpm.activator.BPMComponentActivator;
import org.spagic3.components.bpm.invoker.IServiceInvoker;
import org.spagic3.integration.api.IServiceRunner;

public class OSGiServiceRunner implements IServiceRunner {
	
	public void run(String serviceID, Exchange exchange) {
		
		IServiceInvoker invoker = BPMComponentActivator.getServiceInvoker();
		invoker.invokeService(serviceID, exchange);
	
		String exchangeId = exchange.getId();
		
		while(invoker.isReady(exchangeId)){
			try {
                wait();
            } catch (Exception e) {
            }
		}
	}
}
