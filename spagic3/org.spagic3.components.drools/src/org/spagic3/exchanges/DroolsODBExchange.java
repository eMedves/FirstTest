package org.spagic3.exchanges;

import org.apache.servicemix.nmr.api.Exchange;

public class DroolsODBExchange {
	private String exchangeId;
	private Exchange exchange;
	
	public DroolsODBExchange(String exchangeId, Exchange exchange) {
		super();
		this.exchangeId = exchangeId;
		this.exchange = exchange;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}
	
}
