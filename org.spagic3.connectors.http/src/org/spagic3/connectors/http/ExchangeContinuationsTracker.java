package org.spagic3.connectors.http;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.mortbay.util.ajax.Continuation;
import org.spagic3.constants.SpagicConstants;

public class ExchangeContinuationsTracker {

	private HashMap<String, Exchange> pendingExchanges = new HashMap<String, Exchange>();
    private HashMap<String, Continuation> continuationsForExchange = new HashMap<String, Continuation>();
    
	public void handle(HttpServletRequest request, HttpServletResponse response, Continuation continuation, long timeoutInMillisecond,  HTTPServer spagicEndpoint ) throws Exception {
			Exchange receivedExchange = null;  
			synchronized(this) {
			    if (!continuation.isPending()) {
			    	Exchange exchange = spagicEndpoint.createExchange(request);
			    	request.setAttribute(SpagicConstants.EXCHANGE_ID, exchange.getId());
			    	pendingExchanges.put(exchange.getId(), exchange);
			    	continuationsForExchange.put(exchange.getId(), continuation);
			   
			    	spagicEndpoint.getMessageRouter().send(exchange);
			    	continuation.suspend(timeoutInMillisecond);
			  	} else {
			  		//  * the continuation has been resumed because the exchange has been received
		            //  * the continuation has timed out
			  		// Get the exchange id from the request
			  				String id = (String) request.getAttribute(SpagicConstants.EXCHANGE_ID);
		                    // Remove the continuation from the map, indicating it has been processed or timed out
		                    continuationsForExchange.remove(id);
		                    receivedExchange = pendingExchanges.remove(id);
		                    request.removeAttribute(SpagicConstants.EXCHANGE_ID);
		                    // Check if this is a timeout
		                    if (receivedExchange == null) {
		                        throw new IllegalStateException("Exchange not found");
		                    }
		                    if (!continuation.isResumed()) {
		                        throw new Exception("Exchange timed out: " + receivedExchange.getId());
		                    }
			  	}
			  }
			// At this point, we have received the exchange response,
	        // so process it and send back the HTTP response
	        if (receivedExchange != null && receivedExchange.getStatus() == Status.Error) {
	                Exception e = receivedExchange.getError();
	                if (e == null) {
	                    e = new Exception("Unkown error (exchange aborted ?)");
	                }
	                throw e;
	        } else if (receivedExchange.getStatus() == Status.Active) {
	                try {
	                    Message fault = receivedExchange.getFault(false);
	                    if (fault != null) {
	                        spagicEndpoint.responseFault(receivedExchange, fault, request, response);
	                    } else {
	                       Message out = receivedExchange.getOut(false);
	                       if (out != null) {
	                            spagicEndpoint.responseOut(receivedExchange, out, request, response);
	                        }
	                    }
	                    // Resend the done
	                    String originalSender = (String)receivedExchange.getProperty(SpagicConstants.SPAGIC_SENDER);
	                    receivedExchange.setProperty(SpagicConstants.SPAGIC_SENDER, spagicEndpoint.getSpagicId());
	                    receivedExchange.setProperty(SpagicConstants.SPAGIC_TARGET, originalSender);
	                    receivedExchange.setStatus(Status.Done);
	                    spagicEndpoint.getMessageRouter().send(receivedExchange);
	                    
	                } catch (Exception e) {
	                  
	                    throw e;
	                }
	         } else if (receivedExchange.getStatus() == Status.Done) {
	                // This happens when there is no response to send back
	                spagicEndpoint.responseAccepted(receivedExchange, request, response);
	         }
			  
			  
	}
	
	public void exchangeArrived(Exchange exchange) {

		synchronized (this) {
			Continuation continuationForExchange = continuationsForExchange
					.get(exchange.getId());
			if (continuationForExchange == null)
				throw new IllegalStateException("HTTP request has timed out for exchange: "
						+ exchange.getId());

			synchronized (continuationForExchange) {
				if (continuationsForExchange.remove(exchange.getId()) == null) {
					throw new IllegalStateException(
							"HTTP request has timed out for exchange: "
									+ exchange.getId());
				}

				pendingExchanges.put(exchange.getId(), exchange);
				continuationForExchange.resume();

				if (!continuationForExchange.isResumed()) {
					throw new IllegalStateException(
							"Cannot Resume Continuation for Exchange "
									+ exchange.getId());
				}

			}
		}

	}
}
