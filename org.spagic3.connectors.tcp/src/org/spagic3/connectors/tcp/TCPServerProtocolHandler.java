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
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.SSLFilter.SSLFilterMessage;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.apache.mina.util.SessionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.SpagicConstants;

/**
 * This component should decouple MINA interation with JBI one.
 * 
 */
public class TCPServerProtocolHandler extends IoHandlerAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(TCPServerProtocolHandler.class);
	private static int connNumber;
	/*
	 * TODO check if you only need a ConsumerEndpoint or SimpleEndpoint
	 */
	private TCPServer endpoint;
	private TCPServerInOutBinding binding;
	
	private TCPLogger tcpLog;
	
	public TCPServerProtocolHandler(org.spagic3.connectors.tcp.TCPServer endP) throws Exception {
		binding = new TCPServerInOutBinding(endP);
		endpoint = endP;
		tcpLog = new TCPLogger(endP.getConfig(), "TCPConsumer");
	}
	
	/**
	 * Here messages managed by the chain are received. SSL notification can be
	 * logged, otherwise messages decode by the 'codec' are managed. 
	 * 
	 * 
	 * @Override
	 */
	public synchronized void messageReceived(IoSession session, Object msg) throws Exception {

		log.debug("messageReceived -> Start");
		if (msg instanceof SSLFilterMessage) {
			if (SSLFilter.SESSION_SECURED.equals(msg)) {
				log.debug("Session SECURED");
			}
			return;
		}
		
		if (!session.isConnected()){
			log.debug("messageReceived -> session is NOT Connected");
		}else{
			log.debug("messageReceived -> Session is OK");
			((SocketSessionConfig ) session.getConfig() ).setReceiveBufferSize( 2048 );
			if (log.isDebugEnabled()) {
				ByteBuffer rb = ( ByteBuffer ) msg;
				log.debug("messageReceived -> RECEIVED: "+rb.capacity());
				rb.rewind();
			}
			log.debug("messageReceived -> Read messages: "+session.getReadMessages());
			binding.process(session, (ByteBuffer)msg);
			tcpLog.logDataReceived(msg);
		}
	}

	@Override
	public void messageSent(IoSession arg0, Object arg1) throws Exception {
		super.messageSent(arg0, arg1);
		tcpLog.logDataSent(arg1);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		
		session.setAttribute(SessionLog.PREFIX, "Consumer ");
		tcpLog.logSession("TCP Consumer: Session OPENED: " +
				"remote["+session.getRemoteAddress()+"]," +
				"local["+session.getLocalAddress()+"]");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		
		synchronized (TCPServerProtocolHandler.class) {
			
			binding.start();
			// here you can configure the just created session
			// buffer size...
			// ssl negotiation notification
	
			// We're going to use SSL negotiation notification.
	        session.setAttribute( SSLFilter.USE_NOTIFICATION );
	        connNumber++;

	        log.debug("Accepted connections: "+connNumber);
			if (endpoint.getConfig().getConnNumber() != -1 && connNumber > endpoint.getConfig().getConnNumber()) {
				log.debug("Connection number reached... close connection!");
//				endpoint.pauseAccept();
				session.close();
			}
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		synchronized (TCPServerProtocolHandler.class) {
			connNumber--;
//			if (endpoint.getConfig().getConnNumber() != -1) {
//				log.debug("Check if we need to restart binding");
//				endpoint.startAccept();
//			}
		}
		String correlationId = (String)session.getAttribute(SpagicConstants.CORRELATION_ID);
		endpoint.removeCorrelatedSession(correlationId);
		tcpLog.logSession("TCP Consumer: Session CLOSED: " +
				"remote["+session.getRemoteAddress()+"]," +
				"local["+session.getLocalAddress()+"]");
	}

    @Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error("exception occurred", cause);
        session.close();
	}
    
    public void dispose() {
    	tcpLog.dispose();
    }
    
}
