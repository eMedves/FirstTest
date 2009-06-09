package org.spagic3.connectors.http.adapters;

import org.apache.servicemix.nmr.api.Exchange;

import org.spagic3.connectors.http.SpagicJettyHTTPExchange;

public interface IHTTPOutputProtocolAdapter {
	
	public void fillJettyExchange(Exchange nmrExchange, SpagicJettyHTTPExchange jettyExchange);
	
	public void handleResponse(Exchange nmrExchange, SpagicJettyHTTPExchange jettyExchange);
	
	public void handleException(Exchange nmrExchange, SpagicJettyHTTPExchange jettyExchange, Throwable t);
	
}
