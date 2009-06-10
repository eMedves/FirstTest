package org.spagic3.components.bpm;


import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.jbpm.JbpmContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.metadb.model.ProcessInstance;
import org.spagic.workflow.api.IControlAPI;
import org.spagic.workflow.api.IProcessEngine;
import org.spagic.workflow.api.IQueryAPI;
import org.spagic.workflow.api.Variable;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.SpagicConstants;
import org.spagic3.core.SpagicUtils;
import org.spagic3.core.routing.IMessageRouter;


public class BPMComponent extends BaseSpagicService  {
	
	
	
	public String process = null;
	protected Logger logger = LoggerFactory.getLogger(BPMComponent.class);
	
	private final AtomicReference<IProcessEngine> processEngine = new AtomicReference<IProcessEngine>(null);
	public static ConcurrentHashMap<Long, Exchange> exchangeMap = new ConcurrentHashMap<Long, Exchange>();
	
	
	public IProcessEngine getProcessEngine() {
		return processEngine.get();
	}

	public void unsetProcessEngine(IProcessEngine processEngine) {
		this.processEngine.compareAndSet(processEngine, null);
	}
	
	public void setProcessEngine(IProcessEngine processEngine) {
		this.processEngine.set(processEngine);
	}

	
	
	
	public void init(){
		try{
			String process = propertyConfigurator.getString("script");
			
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(" Cannot instantiate BPM Component", e);
		}
	}
	
	
    
	public void process(Exchange exchange){
	
	  if ( exchange.getStatus() == Status.Active){
		  IControlAPI controlAPI = getProcessEngine().getControlAPI();
		  IQueryAPI queryAPI = getProcessEngine().getQueryAPI();
	  
		  String xmlMessage = (String)exchange.getIn(false).getBody();
		 
	  	Variable[] var = new Variable[3];
	  	var[0] = new Variable();
	  	var[0].setName(BPMContextSingleton.XML_MESSAGE);
	  	var[0].setValue(xmlMessage);
		 
		 
	  	var[1] = new Variable();
	  	var[1].setName(BPMContextSingleton.ORCHESTRATION_SERVICE_ID);
	  	var[1].setValue(this.getSpagicId());

	  	long pid =  controlAPI.startByProcessName(process, var);
	  }
	}

	
	public void callBack(Long pid,  String xmlMessage){

		Exchange exchange = exchangeMap.get(pid);
		
		if (exchange != null){
			if (ExchangeUtils.isInOnly(exchange)){
				configureForResponse(exchange);
				exchange.setStatus(Status.Done);
				send(exchange);
			}else if (ExchangeUtils.isInAndOut(exchange)){
				configureForResponse(exchange);
				Message out = exchange.getOut(true);
				out.setBody(xmlMessage);
				exchange.setOut(out);
				send(exchange);
			}
		}
	}

	@Override
	public boolean run(Exchange exchange, Message in, Message out)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
}
