package org.spagic3.connectors.http.adapters.omar;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.ebpm.connectors.http.adapters.SOAPProtocolOutputAdapter;
import org.eclipse.ebpm.connectors.http.adapters.SpagicJettyHTTPExchange;
import org.eclipse.ebpm.messaging.api.Exchange;
import org.eclipse.ebpm.soap.api.InterceptorChain;
import org.eclipse.ebpm.soap.api.Message;
import org.eclipse.ebpm.soap.api.InterceptorProvider.Phase;
import org.eclipse.ebpm.soap.api.model.Binding;
import org.eclipse.ebpm.soap.bindings.soap.SoapConstants;
import org.eclipse.ebpm.util.properties.PropertyConfigurator;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpMethods;

public class OmarSOAPOutputAdapter extends SOAPProtocolOutputAdapter {

	// org.spagic3.connectors.http.adapters.omar.OmarSOAPOutputAdapter
	public String getAdapterId() {
		return "OMAR";
	}
	
	@Override
	public void fillJettyExchange(Exchange nmrExchange,
			SpagicJettyHTTPExchange jettyExchange, PropertyConfigurator pc, Object binding) {
		  	
		 	Binding<?> soapBinding = (Binding<?>)binding;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        Message soapMessage = soapBinding.createMessage();
	      
	        boolean mustSetOperationHeader = pc.getBoolean("mustSetOperation", false);
	    
	        
	        String operationName = null;
	        if (mustSetOperationHeader){
	        	operationName = (String) pc.getString("operation");
	        	nmrExchange.setProperty("mustSetOperation", mustSetOperationHeader);
	        	nmrExchange.setProperty(SoapConstants.SOAP_ACTION_HEADER, operationName);
	        }
	       
	        soapMessage.setContent(Exchange.class, nmrExchange);
	        soapMessage.setContent(org.eclipse.ebpm.messaging.api.Message.class, nmrExchange.getIn(false));
	        soapMessage.setContent(OutputStream.class, baos);
	        //
	        soapMessage.put("forceEmptySoapHeader", "true");
	        nmrExchange.setProperty(Message.class.getName(), soapMessage);

	        InterceptorChain phaseOut = getChain(Phase.ClientOut, soapBinding);
	        phaseOut.doIntercept(soapMessage);
	        jettyExchange.setMethod(HttpMethods.POST);
	        org.eclipse.ebpm.messaging.api.Message inMsg = nmrExchange.getIn(false);
	        
	        jettyExchange.setURL(getLocationUri(nmrExchange, inMsg,pc));
	        jettyExchange.setRequestContent(new ByteArrayBuffer(baos.toByteArray()));
	       
	        // OMAR SPECIFIC 
	        jettyExchange.setRequestContentType("text/xml");
	        
	        for (Map.Entry<String,String> entry : soapMessage.getTransportHeaders().entrySet()) {
	        	jettyExchange.addRequestHeader(entry.getKey(), entry.getValue());
	        }
		
	}
}
