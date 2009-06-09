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
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spagic3.connectors.tcp;

import java.net.InetSocketAddress;

import org.apache.commons.codec.binary.Base64;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.api.Status;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.connectors.tcp.codec.util.NMUtils;
import org.spagic3.connectors.tcp.ssl.TCPBCSSLContextFactory;
import org.spagic3.core.AbstractSpagicConnector;
import org.spagic3.core.IConnector;
import org.spagic3.core.routing.IMessageRouter;



/**
 * Implement the "TCPClient"
 * 
 * @org.apache.xbean.XBean element="provider"
 */
public class TCPClient extends AbstractSpagicConnector {

	private static final Logger log = LoggerFactory.getLogger(TCPClient.class);
	
	private static final double retryPower = 2.0;
	private TCPBCConfig config;
	private TCPMarshaller marshaller;
	private ConnectFuture future;


	

	private TCPMarshaller getMarshaller() {
		if (marshaller == null) {
			marshaller = new TCPMarshaller(config);
		}
		return marshaller;
	}

	protected String getEndpoint(){
		return this.getSpagicId();
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		if (exchange.getPattern() == Pattern.InOnly){
			processInOnly(exchange, exchange.getIn());
		}else{
			processInOut(exchange, exchange.getIn(), exchange.getOut());
		}	
	}
	
	protected void processInOnly(Exchange exchange, Message in) throws Exception {
	
		processInOut(exchange, in, null);
	}

	/**
	 * Use MINA to open a comunication to a TCPServer end.
	 */
	protected void processInOut(Exchange exchange, Message in, Message out) throws Exception {

		log.debug(getEndpoint()+ "processInOut->start");

		log.debug(getEndpoint() + "processInOut->Creating IOConnector");
		IoConnector connector = new SocketConnector();

		log.debug(getEndpoint() + "processInOut->IOConnector Creaing Filter Chain");
		DefaultIoFilterChainBuilder chain = connector.getDefaultConfig()
				.getFilterChain();

		if (config.isUseSSL()) {
			log.debug("processInOut->SSL Supporr Required configure SSL In Chain");
			addSSLSupport(chain);
		}
		log.debug(getEndpoint() + "processInOut-> Add The Codec Protocol Filter ");
		chain.addLast("codec", new ProtocolCodecFilter(new org.spagic3.connectors.tcp.codec.TCPMsgCodecFactory(config)));

		if (log.isDebugEnabled()) {
			addLogger(chain);
		}

		synchronized (this) {
			log.debug(getEndpoint() + "processInOut-> call sendTCPMessage : Entered in Synchnronized Code Block ");
			sendTCPMessage(connector, exchange, in, out);
			log.debug(getEndpoint() + "processInOut-> after send : Finish Synchnronized Code Block ");
		}
		/*
		if (TCPBCConfig.CONN_MODE_MAINTAIN.equals(config.getConnectionMode())
				|| TCPBCConfig.CONN_MODE_MANTAIN.equals(config
						.getConnectionMode())) {

			log.debug(getEndpoint() + "processInOut-> CONN_CODE_MANTAIN -> SYNCHRONIZATION REQUIRED on Object ");
			synchronized (this) {
				log.debug(getEndpoint() + "processInOut-> call sendTCPMessage : Entered in Synchnronized Code Block ");
				sendTCPMessage(connector, exchange, in, out);
				log.debug(getEndpoint() + "processInOut-> after send : Finish Synchnronized Code Block ");
			}

		} else {
			
			
			log.debug(getEndpoint() + "processInOut-> Synchronization NOT REQUIRED : call send ");
			sendTCPMessage(connector, exchange, in, out);
			log.debug(getEndpoint() + "processInOut-> after sendTCPMessage ");
			
		}
		*/

		// log.debug(getEndpoint() + "processInOut--> Verify Configuration ");
		// log.debug(getEndpoint() + "**** TCPBCConfig.CONN_MODE_MAINTAIN -> " +
		// TCPBCConfig.CONN_MODE_MAINTAIN);
		// log.debug(getEndpoint() + "**** config.getConnectionMode() -> " +
		// config.getConnectionMode());
		// log.debug(getEndpoint() + "****
		// (TCPBCConfig.CONN_MODE_MAINTAIN.equals(config.getConnectionMode()) ->
		// " +
		// TCPBCConfig.CONN_MODE_MAINTAIN.equals(config.getConnectionMode()));
		// synchronized (this) {
		// log.debug(getEndpoint() + "processInOut-> call sendTCPMessage : Entered in
		// Synchnronized Code Block ");
		// sendTCPMessage(connector, exchange, in, out);
		// log.debug(getEndpoint() + "processInOut-> after send : Finish Synchnronized Code
		// Block ");
		// }
		// log.debug(getEndpoint() + "processInOut-> stop");

	}

	private IoSession getTCPSession(ConnectFuture connectFuture) throws Exception {
		// can throw exception
		IoSession aSession = null;
		long previousDelayTime = config.getRetryDelay();

		log.debug(getEndpoint() + "sendTCPMessage::getTCPSession Getting Session");

		log.debug(getEndpoint() + "sendTCPMessage::getTCPSession Future Is Ready -> [" + connectFuture.isReady() + "]");
		if (!connectFuture.isConnected()) {
			// log.debug(getEndpoint() + "sendTCPMessage::getTCPSession Future Is Not Connected
			// -> Must JOIN");
			// future.join();
			// log.debug(getEndpoint() + "JOIN OK");
		}

		// -> Ciclo For
		for (int retry = 0; retry <= config.getRetryNumber(); retry++) {
			try {
				log.debug("sendTCPMessage::getTCPSession :: Getting TCP Session Retry [" + retry + "] Getting session");
				aSession = connectFuture.getSession();
			} catch (RuntimeIOException rIOe) {
				log.warn("sendTCPMessage::Runtime Exception", rIOe);
			}
			if (aSession != null) {
				log.debug("sendTCPMessage::getTCPSession :: Getting TCP Session Retry [" + retry + "] Getted OK ");
				log.debug(getEndpoint() + "sendTCPMessage::getTCPSessionSession Is Closing -> " + aSession.isClosing());
				log.debug("sendTCPMessage::getTCPSessionSession Is Connected -> " + aSession.isConnected());
				if (!aSession.isClosing() && aSession.isConnected()) {
					log.debug(getEndpoint() + "sendTCPMessage::getTCPSession:: Session OK");
					return aSession;
				}
			} else {
				log.debug("sendTCPMessage::getTCPSession Session is NOT VALID ");
				if (retry <= config.getRetryNumber()) {
					long delay = getRetryWaitTime(previousDelayTime);
					log.debug("sendTCPMessage::getTCPSession:: Sleeping For Retry Wait Time" + delay);
					if (delay == -1) {
						return null;
					}
					log.debug("sendTCPMessage::getTCPSession::Retring in: (ms) " + delay);
					previousDelayTime = delay;
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ie) {
						log.error("Error waiting for retry", ie);
					}
				}
			}
		}
		return aSession;

	}

	private void sendTCPMessage(IoConnector connector, Exchange exchange, Message in, 
			Message out) throws Exception {

		log.debug(getEndpoint() + "sendTCPMessage(connector,exchange,in,out)");
		// connect to the TCPServer using Retry policies
		ConnectFuture connectFuture = getConnection(connector, in);
		if (connectFuture == null) {
			// no connection is possible
			log.warn("sendTCPMessage::connect future is null");
			return;
		}

		IoSession session = getTCPSession(connectFuture);
		// if session isn't connected create a new connection
		if (!session.isConnected()){
			this.future = null;
			connectFuture = getConnection(connector, in);
			session = getTCPSession(connectFuture);
		}

		if (session == null) {
			String remoteHost = getDynamicRemoteHost(in);
			Integer remotePort = getDynamicRemotePort(in);

			log.warn("Error creating connection host=" + remoteHost + ", port=" + remotePort);
			
			Element faultElem = DocumentHelper.createElement("Fault");
			faultElem.setText("Error creating connection host=" + remoteHost + ", port=" + remotePort);
			
			exchange.getFault().setBody(faultElem.asXML());
			exchange.getOut().setBody(faultElem.asXML());
			return;
		}

		if (session.isConnected()) {
			log.debug(getEndpoint() + "Session Is Connected");
			try {
				boolean received = false;
				boolean isNullResponse = false;
				session.setAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY, Boolean.FALSE);
				for (int numRetry = 0; numRetry < config.getRetryCount() && !received; numRetry++) {
					// write the message
					Object msgObj = NMUtils.retrieveMessageInEnvelope(in, config.getInNmEnvelope() + "/text()");
					if (config.isBase64decode()) {
						byte[] decodedB = Base64.decodeBase64((byte[]) msgObj);
						msgObj = decodedB;
					}

					log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry + " -> Message to send  " + msgObj);

					/*
					 * if (log.isDebugEnabled()) { log.debug(getEndpoint() + "Message to send
					 * (retry="+numRetry+"): "+msgObj); log.debug(getEndpoint() + "Checks before
					 * send"); Boolean waitResponseKey =
					 * (Boolean)session.getAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY);
					 * log.debug(getEndpoint() + "Is there response message? "+waitResponseKey);
					 * if (waitResponseKey) { log.debug(getEndpoint() + "Response message: "+
					 * session.getAttribute(TCPClientProtocolHandler.MESSAGE_INSESSION_KEY)); } }
					 */
					log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry + " -> Before WriteObject");
					WriteFuture wFuture = session.write(msgObj);
					wFuture.join();
					log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry + " -> After wFuture.join()");

					if (exchange.getPattern() == Pattern.InOnly || exchange.getPattern() == Pattern.InOut) {
						// terminate the exchange
						exchange.setStatus(Status.Done);
					}

					try {
						log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry + " -> Waiting For Response ");
						received = waitForResponse(session);
						log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry + " -> Response Arrived ");
						Object response = session .getAttribute(TCPClientProtocolHandler.MESSAGE_INSESSION_KEY);
						log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry+ " -> Received ["+received+"]");
						
						if ( response != null )
							log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry+ " -> Response Not Null");
						else
							log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry+ " -> Response Is Null");
						
						if (received){
							if (response != null && response instanceof ByteBuffer) {
								log.debug(getEndpoint()+"sendTCPMessage::Retry=" + numRetry + " -> Transforming Response To Normalized Message ");
								getMarshaller().toNMS(out, (ByteBuffer)response);
								log.debug(getEndpoint() + "sendTCPMessage::Retry=" + numRetry+ " -> OK ");
								session.setAttribute(TCPClientProtocolHandler.MESSAGE_INSESSION_KEY);
							}else {
								isNullResponse = true;
								log.debug(getEndpoint()+"sendTCPMessage::Retry="+ numRetry+ " -> Received a Null Response ");
								log.warn(getEndpoint()+"sendTCPMessage::Retry=" + numRetry + " -> Unable to handle tcp response of type: " +
									((response != null) ? response.getClass().getName(): response));
							}
						} else {
							//do nothing
						}
					} catch (InterruptedException ie) {
						log.error(getEndpoint()+"sendTCPMessage::Retry=" + numRetry + " -> Error waiting for a response", ie);
					} catch (Throwable t) {
						log.error(getEndpoint()+"sendTCPMessage::Retry=" + numRetry + " -> Uncatched Exception Occurred", t);
					} finally {
						// prepare session for next request response exchange
						session.setAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY);
					}
				}
				
				if (!received) {
					log.warn(getEndpoint()+"sendTCPMessage::No response received after retry policy");

					Element faultElem = DocumentHelper.createElement("Fault");
					faultElem.setText("No response received from TCP server");
					log.error("No response received from TCP server");
					exchange.getFault().setBody(faultElem.asXML());
					exchange.getOut().setBody(faultElem.asXML());
				}else if (isNullResponse){
					log.warn(getEndpoint()+"sendTCPMessage::Received a Null Response");
					Element faultElem = DocumentHelper.createElement("Fault");
					faultElem.setText("Received a Null Response From TCP Server");
					log.error("Received a Null Response From TCP Server");
					exchange.getFault().setBody(faultElem.asXML());
					exchange.getOut().setBody(faultElem.asXML());
				}
				
			} finally {
				closeConnection(connectFuture);
			}
		} else {
			log.warn("sendTCPMessage::Session not created!");
		}

	}

	private void closeConnection(ConnectFuture future) {

		if (TCPBCConfig.CONN_MODE_MAINTAIN.equals(config.getConnectionMode()) 
				|| TCPBCConfig.CONN_MODE_MANTAIN.equals(config.getConnectionMode())) {
			log.debug("closeConnection::Endpoint Configured To Mantain Session ");
			log.debug(getEndpoint() + "Nothing to do on future and session");
		} else {
			log.debug(getEndpoint() + "closeConnection::Endpoint Configured To Not Mantain Session ");
			log.debug(getEndpoint() + "closeConnection::Endpoint Configured To Not Mantain Session close session and future");
			future.getSession().close();
			future = null;
		}
	}

	private boolean waitForResponse(IoSession session)
			throws InterruptedException {
		Long cycleNumberL = config.getResponseTimeout() / 500;
		int cycleNumber = cycleNumberL.intValue();
		log.info("waitForResponse::Start Polling Loop......");
		for (int j = 0; j < cycleNumber; j++) {
			Thread.sleep(500);
			log.info(getEndpoint()+"waitForResponse::Loop Poll IoSession " + (j + 1) * 500);
			Boolean received = (Boolean) session.getAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY);
			if (received.booleanValue()) {
				log.info(getEndpoint()+"waitForResponse::Received after " + (j + 1) * 500);
				return true;
			}
		}
		log.info("waitForResponse::End Polling Loop......");
		Boolean received = (Boolean) session.getAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY);
		if (received.booleanValue()) {
			log.info(getEndpoint()+"waitForResponse::After End of Polling Loop Returning True......");
			return true;
		}
		log.info(getEndpoint()+"waitForResponse::After End of Polling Loop Returning False......");
		return false;
		/*
		 * for( int j = 0; j < 1; j ++ ) { Thread.sleep( 500 ); Boolean received =
		 * (Boolean)session
		 * .getAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY); if(
		 * received.booleanValue() ) { return true; } Thread.sleep(
		 * config.getResponseTimeout() ); //check if in the meanwhile the
		 * response arrived received = (Boolean)session
		 * .getAttribute(TCPClientProtocolHandler.WAIT_RESPONSE_KEY); return
		 * received; } return false;
		 */
	}

	private String getDynamicRemoteHost(Message in) {
		String remoteHost = (String) in.getHeader(TCPBCConfig.TCP_DESTINATION_HOST);
		if (remoteHost == null) {
			remoteHost = config.getRemoteHost();
		} else {
			log.debug(getEndpoint() + "Remote Host URI overridden: " + remoteHost);
		}
		return remoteHost;
	}

	private Integer getDynamicRemotePort(Message in) {

		Integer remotePort = (Integer) in.getHeader(TCPBCConfig.TCP_DESTINATION_PORT);
		if (remotePort == null) {
			remotePort = config.getRemotePort();
		} else {
			log.debug(getEndpoint() + "Remote port URI overridden: " + remotePort);
		}
		return remotePort;
	}

	/**
	 * Connect to the TCPServer using retry policies: <br/><br/> From Rapsody
	 * manual:<br/> The Retry Count setting determines how many times the
	 * message should be resent while waiting for a response. This can be set to
	 * 0, to indicate that the message should never be resent, or -1, to
	 * indicate that there is no maximum number of resends. Once the maximum
	 * number of resends has been reached and the timeout occurs, the Fail
	 * Action is executed (which is one of Send Message to Error Queue, Close
	 * Connection, or Send to Error Queue and Close Connection.
	 * 
	 * @param connector
	 * @return
	 */
	private ConnectFuture getConnection(IoConnector connector,
			Message in) {
		long previousDelayTime = config.getRetryDelay();

		if (TCPBCConfig.CONN_MODE_MAINTAIN.equals(config.getConnectionMode())
				|| TCPBCConfig.CONN_MODE_MANTAIN.equals(config.getConnectionMode())) {
			if ((future != null) && future.isConnected()) {
				log.debug(getEndpoint()+"getConnection::Returning already initiated connection");
				return future;
			}
		}

		for (int retry = 0; retry <= config.getRetryNumber(); retry++) {
			log.debug(getEndpoint() + "getConnection::Retry=" + retry + " start");

			try {
				String remoteHost = getDynamicRemoteHost(in);
				log.debug(getEndpoint() + "getConnection::Retry=" + retry + " Remote Host" + remoteHost);
				Integer remotePort = getDynamicRemotePort(in);
				log.debug(getEndpoint() + "getConnection::Retry=" + retry + " Remote Port" + remotePort);
				log.debug(getEndpoint() + "getConnection::Retry=" + retry + " Before Connect");
				ConnectFuture localFuture = connector.connect(
						new InetSocketAddress(remoteHost, remotePort),
						new TCPClientProtocolHandler(config));

				// connection obtained
				localFuture.join();
				log.debug(getEndpoint() + "getConnection::Retry=" + retry + " After Connect");

				if (TCPBCConfig.CONN_MODE_MAINTAIN.equals(config.getConnectionMode())
					|| TCPBCConfig.CONN_MODE_MANTAIN.equals(config.getConnectionMode())) {
					// I forget the previous instance????
					future = localFuture;
				}
				log.debug(getEndpoint() + "getConnection::Retry=" + retry + " Return local Future");
				return localFuture;
			} catch (Exception e) {
				log.error(getEndpoint()+"Error connecting to TCPServer", e);
				if (retry <= config.getRetryNumber()) {
					long delay = getRetryWaitTime(previousDelayTime);
					if (delay == -1) {
						return null;
					}
					log.debug(getEndpoint() + "...Retring in: (ms) " + delay);
					previousDelayTime = delay;
					try {
						Thread.sleep(delay);
					} catch (InterruptedException ie) {
						log.error(getEndpoint()+"Error waiting for retry", ie);
					}
				}
			}
		}
		return null;
	}

	private long getRetryWaitTime(long previousTime) {
		String retryType = config.getRetryType();
		if (retryType.equals(CommonConfig.RETRY_TYPE_IMMEDIATE)) {
			return 0;
		}
		if (retryType.equals(CommonConfig.RETRY_TYPE_LINEAR)) {
			return config.getRetryDelay();
		}
		if (retryType.equals(CommonConfig.RETRY_TYPE_EXPONENTIAL)) {
			return (long) Math.pow(previousTime, retryPower);
		}
		if (retryType.equals(CommonConfig.RETRY_TYPE_NO_RETRY)) {
			return -1;
		}

		return -1;
	}

	private void addSSLSupport(DefaultIoFilterChainBuilder chain)
			throws Exception {
		// TODO check if you can use always the same SSLFilter
		SSLFilter sslFilter = new SSLFilter(TCPBCSSLContextFactory.getInstance(config));
		sslFilter.setUseClientMode(true);
		chain.addLast("SSL", sslFilter);
		log.info("SSL ON");
	}

	public TCPBCConfig getConfig() {
		return config;
	}

	public void setConfig(TCPBCConfig config) {
		this.config = config;
	}

	private void addLogger(DefaultIoFilterChainBuilder chain) throws Exception {
		// we have to log CONNECTION and DATA.
		// TODO CHECK If the logger is added after the codec it receives data
		// already
		// converted to the codec protocol?
		chain.addLast("logger", new LoggingFilter());
		log.info("Logging ON");
	}


	public void stop() throws Exception {
		if (future != null) {
			((TCPClientProtocolHandler) future.getSession().getHandler()).dispose();
			future.getSession().close();
		}
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
