package org.spagic3.components.bpm.invoker;

import java.util.concurrent.ConcurrentHashMap;


import org.apache.servicemix.nmr.api.Exchange;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.components.bpm.BPMComponent;
import org.spagic3.components.bpm.BPMContextSingleton;
import org.spagic3.components.bpm.activator.BPMComponentActivator;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.AbstractSpagicService;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.ISpagicService;
import org.spagic3.integration.api.IWorkflowContextUpdater;

public class OSGiServiceInvoker extends AbstractSpagicService implements IServiceInvoker{

	private ConcurrentHashMap<String, Exchange> storedExchanges = new ConcurrentHashMap<String, Exchange>();
	private Logger logger = LoggerFactory.getLogger(OSGiServiceInvoker.class);
	
	@Override
	public void invokeService(String serviceID, Exchange exchange) {
		try{
			storedExchanges.put(exchange.getId(), exchange);
			logger.info(" Storing exchange ["+exchange.getId()+"] Associated to ["+this.getSpagicId()+"] -> ["+serviceID+"]");
			exchange.setProperty(SpagicConstants.SPAGIC_SENDER, this.getSpagicId());
			exchange.setProperty(SpagicConstants.SPAGIC_TARGET, serviceID);
			if (ExchangeUtils.isSync(exchange)){
				invokeSync(serviceID, exchange);
				process(exchange);
			}else{
				send(exchange);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void invokeSync(String serviceID, Exchange exchange) {
		ServiceReference[] refs = null;
		try {

			refs = BPMComponentActivator.getCtx().getServiceReferences(
					ISpagicService.class.getName(),
					"(" + SpagicConstants.SPAGIC_ID_PROPERTY + "=" + serviceID
							+ ")");
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
			service = (ISpagicService) BPMComponentActivator.getCtx()
					.getService(refs[0]);

			if (service != null) {
				try {

					service.process(exchange);

				} finally {
					BPMComponentActivator.getCtx().ungetService(refs[0]);
				}
			} else {
				throw new IllegalStateException("Service is null");
			}
		} else {
			throw new IllegalStateException("Service Reference is null");
		}
	}

	@Override
	public void process(Exchange responseExchange) {
		try{
			logger.info(" ============= ["+ responseExchange.getProperty(SpagicConstants.SPAGIC_SENDER) + "] -> ["+responseExchange.getProperty(SpagicConstants.SPAGIC_TARGET) +"]");
			String id  = responseExchange.getId();
			logger.info("  Removing ["+responseExchange.getId()+"]");
			Exchange storedExchange = storedExchanges.remove(id);
			
			Long tokenId =(Long) storedExchange.getProperty(BPMContextSingleton.TOKEN_ID_PROPERTY);
			responseExchange.setProperty(BPMContextSingleton.TOKEN_ID_PROPERTY, tokenId);
			if (!ExchangeUtils.isSync(responseExchange)){
				String workflowContextUpdaterClass = (String) storedExchange.getProperty(BPMContextSingleton.WORKFLOW_UPDATER_CLASS);
			
				IWorkflowContextUpdater updater = (IWorkflowContextUpdater)Class.forName(workflowContextUpdaterClass).newInstance();;
				updater.updateWorkflowContext(null, responseExchange);
			}
			
		}catch (ClassNotFoundException cnfe) {
				throw new IllegalStateException(cnfe.getMessage(), cnfe);
		}catch (IllegalAccessException iae) {
				throw new IllegalStateException(iae.getMessage(), iae);
		}catch (InstantiationException ie) {
			throw new IllegalStateException(ie.getMessage(), ie);
		}
	} 
	
	
	
	

}
