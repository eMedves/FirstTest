package org.spagic3.connectors.http.adapters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;


public interface IHTTPInputProtocolAdapter {
	
	public Exchange createExchange(HttpServletRequest request, String mep, String sender, String target) throws Exception;
	public void sendAccepted(Exchange exchange, HttpServletRequest request, HttpServletResponse response) throws Exception;
	public void sendError(Exchange exchange, Exception e, HttpServletRequest request, HttpServletResponse response) throws Exception;
	public void sendFault(Exchange exchange, Message fault,HttpServletRequest request, HttpServletResponse response) throws Exception;
	public void sendOut(Exchange exchange, Message out,HttpServletRequest request, HttpServletResponse response) throws Exception;

}
