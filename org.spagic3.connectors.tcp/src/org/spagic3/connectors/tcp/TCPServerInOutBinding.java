/**

    Copyright 2007, 2008 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
**/
package org.spagic3.connectors.tcp;


import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.api.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;

public class TCPServerInOutBinding  {
	
	private static final Logger log = LoggerFactory.getLogger(TCPServerInOutBinding.class);
	private org.spagic3.connectors.tcp.TCPServer server;
	private TCPMarshaller marshaller;
	
	public TCPServerInOutBinding(TCPServer endP) {
		server = endP;

	}
	
	private TCPMarshaller getMarshaller() {
		if (marshaller == null) {
			marshaller = new TCPMarshaller(server.getConfig());
		}
		return marshaller;
	}
	

	public void start()  {
	}
	
	private boolean isInOutMep() {
		return server.getConfig().getDefaultMep().toString().equals(CommonConfig.DEFINOUTMEP);
	}
	
	private boolean isInOnlyMep() {
		return server.getConfig().getDefaultMep().toString().equals(CommonConfig.DEFINMEP);
	}
	
	public void process(IoSession session, ByteBuffer msg) throws Exception  {
		log.debug("process: start");
		
        Exchange exchange = null;
        if (isInOutMep()) {
        	exchange = ExchangeUtils.createExchange(Pattern.InOut);
        }
        if (isInOnlyMep()) {
        	exchange = ExchangeUtils.createExchange(Pattern.InOnly);
        }
        String correlationId = "1000000000000";
    	exchange.setProperty(SpagicConstants.CORRELATION_ID, "1000000000000"); 
    	server.addCorrelatedSession(correlationId, session);
        Message inMessage = exchange.getIn();
        try {
        	// set on inMessage marshalled message
        	getMarshaller().toNMS(inMessage, msg);
        	
            if ( CommonConfig.OPERATION_MODE_BIDIRECTIONAL.equals(
            		server.getConfig().getPointMode()) ) {

            	server.getMessageRouter().send(exchange);
	            server.process(exchange);
            } 
           
	    }
	    finally {
			log.debug("process: end");
	    }		
	}
	
	
}
