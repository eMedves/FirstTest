package org.spagic3.core;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Pattern;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.routing.IMessageRouter;

public  abstract class AbstractSpagicService implements ISpagicService, EventHandler {

	private static Logger logger = LoggerFactory.getLogger(AbstractSpagicService.class);
	
	protected String spagicId = null;
	protected String target = null;
	protected PropertyConfigurator propertyConfigurator = null;
	private final AtomicReference<IMessageRouter> router = new AtomicReference<IMessageRouter>(null); 
	
	

	protected void activate(ComponentContext componentContext){
		Dictionary propertiesDictionary = componentContext.getProperties();
		this.propertyConfigurator = new PropertyConfigurator(propertiesDictionary);
		
		this.spagicId = (String)this.propertyConfigurator.getString("spagic.id");
		this.target = (String)this.propertyConfigurator.getString("target",null);
		init();
	}
	
	protected void deactivate(ComponentContext componentContext){
	
	}
	
	public void init(){
		
	}
	
	@Override
	public IMessageRouter getMessageRouter() {
		return this.router.get();
	}

	@Override
	public void unsetMessageRouter(IMessageRouter router) {
		this.router.compareAndSet(router, null);
	}
	
	@Override
	public String getSpagicId() {
		return this.spagicId;
	}

	

	@Override
	public void setMessageRouter(IMessageRouter router) {
		this.router.set(router);
		
	}

	@Override
	public void handleEvent(Event event) {
		logger.info("Service["+getSpagicId()+"] -> Received Event " + event);
		Exchange exchange = ExchangeUtils.fromEvent(event);
		logger.info("Service["+getSpagicId()+"] -> Received Exchange ["+exchange.getId()+"] ["+exchange.getStatus()+"]" );
		try{
			process(exchange);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void send(Exchange exchange){
		try{
			getMessageRouter().send(exchange);
		}catch (Throwable e) {
			logger.error("Exception sending exchange ["+exchange+"]", e);
		}
	}
	
	public void configureForResponse(Exchange exchange){
		String originalSender = (String)exchange.getProperty(SpagicConstants.SPAGIC_SENDER);
		exchange.setProperty(SpagicConstants.SPAGIC_SENDER, getSpagicId());
		exchange.setProperty(SpagicConstants.SPAGIC_TARGET, originalSender);
	}
	
	
	protected Exchange createInOnlyExchange(){
		return ExchangeUtils.createExchange(getSpagicId(), this.target, Pattern.InOnly);
	}
	
	protected Exchange createInOutExchange(){
		return ExchangeUtils.createExchange(getSpagicId(), this.target, Pattern.InOut);
	}

}
