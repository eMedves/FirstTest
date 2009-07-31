package org.spagic3.core.routing;

import org.apache.servicemix.nmr.api.Exchange;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.AbstractSpagicConnector;
import org.spagic3.core.ExchangeUtils;

public class SpagicRouter implements IMessageRouter {
	
	private static Logger logger = LoggerFactory.getLogger(SpagicRouter.class);
	
	private EventAdmin ea = null;
	private IDynamicRouter dynamicRouter = null;
	
	
	
	public void send(Exchange exchange) throws Exception {
		String sender = (String)exchange.getProperties().get(SpagicConstants.SPAGIC_SENDER);
		// Target Configuration
		String target = (String)exchange.getProperties().get(SpagicConstants.SPAGIC_TARGET);
		
		logger.info("Spagic Router SEND EXCHANGE ["+exchange.getId()+"] ["+sender+"] ["+target+"] Status ["+exchange.getStatus().toString()+"]");
		// If target is not configured as a properties of the exchange
		// Se if there's a connector routing map
		
		
		if (target == null){
			target = dynamicRouter.getTarget(sender);
			exchange.setProperty(SpagicConstants.SPAGIC_TARGET, target);
		}
		
		if (target == null)
			throw new Exception(" No Target Configured ");
		if (this.ea != null){
			ea.postEvent(ExchangeUtils.toEvent(exchange));
		}
	}
		


	/*
	public boolean sendSync(Exchange exchange) throws Exception {
		String sender = (String)exchange.getProperties().get(SpagicConstants.SPAGIC_SENDER);
		// Target Configuration
		String target = (String)exchange.getProperties().get(SpagicConstants.SPAGIC_TARGET);
		
		// If target is not configured as a properties of the exchange
		// Se if there's a connector routing map
		
		
		if (target == null)
			target = connectorRoutingService.getTarget(sender);
		
		if (target == null)
			throw new Exception(" No Target Configured ");
		if (this.ea != null){
			ea.sendEvent(ExchangeUtils.toEvent(exchange));
			return true;
		}
		return false;
	}
*/
	
	public void bind(EventAdmin ea){
		this.ea = ea;
	}

	public void unbind(EventAdmin ea){
		this.ea = null;
	}
	
	public void bindDynamicRouter(IDynamicRouter dr){
		this.dynamicRouter = dr;
	}

	public void unbindDynamicRouter(IDynamicRouter dr){
		this.dynamicRouter = null;
	}
}
