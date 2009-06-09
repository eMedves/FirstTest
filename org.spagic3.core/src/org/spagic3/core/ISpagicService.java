package org.spagic3.core;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic3.core.routing.IMessageRouter;


public interface ISpagicService {

	public String getSpagicId();
	
	/**
	 * This is the method that is invoked when the service
	 * received an Exchange from the NMR
	 * 
	 * @param Exchange exchange
	 * @throws Exception
	 */
	public void process(Exchange exchange) throws Exception; 
	
	/**
	 * Methods to bind with the Message Router Service
	 * @param router
	 */
	public void setMessageRouter(IMessageRouter router);
	
	public void unsetMessageRouter(IMessageRouter router);
	
	public IMessageRouter getMessageRouter();
	
	public void init();
	

}
