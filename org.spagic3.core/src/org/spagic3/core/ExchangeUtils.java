package org.spagic3.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.api.Status;
import org.apache.servicemix.nmr.core.ExchangeImpl;
import org.osgi.service.event.Event;
import org.spagic3.constants.SpagicConstants;

public class ExchangeUtils {
	
	 /**
     * A helper method which will return true if the exchange is capable of both In and Out such as InOut,
     * InOptionalOut etc.
     *
     * @param exchange
     * @return true if the exchange can handle both input and output
     */
    public static boolean isInAndOut(Exchange exchange) {
    	return exchange.getPattern() == Pattern.InOut || exchange.getPattern() == Pattern.InOptionalOut;
    }
    
    public static boolean isInOnly(Exchange exchange) {
    	return exchange.getPattern() == Pattern.InOnly || exchange.getPattern() == Pattern.RobustInOnly;
    }
    
    public static Exchange createExchange(Pattern pattern) {
        return new ExchangeImpl(pattern);
    }
    
    public static Exchange createExchange(String id, Pattern pattern) {
        return new ExchangeImpl(id, pattern);
    }
    
    public static Exchange createExchange(String sender, String target, Pattern pattern ){
    	Exchange e = ExchangeUtils.createExchange(pattern);
		e.setProperty(SpagicConstants.SPAGIC_SENDER, sender);
		if (target != null)
			e.setProperty(SpagicConstants.SPAGIC_TARGET, target);
		return e;
	}
    
    public static  Exchange fromEvent(Event event){
    	String mep = (String)event.getProperty(SpagicConstants.EXCHANGE_MEP);
    	String exId = (String)event.getProperty(SpagicConstants.EXCHANGE_ID);
    	String status  = (String)event.getProperty(SpagicConstants.EXCHANGE_STATUS);
    	
    	Exchange exchange = null;
    	if (mep.equalsIgnoreCase(SpagicConstants.IN_ONLY_MEP)){
    		exchange = ExchangeUtils.createExchange(exId, Pattern.InOnly);
    	}else if (mep.equalsIgnoreCase(SpagicConstants.IN_OUT_MEP)){
    		exchange = ExchangeUtils.createExchange(exId, Pattern.InOut);
    	}
    	
    	
    	String inBody = (String)event.getProperty(SpagicConstants.INBODY);
    	Message in = null;
    	Message out = null;
    	Message fault = null;
    	if (inBody != null){
    		in = exchange.getIn();
    		in.setBody(inBody);
    	}
    	String outBody = (String)event.getProperty(SpagicConstants.OUTBODY);
    	if (outBody != null){
    		out = exchange.getOut();
    		out.setBody(outBody);
    	}
    	String faultBody = (String)event.getProperty(SpagicConstants.FAULTBODY);
    	if (faultBody != null){
    		fault = exchange.getFault();
    		fault.setBody(faultBody);
    	}
    	
    	String[] pNames = event.getPropertyNames();
    	
    	String realPropertyName = null;
		for (int i = 0; i < pNames.length; i++) {
			if (pNames[i].startsWith(SpagicConstants.IN_PROPERTY)) {
				realPropertyName = pNames[i].substring(pNames[i].lastIndexOf(".") + 1);
				if (in != null)
					in.setHeader(realPropertyName, event.getProperty(pNames[i]));
			} else if (pNames[i].startsWith(SpagicConstants.OUT_PROPERTY)) {
				realPropertyName = pNames[i].substring(pNames[i].lastIndexOf(".") + 1 );
				if (out != null)
					out.setHeader(realPropertyName, event.getProperty(pNames[i]) );
			} else if (pNames[i].startsWith(SpagicConstants.FAULT_PROPERTY)) {
				realPropertyName = pNames[i].substring(pNames[i].lastIndexOf(".") + 1);
				if (fault != null)
					fault.setHeader(realPropertyName, event.getProperty(pNames[i]));
			} else if (pNames[i].startsWith(SpagicConstants.EXCHANGE_PROPERTY)) {
				realPropertyName = pNames[i].substring(pNames[i].indexOf(".") + 1);
				
				if (realPropertyName.startsWith(SpagicConstants.WF_VARIABLE_PREFIX))
					System.out.println(realPropertyName);
				else
					realPropertyName = pNames[i].substring(pNames[i].lastIndexOf(".") + 1 );
				if (exchange != null)
					exchange.setProperty(realPropertyName, event.getProperty(pNames[i]));
			}

		}
    	if (in != null)
    		exchange.setIn(in);
    	if (out != null)
    		exchange.setOut(out);
    	if (fault != null)
    		exchange.setFault(fault);
    	
    	if (status.equalsIgnoreCase(SpagicConstants.STATUS_ACTIVE)){
    		exchange.setStatus(Status.Active);
    	}if (status.equalsIgnoreCase(SpagicConstants.STATUS_DONE)){
    		exchange.setStatus(Status.Done);
    	}if (status.equalsIgnoreCase(SpagicConstants.STATUS_ERROR)){
    		exchange.setStatus(Status.Error);
    	}
    
    	return exchange;
    }
    
    public static Event toEvent(Exchange exchange){
    	String sender = (String)exchange.getProperties().get(SpagicConstants.SPAGIC_SENDER);
    	String target = (String)exchange.getProperties().get(SpagicConstants.SPAGIC_TARGET);
		String eventTopic = SpagicUtils.normalizeTopic(SpagicConstants.SPAGIC_BASE_TOPIC + target);
		
		Map properties = new HashMap();
		
		
		if (exchange.getPattern() == Pattern.InOnly)
			properties.put(SpagicConstants.EXCHANGE_MEP, SpagicConstants.IN_ONLY_MEP);
		else if ( exchange.getPattern() == Pattern.InOut){
			properties.put(SpagicConstants.EXCHANGE_MEP, SpagicConstants.IN_OUT_MEP);
		}
		properties.put(SpagicConstants.EXCHANGE_ID, exchange.getId());
		
		if (exchange.getStatus() == Status.Active){
			properties.put(SpagicConstants.EXCHANGE_STATUS, SpagicConstants.STATUS_ACTIVE);
		}else if (exchange.getStatus() == Status.Done){
			properties.put(SpagicConstants.EXCHANGE_STATUS, SpagicConstants.STATUS_DONE);
		}else if (exchange.getStatus() == Status.Error){
			properties.put(SpagicConstants.EXCHANGE_STATUS, SpagicConstants.STATUS_ERROR);
		}
		
		// Save exchangeProperties
		
		Map<String, Object> exchangeProperties = exchange.getProperties();
		
		String destPropertyName = null;
		
		for (String exProp : exchangeProperties.keySet()){
			destPropertyName = SpagicConstants.EXCHANGE_PROPERTY+"."+exProp;
			properties.put(destPropertyName, exchangeProperties.get(exProp));
		}
		
		Message in = exchange.getIn(false);
		if (in != null){
			properties.put(SpagicConstants.INBODY, (String)in.getBody());
			
			Map<String, Object> inProperties = in.getHeaders();
			
			for (String inProperty : inProperties.keySet()){
				destPropertyName = SpagicConstants.IN_PROPERTY + "." + inProperty;
				properties.put(destPropertyName, inProperties.get(inProperty));
			}
		}
		
		Message out = exchange.getOut(false);
		if (out != null){
			properties.put(SpagicConstants.OUTBODY, (String)out.getBody());
			
			Map<String, Object> outProperties = out.getHeaders();
			
			for (String outProperty : outProperties.keySet()){
				destPropertyName = SpagicConstants.OUT_PROPERTY+ "." + outProperty;
				properties.put(destPropertyName, outProperties.get(outProperty));
			}
		}
		
		Message fault = exchange.getFault(false);
		if (fault != null){
			properties.put(SpagicConstants.FAULTBODY, (String)fault.getBody());
			
			Map<String, Object> faultProperties = fault.getHeaders();
			
			for (String faultProperty : faultProperties.keySet()){
				destPropertyName = SpagicConstants.FAULT_PROPERTY+ "." + faultProperty;
				properties.put(destPropertyName, faultProperties.get(faultProperty));
			}
		}
		
		
		Event ev = new Event(eventTopic, properties);
		
		
		return ev;
    }
}
