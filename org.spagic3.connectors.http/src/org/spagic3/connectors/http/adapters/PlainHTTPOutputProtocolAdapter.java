package org.spagic3.connectors.http.adapters;

import java.net.URI;
import java.util.Map;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.api.Status;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpHeaders;
import org.spagic3.connectors.http.SpagicJettyHTTPExchange;

/**
 * Default marshaler used for non-soap provider endpoints.
 *
 * @author gnodet
 * @since 3.2
 */
public class PlainHTTPOutputProtocolAdapter implements IHTTPOutputProtocolAdapter {

   
    private String locationURI;
    private String method;
    private String contentType = "text/xml";
   
    private Map<String, String> headers;

    public String getLocationURI() {
        return locationURI;
    }

    public void setLocationURI(String locationUri) {
        this.locationURI = locationUri;
    }

  

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    protected String getLocationUri(Exchange exchange, Message inMsg)  {
       if (inMsg.getHeader("locationURI") != null){
    	   return (String)inMsg.getHeader("locationURI");
       }else{
    	   return this.locationURI;
       }
       
    }

    protected String getMethod(Exchange exchange, Message inMsg)  {
    	if (inMsg.getHeader("httpMethod") != null){
     	   return (String)inMsg.getHeader("httpMethod");
        }else{
     	   return this.method;
        }
    }

    protected String getContentType(Exchange exchange, Message inMsg) {
    	if (inMsg.getHeader("content-type") != null){
      	   return (String)inMsg.getHeader("content-type");
         }else{
      	   return this.contentType;
         }
    }

    public void fillJettyExchange(Exchange exchange,
                              SpagicJettyHTTPExchange httpExchange) {
        try{
        	Message inMsg = exchange.getIn(false);
        	httpExchange.setURL(getLocationUri(exchange, inMsg));

        	// Temporary fix for bug in jetty-client 6.1.5
        	// http://fisheye.codehaus.org/browse/jetty-contrib/jetty/trunk/contrib/client/src/main/java/org/mortbay/jetty/client/HttpConnection.java?r1=374&r2=378
        	httpExchange.addRequestHeader(HttpHeaders.HOST_BUFFER, new ByteArrayBuffer(new URI(getLocationUri(exchange, inMsg)).getHost()));

        	httpExchange.setMethod(getMethod(exchange, inMsg));
        	httpExchange.setRequestHeader(HttpHeaders.CONTENT_TYPE, getContentType(exchange, inMsg));
        	if (getHeaders() != null) {
        		for (Map.Entry<String, String> e : getHeaders().entrySet()) {
        			httpExchange.setRequestHeader(e.getKey(), e.getValue());
        		}
        	}
        	if (inMsg.getBody() != null) {
           
        		byte[] byteBuffer = ((String)inMsg.getBody()).getBytes();
        		httpExchange.setRequestContent(new ByteArrayBuffer(byteBuffer));
        	}
        }catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    public void handleResponse(Exchange exchange, SpagicJettyHTTPExchange httpExchange) {
    	try{
    		int response = httpExchange.getStatus();
    		if (response != org.mortbay.jetty.HttpStatus.ORDINAL_200_OK && response != org.mortbay.jetty.HttpStatus.ORDINAL_202_Accepted) {
            
        	if (exchange.getPattern() == Pattern.InOnly){
        		exchange.setError(new Exception("Invalid status response: " + response));
        	}
        	if (exchange.getPattern() == Pattern.InOut){
        		Message fault = exchange.getFault(true);
        		fault.setBody(new String(httpExchange.getResponse()));
        		exchange.setFault(fault);
        	}
        	
        } else  {
        	if (exchange.getPattern() == Pattern.InOut){
        		Message out = exchange.getOut();
        		out.setBody(new String(httpExchange.getResponse()));
        		exchange.setOut(out);
        	} else {
        		exchange.setStatus(Status.Done);
        	}
        }
    	}catch (Exception e) {
    		
			throw new RuntimeException(e);
		}
    }

    public void handleException(Exchange exchange, SpagicJettyHTTPExchange httpExchange, Throwable ex) {
        exchange.setError((Exception)ex);
    }

}
