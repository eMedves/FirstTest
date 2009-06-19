package org.spagic3.components.consoleout;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.ExchangeUtils;


public class ConsoleOutComponent extends BaseSpagicService {

	public void process(Exchange exchange){
	  // Skip done exchanges
      if (exchange.getStatus() == Status.Done) {
          return;
      // Handle error exchanges
      } else if (exchange.getStatus() == Status.Error) {
          return;
      }
      if (ExchangeUtils.isInAndOut(exchange)) {
      		fail(exchange, new UnsupportedOperationException("Use an InOnly or RobustInOnly MEP"));
      } else {
    	  Message in = exchange.getIn(false);
    	  String msgBody = (String)in.getBody();
    	  System.out.println(msgBody);
          done(exchange);
      }
      
	}
    
	

}