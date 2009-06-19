package org.spagic3.core;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSpagicService extends AbstractSpagicService {
	
	protected Logger logger = LoggerFactory.getLogger(BaseSpagicService.class);
	
	private boolean copyAttachments = true;
	private boolean copyProperties = true;
	
	public boolean isCopyAttachments() {
		return copyAttachments;
	}

	public void setCopyAttachments(boolean copyAttachments) {
		this.copyAttachments = copyAttachments;
	}

	public boolean isCopyProperties() {
		return copyProperties;
	}

	public void setCopyProperties(boolean copyProperties) {
		this.copyProperties = copyProperties;
	}

	
	
	public void process(Exchange exchange){
		  // Skip done exchanges
        if (exchange.getStatus() == Status.Done) {
            return;
        // Handle error exchanges
        } else if (exchange.getStatus() == Status.Error) {
            return;
        }
        try {
            Exchange outExchange = null;
            Message in = exchange.getIn();
            Message out;
            if (ExchangeUtils.isInAndOut(exchange)) {
                out = exchange.getOut(true);
            } else {
                outExchange = createInOnlyExchange();
                outExchange.setProperty(SpagicConstants.SPAGIC_SENDER, getSpagicId());
                String processCorrelationId = (String)exchange.getProperty(SpagicConstants.CORRELATION_ID);
                if (processCorrelationId != null) {
                    outExchange.setProperty(SpagicConstants.CORRELATION_ID, processCorrelationId);
                }
                out = exchange.getOut(true);
            }
            
            copyPropertiesAndAttachments(exchange, in, out);
            
            if (run(exchange, in, out)) {
                if (ExchangeUtils.isInAndOut(exchange)) {
                	
                	configureForResponse(exchange);
                	exchange.setOut(out);
                    send(exchange);
                } else {
                	// *******************************************************
                    // 1 - Send the Out Exchange
                	// *******************************************************
                	outExchange.setIn(out);
                    send(outExchange);
                    
                    // *******************************************************
                    // 2 - Send Back the original Exchange with status DONE
                	// *******************************************************
                    done(exchange);
                }
            } else {
                done(exchange);
            }
        } catch (Exception e) {
                fail(exchange, e);
        }
	}
	
	
	
	 
	 protected void copyPropertiesAndAttachments(Exchange exchange, Message in, Message out) throws Exception {
		 if (isCopyProperties()) {
			
		 }
		 if (isCopyAttachments()) {

		 }
	 }
	 
	 public boolean run(Exchange exchange, Message in, Message out) throws Exception{
		 return false;
	 }
	
}
