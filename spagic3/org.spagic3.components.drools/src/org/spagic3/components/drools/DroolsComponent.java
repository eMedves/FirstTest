package org.spagic3.components.drools;


import java.util.HashMap;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.components.drools.handler.CustomHandlerWorkItem;
import org.spagic3.components.drools.invoker.IServiceInvoker;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.resources.IResource;
import org.spagic3.exchanges.DroolsExchangeStore;


public class DroolsComponent extends BaseSpagicService {
		
	public String flowId = null;
	public IResource rulesFile = null;
	protected Logger logger = LoggerFactory.getLogger(DroolsComponent.class);
	
	
	private EventAdmin ea = null;
	private IServiceInvoker serviceInvoker;

	private KnowledgeBase kbase;
	
	public void bind(EventAdmin ea) {
		this.ea = ea;
	}

	public void unbind(EventAdmin ea) {
		this.ea = null;
	}

	public void setServiceInvoker(IServiceInvoker serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
	}

	public void unsetServiceInvoker(IServiceInvoker serviceInvoker) {
		this.serviceInvoker = null;
	}

	public void init() {
		try{
			this.flowId = propertyConfigurator.getString("flowId");
			logger.debug("flowId = " + flowId);
			this.rulesFile = propertyConfigurator.getResource("rulesFile");
			logger.debug("rulesFile = " + rulesFile);
			this.kbase = readKnowledgeBase(rulesFile);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(" Cannot instantiate Drools Component", e);
		}
	}
	
	private KnowledgeBase readKnowledgeBase(IResource rulesFile) throws Exception {
		logger.debug("reading knowledge base");
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newUrlResource(rulesFile.asURL()), ResourceType.DRF);
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error: errors) {
				logger.error(error.getMessage());
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}
	
	public void beforeDestroy() {}
    
	public void process(Exchange exchange) {

		logger.debug("processing exchange " + exchange.getId());
		logger.debug("original sender = " + 
				(String) exchange.getProperty(SpagicConstants.SPAGIC_SENDER));
		logger.debug("original target = " + 
				(String) exchange.getProperty(SpagicConstants.SPAGIC_TARGET));
	   
		if (exchange.getStatus() == Status.Active) {

			logger.debug("exchange is active");

			if (ExchangeUtils.isInOnly(exchange)) {
				logger.debug("exchange is in only: calling done");
				done(exchange);
			}

			logger.debug("opening stateful knowledge base session");
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
			
			logger.debug("registering custom handler");
			CustomHandlerWorkItem customHandler =
					new CustomHandlerWorkItem(serviceInvoker);
			ksession.getWorkItemManager().registerWorkItemHandler(
					"CustomHandler", customHandler);
			
			String exchangeID = exchange.getId();
			String xmlInMessage = (String) exchange.getIn(false).getBody();

			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put(DroolsContext.XML_MESSAGE, xmlInMessage);
			variables.put(DroolsContext.ORCHESTRATION_SERVICE_ID, this.getSpagicId());
			variables.put(DroolsContext.ORCHESTRATION_EXCHANGE_ID, exchangeID);
			
			if (ExchangeUtils.isInOnly(exchange)) {
				// If exchange is InOnly means fire and forget send back the
				// done and execute the process
//				done(exchange);
				
				logger.debug("registering process listener");
				ksession.addEventListener(new DroolsRuleflowCompletionListener(this));

				// Store exchange for managing the process response
				DroolsExchangeStore.store(exchangeID, exchange);

				// start a new process instance
				logger.debug("starting process (exchange in only)");
				ksession.startProcess(flowId, variables);
				
				// Send notification for monitoring
//				notify(exchangeID, SpagicConstants._INTERNAL_EVENT_DROOLS_STARTED, pid);
				
			} else if (ExchangeUtils.isInAndOut(exchange)) {
				if (ExchangeUtils.isSync(exchange)) {
					logger.debug("starting process (exchange in out sync)");
					variables.put(DroolsContext.REQUIRE_DROOLS_SYNC_EXECUTION, "TRUE");
					ProcessInstance processInstance
							= ksession.startProcess(flowId, variables);
					
					// Send notification for monitoring
//					notify(exchangeID, SpagicConstants._INTERNAL_EVENT_DROOLS_STARTED, pid);
					
					String xmlOutMessage = (String) ((WorkflowProcessInstance) 
							processInstance).getVariable(DroolsContext.XML_MESSAGE);
					
					exchange.getOut(true).setBody(xmlOutMessage);
				} else {
					logger.debug("registering process listener");
					ksession.addEventListener(new DroolsRuleflowCompletionListener(this));

					// Store exchange for managing the process response
					DroolsExchangeStore.store(exchangeID, exchange);

					// start a new process instance
					logger.debug("starting process (exchange in out async)");
					ksession.startProcess(flowId, variables);

					// Send notification for monitoring
//					notify(exchangeID, SpagicConstants._INTERNAL_EVENT_DROOLS_STARTED, pid);
				}
			}
		}
	}
	
	public void notify(String id, String eventType, long processId) {
		
		Map<String, Object> internalEventProperties = new HashMap<String, Object>();
		internalEventProperties.put(SpagicConstants._IS_INTERNAL_EVENT, true);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_TYPE, eventType);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_REFERRING_ID, id);
//		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_DROOLS_ID, processId);
		Event ev = new Event(SpagicConstants.SPAGIC_GENERIC_TOPIC, internalEventProperties);
		if (this.ea != null) {
			ea.postEvent(ev);
		}
	}
	
	public void callBack(String exchangeId, String xmlMessage, boolean isProcessAborted){

		Exchange exchange = DroolsExchangeStore.delete(exchangeId);

		if (exchange != null){
			logger.debug("original sender = " + 
					(String) exchange.getProperty(SpagicConstants.SPAGIC_SENDER));
			logger.debug("original target = " + 
					(String) exchange.getProperty(SpagicConstants.SPAGIC_TARGET));

			if (ExchangeUtils.isInOnly(exchange)){
				logger.debug("exchange is in only");
				if (!isProcessAborted) {
					logger.debug("process is not aborted");
					Exchange newExchange = createInOnlyExchange();
					Message newIn = exchange.getIn(true);
					
					newExchange.setProperty(SpagicConstants.CORRELATION_ID, exchange.getProperty(SpagicConstants.CORRELATION_ID));
					newIn.setBody(xmlMessage);
					newExchange.setIn(newIn);
					send(newExchange);
				} else {
					logger.debug("process is aborted");
					// The process is terminated by one of the component in the workflow
					// process this means we stop here
				}
			} else if (ExchangeUtils.isInAndOut(exchange)) {
				logger.debug("exchange is in out");
				logger.debug("configuring exchange for response");
				configureForResponse(exchange);
				Message out = exchange.getOut(true);
				out.setBody(xmlMessage);
				exchange.setOut(out);
				send(exchange);
			}
		} else {
			// The exchange could be null because the process was started manually
			// In that case we must send an InOnly Exchange only if the component has a target
			
			if ((!isProcessAborted ) && (this.target != null)) {
				Exchange newExchange = createInOnlyExchange();
				Message newIn = newExchange.getIn(true);
				
				newIn.setBody(xmlMessage);
				newExchange.setIn(newIn);
				send(newExchange);
			}
		}
	}

	private class DroolsRuleflowCompletionListener extends DefaultProcessEventListener {
		
		DroolsComponent component;
		
		public DroolsRuleflowCompletionListener(DroolsComponent component) {
			this.component = component;
		}

		public void afterProcessCompleted(ProcessCompletedEvent event) {

			logger.debug("process completed");

			ProcessInstance p = event.getProcessInstance();
			String exchangeId = (String) ((WorkflowProcessInstance) p)
					.getVariable(DroolsContext.ORCHESTRATION_EXCHANGE_ID);
			String xmlOutMessage = (String) ((WorkflowProcessInstance) p)
					.getVariable(DroolsContext.XML_MESSAGE);
			boolean isProcessAborted = p.getState() == ProcessInstance.STATE_ABORTED;

			logger.debug("calling back component with exchangeId = " + exchangeId);
			
			component.callBack(exchangeId, xmlOutMessage, isProcessAborted);
		}
	}	
	
}
