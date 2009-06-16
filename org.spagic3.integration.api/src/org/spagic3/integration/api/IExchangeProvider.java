package org.spagic3.integration.api;

import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;

public interface IExchangeProvider extends IElementWithProperties {
	
	public Exchange createExchangeFromWorkflowContext(Object workflowContext);
	
}
