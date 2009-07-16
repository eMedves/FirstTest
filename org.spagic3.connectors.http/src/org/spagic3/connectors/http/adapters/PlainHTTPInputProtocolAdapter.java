package org.spagic3.connectors.http.adapters;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;

public class PlainHTTPInputProtocolAdapter implements IHTTPInputProtocolAdapter {

	@Override
	public Exchange createExchange(HttpServletRequest request, String mep, String sender, String target) throws Exception  {
		Exchange exchange = null;
		
		if (mep.equalsIgnoreCase(SpagicConstants.IN_OUT_MEP)){
			exchange = ExchangeUtils.createExchange(sender,target, Pattern.InOut);
        }else if (mep.equalsIgnoreCase(SpagicConstants.IN_ONLY_MEP)){
        	exchange = ExchangeUtils.createExchange(sender,target, Pattern.InOnly);
        }
		
		Message in = exchange.getIn();
	    InputStream is = request.getInputStream();
	    is = new BufferedInputStream(is);
	    SAXReader reader = new SAXReader();
	    Document doc = reader.read(is);
	    in.setBody(doc.asXML());
	    exchange.setIn(in);
	    return exchange;
	}

	@Override
	public void sendAccepted(Exchange exchange, HttpServletRequest request,
			HttpServletResponse response) {
		
		 response.setStatus(HttpServletResponse.SC_ACCEPTED);
		
	}

	@Override
	public void sendError(Exchange exchange, Exception error,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        
		XMLWriter responseWriter = new XMLWriter(response.getWriter());
        Document doc = DocumentHelper.createDocument();
        
        Element root = doc.addElement("error");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        pw.close();
        root.addText(sw.toString());
        responseWriter.write(doc);
        responseWriter.flush();
        responseWriter.close();
		
	}

	@Override
	public void sendFault(Exchange exchange, Message fault,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		XMLWriter responseWriter = new XMLWriter(response.getWriter());
	   	 Document doc = DocumentHelper.parseText((String)fault.getBody());
	   	 responseWriter.write(doc);
	   	 responseWriter.flush();
	   	 responseWriter.close();
		
	}

	@Override
	public void sendOut(Exchange exchange, Message out,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
	
		XMLWriter responseWriter = new XMLWriter(response.getWriter());
   	 	Document doc = DocumentHelper.parseText((String)out.getBody());
   	 	responseWriter.write(doc);
   	 	responseWriter.flush();
   	 	responseWriter.close();
		
	}

}
