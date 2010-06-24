package org.spagic3.components.drools.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Pattern;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.spagic3.components.drools.invoker.IServiceInvoker;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;

public class CustomHandlerWorkItem implements WorkItemHandler {
	
	IServiceInvoker serviceInvoker;
	
	public CustomHandlerWorkItem(IServiceInvoker serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

		String serviceId = (String) workItem
				.getParameter("Service Id");
		String serviceType = (String) workItem
				.getParameter("Service Type");
		String xmlInMessage = (String) workItem
				.getParameter("inMessage");
		
		try {
			
			Exchange exchange = ExchangeUtils.createExchange(Pattern.InOut);
			exchange.setProperty(SpagicConstants.SYNC_EXCHANGE, "true");
			exchange.getIn(true).setBody(xmlInMessage);
			
			serviceInvoker.invokeService(serviceId, exchange);
			
			String xmlOutMessage = (String) exchange.getOut().getBody();
			Map<String, Object> results = new HashMap<String, Object>();
			results.put("outMessage", xmlOutMessage);
			
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Exception e) {
			manager.abortWorkItem(workItem.getId());
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// nothing to do
	}

}
