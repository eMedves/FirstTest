package org.spagic3.integration.api;

import org.apache.servicemix.nmr.api.Exchange;

/**
 * 
 * @author zoppello
 * This interface is the main interface to be used by the orchestration
 * engine to call message style service
 */
public interface IServiceRunner {
	
	public void run(String serviceID, Exchange exchange);

}
