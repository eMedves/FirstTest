package org.spagic3.client.api;

import java.util.Map;

public interface Client {

	/**
	 * Within this method a client invoke a spagic service and forget about it
	 * 
	 * In Spagic3 embedded mode this is useful when a webapplication want invoke a 
	 * spagic service ( that could be an orchestration service ) that will terminate with
	 * an outbound connector
	 * 
	 * @param spagicServiceId - The spagic service ID to invoke
	 * @param message - The client message
	 */
	public void fireAndForget(String spagicServiceId, ClientMessage message);
	
	
	/**
	 * Within this method a client invoke a spagic service and wait until
	 * a response is received 
	 *
	 * You could invoke ( Simple Spagic Service )
	 * 					( Orchestration Service )
	 * 
	 * @param spagicServiceId - The spagic service ID to invoke
	 * @param message - The client message
	 */
	public ClientMessage invokeAndWait(String spagicServiceId, ClientMessage message);
	
	
}
