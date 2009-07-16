package org.spagic3.connectors.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.commons.codec.binary.Base64;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.util.NewThreadExecutor;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.connectors.tcp.codec.TCPMsgCodecFactory;
import org.spagic3.connectors.tcp.codec.util.NMUtils;
import org.spagic3.connectors.tcp.ssl.TCPBCSSLContextFactory;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.AbstractSpagicConnector;

public class TCPServer extends AbstractSpagicConnector {
	
	
	private static final String SSL_RSA_WITH_NULL_SHA = "SSL_RSA_WITH_NULL_SHA";
	private static final Logger log = LoggerFactory.getLogger(TCPServer.class); 
	private TCPBCConfig config;

	private InetSocketAddress socketAddress;
	private TCPServerProtocolHandler serverHandler;
	private IoAcceptorConfig acceptorConfig;
	private IoAcceptor acceptor = null;
	private HashMap<String, IoSession> correlatedSessions;	
	
	public void init(){
		System.out.println("-- TCP Server Component Init --");
		
		TCPBCConfig config = new TCPBCConfig();
		/*
		<property name="connection.number" value="-1"/>
	    <property name="local.port" value="10222"/>
	    <property name="local.address" value="localhost"/>
	    <property name="point.name" value="TCP2.TCP_v_0"/>
	    <property name="point.type" value="POINT_TYPE_SERVER"/>
	    <property name="point.mode" value="OPERATION_MODE_BIDIRECTIONAL"/>
	    
	    <property name="out.envelope" value="PLAIN-HL7"/>
	    <property name="in.envelope" value="PLAIN-HL7"/>
	    
	    <property name="base64.decode" value="true"/>
	    <property name="base64.encode" value="true"/>
	    
	    <property name="mep" value="http://www.w3.org/2004/08/wsdl/in-out"/>
	    
	    <property name="incoming.wrapper" value="WRAPPER_USER"/>
	    <property name="incoming.header" value="0x0B"/>
	    <property name="incoming.trailer" value="0x1C 0x0D"/>
	    
	    <property name="outgoing.wrapper" value="WRAPPER_USER"/>
	    <property name="outgoing.header" value="0x0B"/>
	    <property name="outgoing.trailer" value="0x1C 0x0D"/>
	    
	    <property name="strip.wrapping" value="true"/>
	    
	    <property name="log.connections" value="true"/>
	    <property name="log.data" value="false"/>
	    <property name="log.dataAsHex" value="false"/>
	    */
		config.setConnNumber(propertyConfigurator.getInteger("connection.number"));
		config.setLocalPort(propertyConfigurator.getInteger("local.port"));
		config.setLocalAddress(propertyConfigurator.getString("local.address"));
  
		config.setPointName(propertyConfigurator.getString("point.name"));
		config.setPointType(propertyConfigurator.getString("point.type"));
		config.setPointMode(propertyConfigurator.getString("point.mode"));
		config.setOutNmEnvelope(propertyConfigurator.getString("out.envelope"));
		config.setInNmEnvelope(propertyConfigurator.getString("in.envelope"));
		config.setBase64decode(propertyConfigurator.getBoolean("base64.decode"));
		config.setBase64encode(propertyConfigurator.getBoolean("base64.encode"));
		config.setDefaultMep(URI.create(propertyConfigurator.getString("mep")));
		
		config.setIncomingWrapper(propertyConfigurator.getString("incoming.wrapper"));
		config.setIncomingHeader(propertyConfigurator.getString("incoming.header"));
		config.setIncomingTrailer(propertyConfigurator.getString("incoming.trailer"));
		config.setStripWrapping(propertyConfigurator.getBoolean("strip.wrapping"));
	
		config.setOutgoingWrapper(propertyConfigurator.getString("outgoing.wrapper"));
		config.setOutgoingHeader(propertyConfigurator.getString("outgoing.header"));
		config.setOutgoingTrailer(propertyConfigurator.getString("outgoing.trailer"));
		
		config.setLogConnections(propertyConfigurator.getBoolean("log.connections"));
		config.setLogData(propertyConfigurator.getBoolean("log.data"));
		config.setLogDataAsHex(propertyConfigurator.getBoolean("log.dataAsHex"));
   		
		
		config.setConnectionLogFileName(propertyConfigurator.getString("log.connections.filename"));
		config.setExtraInformation(propertyConfigurator.getString("log.extrainfo"));
		setConfig(config);
	}	
	
	/**
     * Start the MINA server; setting:
     * <ul>
     * 	<li>the 'codec' that recognise messages of the TCP protocol unwrapping received bytes</li>
     * 	<li>optionally add ssl support</li>
     * 	<li>the TCPServerProtocolHandler that manage messages identify by the 'codec'</li>
     * </ul>
     */
    public void start() throws Exception {

        
        correlatedSessions = new HashMap<String, IoSession>();
        
        // Suggested in MINA FAQ
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        
        log.debug("start -> Byte Buffer.isUseDirectBuffer -> ["+ByteBuffer.isUseDirectBuffers()+"]");
        log.debug("start -> Byte Buffer.Allocator Class Name -> ["+ByteBuffer.getAllocator().getClass().getName()+"]");
       

        if (config.getConnNumber() > 0) {
        	acceptor = new SocketAcceptor(config.getConnNumber(), new NewThreadExecutor());
        } else {
        	acceptor = new SocketAcceptor();
        }
        acceptorConfig = new SocketAcceptorConfig();
        
        ((SocketAcceptorConfig)acceptorConfig).setReuseAddress(true);
        ((SocketAcceptorConfig)acceptorConfig).setDisconnectOnUnbind(true);
        ((SocketAcceptorConfig)acceptorConfig).setBacklog(config.getListenBacklog());
        
        
        // This is the filters pipeline
        DefaultIoFilterChainBuilder chain = acceptorConfig.getFilterChain();
        
        if (log.isDebugEnabled()) {
        	addLogger( chain );
        }
        
        if (config.isUseSSL()) {
        	addSSLSupport(chain);
        }

        chain.addLast( "codec", new ProtocolCodecFilter(
            	new TCPMsgCodecFactory(config)));
        
        socketAddress = new InetSocketAddress(
            	InetAddress.getByName(this.config.getLocalAddress()),
            	this.config.getLocalPort() );
        serverHandler = new TCPServerProtocolHandler(this);
        
        // Bind
        startAccept();

        log.info( "Listening on port " + this.config.getLocalPort() );
    }
    
    public void stop() throws Exception {

        if (serverHandler != null) {
        	serverHandler.dispose();
        }
        acceptor.unbindAll();
    }
    
    public void pauseAccept() {
    	acceptor.unbindAll();
    }
    
    public void startAccept() throws IOException {
    	log.debug("Acceptor managing socket? "+acceptor.isManaged(socketAddress));
    	if (!acceptor.isManaged(socketAddress)) {
   			acceptor.bind(socketAddress, serverHandler, acceptorConfig);
    	}
    	log.debug("Acceptor managing socket? "+acceptor.isManaged(socketAddress));
    }

    public void process(Exchange exchange) throws Exception {
        // As we act as a consumer (we just send JBI exchanges)
        // we will receive responses or DONE / ERROR status here
    	log.debug("Received ME: ", exchange);
    	
    	
		if (exchange.getStatus() == Status.Done) {
			// This should never happen
			log.debug("Received an unattended DONE message");
			return;
		}
    	
    	
    	synchronized (correlatedSessions) {
    		
	    	if (config.getDefaultMep().toString().equals(
	    			CommonConfig.DEFINOUTMEP)) {
	    		
	    		try {
	    			String correlationId = (String)exchange.getProperty(SpagicConstants.CORRELATION_ID);
	            	if (correlationId == null) {
	            		log.warn("CorrelationId null. The client will not receive response");
	            		return;
	            	}
	    	    	IoSession session = correlatedSessions.get(correlationId);
	    	    	if (session == null) {
	    	    		throw new IOException("CorrelationID ["+correlationId+"] does not identify any TCP session");
	    	    	}
	    	    	if (!session.isConnected()) {
	    	    		log.warn("The tcp session is not connected The client will not receive response. " +
	    	    			"CorrelationId="+correlationId);
	    	    		removeCorrelatedSession(correlationId);
	    	    		return;
	    	    	}
					if (exchange.getStatus() == Status.Active) {
						
						// Check for errors
						if (exchange.getFault(false) != null) {
							log.warn("Received an error: no response is produced", exchange.getFault());
							return;
						}
						
						Message out = exchange.getOut(false);

						String xpathString = (config.isUseInNMEnvelope()) ? config
								.getInNmEnvelope()
								+ "/text()"
								: "text()";
						
						Object content = NMUtils.retrieveMessageInEnvelope(out,
								xpathString);
					
						log.debug("Sending response for correlationId: "
								+ correlationId + " msg: " + content);

						if (config.isBase64decode()) {
							try {
								session.write(Base64
										.decodeBase64((byte[]) content));
							} catch (ClassCastException cce) {
								log.error("Error writing response", cce);
							}
						} else {
							session.write(content);
						}
					} else if (exchange.getStatus() == Status.Error) {
						log.warn("Received an error: no response is produced");
						return;
					} else {
						log.warn("Received an unexpected state: no response is produced");
						return;
					}
				} finally {
			    	// terminate the exchange
			    	exchange.setStatus(Status.Done);
	    		}
	    	}
    	
    	}
    }
    
    

    private void addLogger( DefaultIoFilterChainBuilder chain ) throws Exception
    {
    	
        chain.addLast( "logger", new LoggingFilter() );
        log.info( "Logging ON" );
    }
    
    private void addSSLSupport( DefaultIoFilterChainBuilder chain )
            throws Exception
    {
    	SSLContext context = TCPBCSSLContextFactory
        			.getInstance( config );
        SSLFilter sslFilter = new SSLFilter( context );
        sslFilter.setNeedClientAuth(config.isUseSSLClientMode());
        
        // Create a SSLEngine to get the supported cipher suites
        SSLEngine engine = context.createSSLEngine();
        
        // Get enabled and supported cipher suites
        String[] supportedCipherSuites = engine.getSupportedCipherSuites();
        String[] enabledCipherSuites = engine.getEnabledCipherSuites();
        List<String> supportedCipherSuitesList = Arrays.asList(supportedCipherSuites);
        List<String> enabledCipherSuitesList = Arrays.asList(enabledCipherSuites);
        
        // Check if supported and enabled
        if (!enabledCipherSuitesList.contains(SSL_RSA_WITH_NULL_SHA)) {
        	// Enable it, if it's supported
        	if (supportedCipherSuitesList.contains(SSL_RSA_WITH_NULL_SHA)) {
        		String[] newEnabledCipherSuites = new String[enabledCipherSuitesList.size() + 1];
        		enabledCipherSuitesList.toArray(newEnabledCipherSuites);
        		newEnabledCipherSuites[enabledCipherSuitesList.size()] = SSL_RSA_WITH_NULL_SHA;
        		
        		sslFilter.setEnabledCipherSuites(newEnabledCipherSuites);
        		log.info("SSL_RSA_WITH_NULL_SHA enabled for TCP component");
        	} else {
        		log.warn("SSL_RSA_WITH_NULL_SHA not enabled and not supported for TCP component");
        	}
        }
        
        chain.addLast( "sslFilter", sslFilter );
        log.info( "SSL ON" );
    }    

	public TCPBCConfig getConfig() {
		return config;
	}

	public void setConfig(TCPBCConfig config) {
		this.config = config;
	}
	
	public synchronized void addCorrelatedSession(String id, IoSession session) {
		log.debug("Adding correlationID: "+id);
		correlatedSessions.put(id, session);
	}
	
	public synchronized void removeCorrelatedSession(String id) {
		log.debug("Removing correlationID: "+id);
		correlatedSessions.remove(id);
	}


}
