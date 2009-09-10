package org.spagic3.components.bpm;

import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.spagic3.components.bpm.activator.BPMComponentActivator;
import org.spagic3.components.bpm.invoker.IServiceInvoker;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.ISpagicService;

import org.spagic3.integration.api.IServiceRunner;

public class OSGiServiceRunner implements IServiceRunner {

	@Override
	public void run(String serviceID, Exchange exchange) {
		if (ExchangeUtils.isSync(exchange)){
			runSync(serviceID,exchange);
		}else{
			runASync(serviceID, exchange);
		}
	}	
	
	public void runSync(String serviceID, Exchange exchange) {
		ServiceReference[] refs = null;
		try {

			refs = BPMComponentActivator.getCtx().getServiceReferences(ISpagicService.class.getName(),
					"(" + SpagicConstants.SPAGIC_ID_PROPERTY + "="
							+ serviceID + ")");
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		if (refs == null || (refs.length == 0))
			throw new IllegalStateException("Service with spagic.id ["
					+ serviceID + "] not found");
		if (refs != null && (refs.length > 1))
			throw new IllegalStateException(
					"Founded more than one service with the same spagicId ["
							+ serviceID + "]");

		ISpagicService service = null;

		if (refs[0] != null) {
			service = (ISpagicService) BPMComponentActivator.getCtx().getService(refs[0]);

			if (service != null) {
				try {
					
					service.process(exchange);
					
				} finally {
					BPMComponentActivator.getCtx().ungetService(refs[0]);
				}
			}else{
				throw new IllegalStateException(
						"Service is null");
			}
		}else{
			throw new IllegalStateException(
			"Service Reference is null");
		}
	}
	public void runASync(String serviceID, Exchange exchange) {
		IServiceInvoker invoker = BPMComponentActivator.getServiceInvoker();
		invoker.invokeService(serviceID, exchange);
	}
	
	
	@Override
	public Map<String, String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		// TODO Auto-generated method stub
		
	}
}
