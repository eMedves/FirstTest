package org.spagic3.connectors.http.adapters;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.util.DOM4JUtils;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.soap.api.InterceptorChain;
import org.apache.servicemix.soap.api.InterceptorProvider.Phase;
import org.apache.servicemix.soap.api.model.Binding;
import org.apache.servicemix.soap.bindings.http.HttpConstants;
import org.apache.servicemix.soap.bindings.soap.SoapFault;
import org.apache.servicemix.soap.bindings.soap.SoapVersion;
import org.spagic3.core.SpagicConstants;

public class SOAPInputProtocolAdapter implements IHTTPInputProtocolAdapter {
	
	
	private Binding<?> binding;
	private Map<Phase, InterceptorChain> chains = new HashMap<Phase, InterceptorChain>();
	
	public Binding<?> getBinding() {
		return binding;
	}

	public void setBinding(Binding<?> binding) {
		this.binding = binding;
	}

	public SOAPInputProtocolAdapter(){
	}
	
	@Override
	public Exchange createExchange(HttpServletRequest request, String mep,
			String sender, String target) throws Exception {
		 
		String method = request.getMethod();
        org.apache.servicemix.soap.api.Message soapMessage = binding.createMessage();
      
        soapMessage.put(org.apache.servicemix.soap.api.Message.CONTENT_TYPE, request.getContentType());
        Map<String, String> headers = soapMessage.getTransportHeaders();
        for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }
        headers.put(HttpConstants.REQUEST_URI, request.getRequestURL().toString());
        headers.put(HttpConstants.CONTENT_TYPE, request.getContentType());
        headers.put(HttpConstants.REQUEST_METHOD, method);
        if (HttpConstants.METHOD_POST.equals(method) || HttpConstants.METHOD_PUT.equals(method)) {
            soapMessage.setContent(InputStream.class, request.getInputStream());
        }
        request.setAttribute(org.apache.servicemix.soap.api.Message.class.getName(), soapMessage);
        soapMessage.put(SpagicConstants.SPAGIC_SENDER, sender);
        if (target != null)
        	soapMessage.put(SpagicConstants.SPAGIC_TARGET, target);
        
        InterceptorChain phase = getChain(Phase.ServerIn);
        phase.doIntercept(soapMessage);
        return (Exchange)soapMessage.getContent(Exchange.class);
	}

	@Override
	public void sendAccepted(Exchange exchange, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		 response.setStatus(HttpServletResponse.SC_ACCEPTED);
		
		
	}

	@Override
	public void sendError(Exchange exchange, Exception error,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		org.apache.servicemix.soap.api.Message soapMessageIn = ( org.apache.servicemix.soap.api.Message ) request.getAttribute(org.apache.servicemix.soap.api.Message.class.getName());
		org.apache.servicemix.soap.api.Message soapMessageOut = binding.createMessage(soapMessageIn);
		soapMessageOut.setContent(OutputStream.class, response.getOutputStream());
		soapMessageOut.setContent(Exchange.class, exchange);
		soapMessageOut.put(SoapVersion.class, soapMessageIn.get(SoapVersion.class));

        InterceptorChain phase = getChain(Phase.ServerOutFault);
        SoapFault soapFault;
        if (error instanceof SoapFault) {
            soapFault = (SoapFault) error;
        } else {
            soapFault = new SoapFault(error);
        }
        soapMessageOut.setContent(Exception.class, soapFault);
        phase.doIntercept(soapMessageOut);
		
	}

	@Override
	public void sendFault(Exchange exchange, Message fault,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		org.apache.servicemix.soap.api.Message soapMessageIn = ( org.apache.servicemix.soap.api.Message ) request.getAttribute(org.apache.servicemix.soap.api.Message.class.getName());
		org.apache.servicemix.soap.api.Message soapMessageOut = binding.createMessage(soapMessageIn);
		
		soapMessageOut.setContent(OutputStream.class, response.getOutputStream());
		soapMessageOut.setContent(Exchange.class, exchange);
		soapMessageOut.setContent(Message.class, fault);
		soapMessageOut.put(SoapVersion.class, soapMessageIn.get(SoapVersion.class));
       
        InterceptorChain phase = getChain(Phase.ServerOutFault);
        QName code = (QName) fault.getHeader(SpagicConstants.SOAP_FAULT_CODE);
        String reason = (String)fault.getHeader(SpagicConstants.SOAP_FAULT_REASON);
        SoapFault soapFault = new SoapFault(code, reason, null, null, DOM4JUtils.getDOM4JDocumentSource(fault));
        soapMessageOut.setContent(Exception.class, soapFault);
        phase.doIntercept(soapMessageOut);
		
	}

	@Override
	public void sendOut(Exchange exchange, Message out,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		 
		 response.setStatus(HttpServletResponse.SC_OK);
		 org.apache.servicemix.soap.api.Message soapMessageIn = ( org.apache.servicemix.soap.api.Message )  request.getAttribute(org.apache.servicemix.soap.api.Message.class.getName());
		 org.apache.servicemix.soap.api.Message soapMessageOut = binding.createMessage(soapMessageIn);
		 soapMessageOut.setContent(OutputStream.class, response.getOutputStream());
		 soapMessageOut.setContent(Exchange.class, exchange);
		 soapMessageOut.setContent(Message.class, out);
		 SoapVersion version = soapMessageIn.get(SoapVersion.class); 
		 soapMessageOut.put(SoapVersion.class, version);
		 response.setContentType(version.getSoapMimeType());
	     InterceptorChain phase = getChain(Phase.ServerOut);
	     phase.doIntercept(soapMessageOut);
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
