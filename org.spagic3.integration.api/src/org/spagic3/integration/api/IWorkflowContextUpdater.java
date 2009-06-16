package org.spagic3.integration.api;

import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;

public interface IWorkflowContextUpdater extends IElementWithProperties {
	
	public void updateWorkflowContext(Object workflowContex, Exchange exchange);
	
}
