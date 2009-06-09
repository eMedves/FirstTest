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

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.util.SessionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPClientProtocolHandler extends IoHandlerAdapter {


	private static final Logger log = LoggerFactory.getLogger(TCPClientProtocolHandler.class);
	public static final String WAIT_RESPONSE_KEY = "WAIT_RESPONSE_KEY".intern();
	public static final String MESSAGE_INSESSION_KEY = "MESSAGE_INSESSION_KEY".intern();
	
	private TCPOutInReceiver receiver;
	private TCPLogger tcpLog;
	
	public TCPClientProtocolHandler(TCPBCConfig cfg) {

		try {
			Class rClazz = Class.forName(cfg.getTcpOutInReceiverClassName());
			Object instance = rClazz.newInstance();
			if ( !(instance instanceof TCPOutInReceiver) ) {
				throw new Exception("Provider class name does not identify TCPOutInReceiver");
			}
			receiver = (TCPOutInReceiver)instance;
			receiver.init();
		} catch (Exception e) {
			log.error("Error istantiating TCP OutIn receiver: " +
				"messages forward will not be done", e);
		}
		tcpLog = new TCPLogger(cfg, "TCPProvider");
	}

	@Override
	public void messageSent(IoSession arg0, Object arg1) throws Exception {
		log.info("Message sent.");
		tcpLog.logDataSent(arg1);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		session.setAttribute(SessionLog.PREFIX, "Provider ");
		tcpLog.logSession("TCP Provider: Session OPENED: " +
			"remote["+session.getRemoteAddress()+"]," +
			"local["+session.getLocalAddress()+"]");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		tcpLog.logSession("TCP Provider: Session CLOSED: " +
				"remote["+session.getRemoteAddress()+"]," +
				"local["+session.getLocalAddress()+"]");
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		log.debug("messageReceived - start -");
		if (receiver != null) {
			receiver.messageReceived(message);
		}
		session.setAttribute(MESSAGE_INSESSION_KEY, message);
		session.setAttribute(WAIT_RESPONSE_KEY, Boolean.TRUE);
		log.debug("messageReceived - stop - thread="+Thread.currentThread().getId());
		tcpLog.logDataReceived(message);
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		log.error("Error",cause);
		session.close();
	}
	
	public void dispose() {
		if (tcpLog != null) {
			tcpLog.dispose();
		}
	}
	
}
