package org.spagic3.connectors.http;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.util.LazyList;
import org.spagic3.connectors.http.handlers.ServerStatusHandler;
import org.spagic3.connectors.http.servlet.SpagicServlet;
import org.spagic3.connectors.http.ssl.SslParameters;
import org.spagic3.core.ISpagicService;
import org.spagic3.core.PropertyConfigurator;

public class HttpServerManager {
	
	/**
     * The maximum number of threads for the Jetty thread pool. It's set to 255
     * by default to match the default value in Jetty.
     */
    private static int jettyThreadPoolSize = 255;

    /**
     * The maximum number of threads for the jetty client thread pool. It's set
     * to 16 to match the default value in Jetty.
     */
    private static  int jettyClientThreadPoolSize = 16;

    /**
     * Configuration to switch from shared jetty client for all
     * HttpProviderEndpoints to jetty client per HttpProviderEndpoint. It's
     * default value is false.
     */
    private static boolean jettyClientPerProvider;

    /**
     * Maximum number of concurrent requests to the same host.
     */
    private static int maxConnectionsPerHost = 65536;

    /**
     * Maximum number of concurrent requests.
     */
    private static int maxTotalConnections = 65536;

    /**
     * If true, use register jetty mbeans
     */
    private static boolean jettyManagement;

    /**
     * If the component is deployed in a web container and uses a servlet
     * instead of starting its own web server.
     */
    private static boolean managed;

 
    /**
     * Jetty connector max idle time (default value in jetty is 30000msec)
     **/
    private static int connectorMaxIdleTime = 30000;

    /**
     * HttpConsumerProcessor continuation suspend time (default value in
     * servicemix is 60000msec)
     */
    private static int consumerProcessorSuspendTime = 60000;

    /**
     * Number of times a given HTTP request will be tried until successful. If
     * streaming is enabled, the value will always be 0.
     */
    private static int retryCount = 0;

    /**
     * Proxy hostname. Component wide configuration, used either for http or
     * https connections. Can be overriden on a endpoint basis.
     */
    private static String proxyHost;

    /**
     * Proxy listening port. Component wide configuration, used either for http
     * or https connections. Can be overriden on a endpoint basis.
     */
    private static int proxyPort;
	
	
	
	private static ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<String, Server>();
	private static ConcurrentHashMap<String, org.spagic3.connectors.http.ssl.SslParameters> sslConfigurations = new ConcurrentHashMap<String, org.spagic3.connectors.http.ssl.SslParameters>();
	private static ConcurrentHashMap<String, ContextHandler> contextsMap = new ConcurrentHashMap<String, ContextHandler>();

	
	public static ContextHandler getContext(String locationUri){
		return contextsMap.get(locationUri);
	}
	
	public static Server configureServer(String locationURI, PropertyConfigurator serviceProperties, boolean isSsl, SslParameters sslParameters) throws Exception {
		// Check if there'is a server with key equals to the URL without the context part
		URL locationURL = new URL(locationURI);
	    String keyOfServerInMap = getServerKeyForMap(locationURL);
		Server server = HttpServerManager.servers.get(keyOfServerInMap);
	    
	    if (server == null) {
	        server = createHttpServer(locationURL, serviceProperties,isSsl, sslParameters);
	    } else {
	    	SslParameters storedSslParameters = sslConfigurations.get(keyOfServerInMap);
	    	if (storedSslParameters != null && !storedSslParameters.equals(sslParameters)) {
                throw new RuntimeException("An https server is already created on port " + locationURL.getPort()
                                + " but SSL parameters do not match");
            }
	    }
	    return server;
	}
	
	
	private static Server createHttpServer(URL locationURL, PropertyConfigurator serviceProperties, boolean isSsl, SslParameters sslParameters){
		
        // Create a new server
        Connector connector = createConnector(locationURL, serviceProperties, isSsl, sslParameters);
       
        Server server = new Server();
        server.setThreadPool(new QueuedThreadPool(jettyThreadPoolSize));
        server.setConnectors(new Connector[] {connector});
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {contexts, new ServerStatusHandler()});
        server.setHandler(handlers);

        String serverKey = getServerKeyForMap(locationURL);
        servers.put(serverKey, server);
        if (isSsl)
        	sslConfigurations.put(serverKey, sslParameters);
        return server;
	}
	
	
	
	public static Connector createConnector(URL locationURL, PropertyConfigurator serviceProperties, boolean isSsl, SslParameters sslParameters){
		Connector connector = null;
		
		if (isSsl)
	       connector = setupSslConnector(locationURL, sslParameters);
	    else 
	        connector = new SelectChannelConnector();
		
	    connector.setHost(locationURL.getHost());
	    connector.setPort(locationURL.getPort());
	    connector.setMaxIdleTime(connectorMaxIdleTime);
	    return connector;
	}

	private static Connector setupSslConnector(URL url, SslParameters ssl) {
		Connector connector;
		String keyStore = ssl.getKeyStore();
		if (keyStore == null) {
			keyStore = System.getProperty("javax.net.ssl.keyStore", "");
			if (keyStore == null) {
				throw new IllegalArgumentException(
                           "keyStore or system property javax.net.ssl.keyStore must be set");
			}	
		}
  
    String keyStorePassword = ssl.getKeyStorePassword();
    if (keyStorePassword == null) {
        keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyStorePassword == null) {
            throw new IllegalArgumentException(
                "keyStorePassword or system property javax.net.ssl.keyStorePassword must be set");
        }
    }
    SslSocketConnector sslConnector = new SslSocketConnector();
    sslConnector.setSslKeyManagerFactoryAlgorithm(ssl.getKeyManagerFactoryAlgorithm());
    sslConnector.setSslTrustManagerFactoryAlgorithm(ssl.getTrustManagerFactoryAlgorithm());
    sslConnector.setProtocol(ssl.getProtocol());
    sslConnector.setConfidentialPort(url.getPort());
    sslConnector.setPassword(ssl.getKeyStorePassword());
    sslConnector.setKeyPassword(ssl.getKeyPassword() != null ? ssl.getKeyPassword() : keyStorePassword);
    sslConnector.setKeystore(keyStore);
    sslConnector.setKeystoreType(ssl.getKeyStoreType());
    sslConnector.setNeedClientAuth(ssl.isNeedClientAuth());
    sslConnector.setWantClientAuth(ssl.isWantClientAuth());
    // important to set this values for selfsigned keys
    // otherwise the standard truststore of the jre is used
    sslConnector.setTruststore(ssl.getTrustStore());
    if (ssl.getTrustStorePassword() != null) {
        // check is necessary because if a null password is set
        // jetty would ask for a password on the comandline
        sslConnector.setTrustPassword(ssl.getTrustStorePassword());
    }
    sslConnector.setTruststoreType(ssl.getTrustStoreType());
    connector = sslConnector;
    return connector;
}
	
	public static ContextHandler configureContext(Server server, String locationURI, PropertyConfigurator serviceProperties, boolean isSsl, SslParameters sslParameters, ISpagicService spagicService) throws Exception {
		URL locationURL = new URL(locationURI);
		String path = locationURL.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String pathSlash = path + "/";
        // Check that context does not exist yet
        HandlerCollection handlerCollection = (HandlerCollection) server.getHandler();
        ContextHandlerCollection contexts = (ContextHandlerCollection) handlerCollection.getHandlers()[0];
        Handler[] handlers = contexts.getHandlers();
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                if (handlers[i] instanceof ContextHandler) {
                    ContextHandler h = (ContextHandler) handlers[i];
                    String handlerPath = h.getContextPath() + "/";
                    if (handlerPath.startsWith(pathSlash) || pathSlash.startsWith(handlerPath)) {
                        throw new RuntimeException("The requested context for path '" + path
                                        + "' overlaps with an existing context for path: '" + h.getContextPath() + "'");
                    }
                }
            }
        }
     // Create context
        ContextHandler context = new ContextHandler();
        context.setContextPath(path);
        ServletHolder holder = new ServletHolder();
        holder.setName("spagicServlet");
        holder.setClassName(SpagicServlet.class.getName());
        ServletHandler handler = new ServletHandler();
        handler.setServlets(new ServletHolder[] {holder});
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName("spagicServlet");
        mapping.setPathSpec("/*");
        handler.setServletMappings(new ServletMapping[] {mapping});
        context.setHandler(handler);
        
        context.setAttribute("spagicService", spagicService);
        
        contexts.addHandler(context);
        handler.initialize();
        contextsMap.put(locationURL.toString(), context);
        
        return context;
        
	}
	
	
	
	
	
	private static String getServerKeyForMap(URL url) {
	        String host = url.getHost();
	        try {
	            InetAddress addr = InetAddress.getByName(host);
	            if (addr.isAnyLocalAddress()) {
	                host = InetAddress.getLocalHost().getHostName();
	            }
	        } catch (UnknownHostException e) {
	            //unable to lookup host name, using IP address instead
	        }
	        return url.getProtocol() + "://" + host + ":" + url.getPort();
	}
	 
	public static void writeStatus(Writer writer) throws IOException {
		for (String serverUri : servers.keySet()) {
			Server server = servers.get(serverUri);

			Handler[] handlers = server
					.getChildHandlersByClass(ContextHandler.class);
			for (int i = 0; handlers != null && i < handlers.length; i++) {
				if (!(handlers[i] instanceof ContextHandler)) {
					continue;
				}
				ContextHandler context = (ContextHandler) handlers[i];
				StringBuffer sb = new StringBuffer();
				sb.append(serverUri);
				if (!context.getContextPath().startsWith("/")) {
					sb.append("/");
				}
				sb.append(context.getContextPath());
				if (!context.getContextPath().endsWith("/")) {
					sb.append("/");
				}
				if (context.isStarted()) {
					writer.write("<li><a href=\"");
					writer.write(sb.toString());
					writer.write("?wsdl\">");
					writer.write(sb.toString());
					writer.write("</a></li>\n");
				} else {
					writer.write("<li>");
					writer.write(sb.toString());
					writer.write(" [Stopped]</li>\n");
				}
			}
		}

		for (int i = 0; i < 10; i++) {
			writer.write("\n<!-- Padding for IE                  -->");
		}
	}

	public static void unconfigureContext(String locationUri) throws Exception {
		
		ContextHandler context = contextsMap.get(locationUri);
		context.stop();
		
        for (Iterator<Server> it = servers.values().iterator(); it.hasNext();) {
            Server server = it.next();
            HandlerCollection handlerCollection = (HandlerCollection) server.getHandler();
            ContextHandlerCollection contexts = (ContextHandlerCollection) handlerCollection.getHandlers()[0];
            Handler[] handlers = contexts.getHandlers();
            if (handlers != null && handlers.length > 0) {
                contexts.setHandlers((Handler[]) LazyList.removeFromArray(handlers, context));
            }
            
            if (contexts.getHandlers().length == 0){
            	server.stop();
            	server.join();
            	Connector[] connectors = server.getConnectors();
                for (int i = 0; i < connectors.length; i++) {
                    if (connectors[i] instanceof AbstractConnector) {
                        ((AbstractConnector) connectors[i]).join();
                    }
                }
            }
            
        }
	}
	
}
