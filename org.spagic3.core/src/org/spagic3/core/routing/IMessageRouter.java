package org.spagic3.core.routing;

import org.apache.servicemix.nmr.api.Exchange;

public interface IMessageRouter {

	public void send(Exchange exchange) throws Exception;
	public boolean sendSync(Exchange exchange) throws Exception;

}
