package org.spagic3.connectors.http.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.soap.api.InterceptorChain;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.api.InterceptorProvider.Phase;
import org.apache.servicemix.soap.api.model.Binding;
import org.apache.servicemix.soap.interceptors.xml.StaxInInterceptor;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpMethods;
import org.spagic3.connectors.http.SpagicJettyHTTPExchange;

public class SOAPProtocolOutputAdapter implements IHTTPOutputProtocolAdapter {
	
	private Binding<?> binding;
	private Map<Phase, InterceptorChain> chains = new HashMap<Phase, InterceptorChain>();
	private String locationURI;
	
	

	public String getLocationURI() {
		return locationURI;
	}

	public void setLocationURI(String locationUri) {
		locationURI = locationUri;
	}

	public Binding<?> getBinding() {
		return binding;
	}

	public void setBinding(Binding<?> binding) {
		this.binding = binding;
	}

	@Override
	public void fillJettyExchange(Exchange nmrExchange,
			SpagicJettyHTTPExchange jettyExchange) {
		  	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        Message soapMessage = binding.createMessage();
	      
	        soapMessage.setContent(Exchange.class, nmrExchange);
	        soapMessage.setContent(org.apache.servicemix.nmr.api.Message.class, nmrExchange.getIn(false));
	        soapMessage.setContent(OutputStream.class, baos);
	        nmrExchange.setProperty(Message.class.getName(), soapMessage);

	        InterceptorChain phaseOut = getChain(Phase.ClientOut);
	        phaseOut.doIntercept(soapMessage);
	        jettyExchange.setMethod(HttpMethods.POST);
	        jettyExchange.setURL(locationURI);
	        jettyExchange.setRequestContent(new ByteArrayBuffer(baos.toByteArray()));
	        for (Map.Entry<String,String> entry : soapMessage.getTransportHeaders().entrySet()) {
	        	jettyExchange.addRequestHeader(entry.getKey(), entry.getValue());
	        }
		
	}

	@Override
	public void handleException(Exchange nmrExchange,
			SpagicJettyHTTPExchange jettyExchange, Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleResponse(Exchange nmrExchange,
			SpagicJettyHTTPExchange jettyExchange) {
			try{
				Message soapMessageRequest = (Message) nmrExchange.getProperty(Message.class.getName());
				nmrExchange.setProperty(Message.class.getName(), null);
				Message soapMessageResponse = binding.createMessage(soapMessageRequest);
	       
				soapMessageResponse.setContent(Exchange.class, nmrExchange);
				soapMessageResponse.setContent(InputStream.class, new ByteArrayInputStream(jettyExchange.getResponse()));
				soapMessageResponse.put(StaxInInterceptor.ENCODING, "utf-8");
				InterceptorChain phaseOut = getChain(Phase.ClientIn);
				phaseOut.doIntercept(soapMessageResponse);
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
		
	}
	
	 protected InterceptorChain getChain(Phase phase) {
	        InterceptorChain chain = chains.get(phase);
	        if (chain == null) {
	            chain = binding.getInterceptorChain(phase);
	            
	            chains.put(phase, chain);
	        }
	        return chain;
	    }
}
