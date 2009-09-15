package org.spagic3.components.bpm;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.workflow.api.IControlAPI;
import org.spagic.workflow.api.IProcessEngine;
import org.spagic.workflow.api.IQueryAPI;
import org.spagic.workflow.api.Variable;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.ExchangeUtils;


public class BPMComponent extends BaseSpagicService  {
	
	public String process = null;
	public static ConcurrentHashMap<Long, Exchange> exchangeMap = new ConcurrentHashMap<Long, Exchange>();

	protected Logger logger = LoggerFactory.getLogger(BPMComponent.class);
	
	private final AtomicReference<IProcessEngine> processEngine = new AtomicReference<IProcessEngine>(null);
	private EventAdmin ea = null;
	
	public IProcessEngine getProcessEngine() {
		return processEngine.get();
	}

	public void unsetProcessEngine(IProcessEngine processEngine) {
		this.processEngine.compareAndSet(processEngine, null);
	}
	
	public void setProcessEngine(IProcessEngine processEngine) {
		this.processEngine.set(processEngine);
	}

	public void bind(EventAdmin ea){
		this.ea = ea;
	}

	public void unbind(EventAdmin ea){
		this.ea = null;
	}
	
	public void init(){
		try{
			this.process = propertyConfigurator.getString("process");
			BPMContextSingleton.getInstance().register(this);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(" Cannot instantiate BPM Component", e);
		}
	}
	
	public void beforeDestroy(){
		BPMContextSingleton.getInstance().unregister(this);
	}
	
    
	public void process(Exchange exchange){
	   
		if (exchange.getStatus() == Status.Active) {
			if (ExchangeUtils.isInOnly(exchange)) {
				done(exchange);
			}

			IControlAPI controlAPI = getProcessEngine().getControlAPI();
			IQueryAPI queryAPI = getProcessEngine().getQueryAPI();

			
			String exchangeID = exchange.getId();
			String xmlMessage = (String) exchange.getIn(false).getBody();

			Variable[] var = null;

			if (ExchangeUtils.isSync(exchange)) {
				var = new Variable[3];
			} else {
				var = new Variable[2];
			}

			var[0] = new Variable();
			var[0].setName(BPMContextSingleton.XML_MESSAGE);
			var[0].setValue(xmlMessage);

			var[1] = new Variable();
			var[1].setName(BPMContextSingleton.ORCHESTRATION_SERVICE_ID);
			var[1].setValue(this.getSpagicId());

			if (ExchangeUtils.isInOnly(exchange)) {
				// If exchange is InOnly means fire and forget send back the
				// done and execute the process
				done(exchange);
				long pid = controlAPI.startByProcessName(process, var);
				
				// Send notification for monitoring
				notify(exchangeID, SpagicConstants._INTERNAL_EVENT_PROCESS_STARTED, pid);
				
				// Store exchange for managing the process response
				exchangeMap.put(pid, exchange);
			} else if (ExchangeUtils.isInAndOut(exchange)) {
				if (ExchangeUtils.isSync(exchange)) {
					var[2] = new Variable();
					var[2].setName(BPMContextSingleton.REQUIRE_BPM_SYNC_EXECUTION);
					var[2].setValue("TRUE");
					long pid = controlAPI.startByProcessName(process, var);
					
					// Send notification for monitoring
					notify(exchangeID, SpagicConstants._INTERNAL_EVENT_PROCESS_STARTED, pid);
					
					Variable varOut = controlAPI.getGlobalVariable(pid,
							BPMContextSingleton.XML_MESSAGE);
					exchange.getOut(true).setBody(varOut.getValue());
				} else {
					long pid = controlAPI.startByProcessName(process, var);
					// Send notification for monitoring
					notify(exchangeID, SpagicConstants._INTERNAL_EVENT_PROCESS_STARTED, pid);
					
					exchangeMap.put(pid, exchange);
				}

			}

		}
	}
	
	public void notify(String id, String eventType, long processId) {
		
		Map<String, Object> internalEventProperties = new HashMap<String, Object>();
		internalEventProperties.put(SpagicConstants._IS_INTERNAL_EVENT, true);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_TYPE, eventType);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_REFERRING_ID, id);
		internalEventProperties.put(SpagicConstants._INTERNAL_EVENT_PROCESS_ID, processId);
		Event ev = new Event(SpagicConstants.SPAGIC_GENERIC_TOPIC, internalEventProperties);
		if (this.ea != null){
			ea.postEvent(ev);
		}
	}
	
	public void callBack(Long pid,  String xmlMessage, boolean isProcessTerminated){

		Exchange exchange = exchangeMap.remove(pid);
		
		if (exchange != null){
			if (ExchangeUtils.isInOnly(exchange)){
				
				if (!isProcessTerminated){
					Exchange newExchange = createInOnlyExchange();
					Message newIn = exchange.getIn(true);
					
					newExchange.setProperty(SpagicConstants.CORRELATION_ID, exchange.getProperty(SpagicConstants.CORRELATION_ID));
					newIn.setBody(xmlMessage);
					newExchange.setIn(newIn);
					send(newExchange);
				}else{
					// The process is terminated by one of the component in the workflow
					// process this means we stop here
				}
			}else if (ExchangeUtils.isInAndOut(exchange)){
				configureForResponse(exchange);
				Message out = exchange.getOut(true);
				out.setBody(xmlMessage);
				exchange.setOut(out);
				send(exchange);
			}
		}
	}

	
	
	
	
}
