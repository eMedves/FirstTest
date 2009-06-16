package org.spagic3.components.bpm;

import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic3.components.bpm.activator.BPMComponentActivator;
import org.spagic3.components.bpm.invoker.IServiceInvoker;

import org.spagic3.integration.api.IServiceRunner;

public class OSGiServiceRunner implements IServiceRunner {

	@Override
	public void run(String serviceID, Exchange exchange) {
		IServiceInvoker invoker = BPMComponentActivator.getServiceInvoker();
		
		invoker.invokeService(serviceID, exchange);

	}	
	/*
	
	*/

	@Override
	public Map<String, String> getProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		
	}
}
