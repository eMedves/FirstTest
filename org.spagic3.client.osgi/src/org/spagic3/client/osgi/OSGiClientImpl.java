package org.spagic3.client.osgi;

import java.util.Map;

import javax.activation.DataHandler;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.spagic3.client.api.Client;
import org.spagic3.client.api.ClientMessage;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.ISpagicService;

public class OSGiClientImpl implements Client {

	private BundleContext context = null;
	
	public OSGiClientImpl(BundleContext context){
		this.context = context;
	}
	
	public void fireAndForget(String spagicServiceId, ClientMessage message) {
			
	}
	
	public ClientMessage invokeAndWait(String spagicServiceId,
			ClientMessage message) {
		
		ServiceReference[] refs = null;
		try {

			refs = context.getServiceReferences(ISpagicService.class.getName(),
					"(" + SpagicConstants.SPAGIC_ID_PROPERTY + " = "
							+ spagicServiceId + ")");
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		if (refs == null || (refs.length == 0))
			throw new IllegalStateException("Service with spagic.id ["
					+ spagicServiceId + "] not found");
		if (refs != null || (refs.length > 1))
			throw new IllegalStateException(
					"Founded more than one service with the same spagicId ["
							+ spagicServiceId + "]");

		ISpagicService service = null;

		if (refs[0] != null) {
			service = (ISpagicService) context.getService(refs[0]);

			if (service != null) {
				try {
					Exchange inOutExchange = createSyncInOutExchangeFromClientMessage(message);
					service.process(inOutExchange);
					return createClientMessageFromExchange(inOutExchange);
				} finally {
					context.ungetService(refs[0]);
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

	public void fillMessageFromClientMessage(Message msg, ClientMessage clientMessage){
		msg.setBody(clientMessage.getBody());
		
		Map<String, String> clientMessageProperties = clientMessage.getProperties();
		for (String key : clientMessageProperties.keySet()){
			msg.setHeader(key, clientMessageProperties.get(key));
		}
		
		Map<String, DataHandler> clientMessageAttachments = clientMessage.getAttachments();
		for (String key : clientMessageProperties.keySet()){
			msg.addAttachment(key, clientMessageAttachments.get(key));
		}
	}
	
	
	public ClientMessage fillClientMessageFromMessage(ClientMessage clientMessage, Message from ){
		
		clientMessage.setBody((String)from.getBody());
		
		Map<String, Object> clientMessageProperties = from.getHeaders();
		for (String key : clientMessageProperties.keySet()){
			clientMessage.setProperty(key, from.getHeader(key).toString());
		}
		
		Map<String, Object> outAtt = from.getAttachments();
		
		Object att = null;
		for (String key : outAtt.keySet()){
			att = outAtt.get(key);
			if ( att instanceof DataHandler){
				clientMessage.setAttachment(key, (DataHandler)att);
			}
		}
		return clientMessage;
	}
	
	public Exchange createSyncInOutExchangeFromClientMessage(ClientMessage clientMessage){
		Exchange exchange= ExchangeUtils.createExchange("OSGiClientSyncInvoker", Pattern.InOut);
		
		exchange.setProperty(SpagicConstants.SYNC_EXCHANGE, "true");
		Message in = exchange.getIn(true);
		fillMessageFromClientMessage(in, clientMessage);
		exchange.setIn(in);
		
		return exchange;
	}
	
	public ClientMessage createClientMessageFromExchange(Exchange exchange){
		
		Message out = exchange.getOut(false);
		
		if (out == null)
			throw new IllegalStateException("Null Response From Service");
		
		ClientMessage response = new ClientMessage(exchange.getId());
		return fillClientMessageFromMessage(response, out);
	}
}
