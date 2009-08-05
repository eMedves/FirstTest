package org.spagic3.connectors.http;

import java.io.IOException;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.api.Status;
import org.apache.servicemix.soap.bindings.soap.Soap11;
import org.apache.servicemix.soap.bindings.soap.impl.Wsdl1SoapBindingImpl;
import org.apache.servicemix.soap.wsdl.BindingFactory;
import org.apache.servicemix.soap.wsdl.WSDLUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mortbay.jetty.client.Address;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.security.ProxyAuthorization;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.connectors.http.adapters.IHTTPOutputProtocolAdapter;
import org.spagic3.connectors.http.adapters.PlainHTTPOutputProtocolAdapter;
import org.spagic3.connectors.http.adapters.SOAPInputProtocolAdapter;
import org.spagic3.connectors.http.adapters.SOAPProtocolOutputAdapter;
import org.spagic3.connectors.http.ssl.SslParameters;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.AbstractSpagicConnector;
import org.spagic3.core.PropertyConfigurator;
import org.spagic3.core.SpagicUtils;
import org.spagic3.core.resources.IResource;

public class HTTPClient extends AbstractSpagicConnector{
	
	protected Logger logger = LoggerFactory.getLogger(HTTPClient.class);
	
	private String locationURI = null;
	private boolean ssl = false; 
	private SslParameters sslParameters = null;
	private IResource wsdl;
	private QName service = null;
	private String port = null;
	
	private long clientTimeout = 60000;
    private HttpClient jettyClient;
   
    private String mep = null;
    
    private int jettyClientThreadPoolSize = 16;
    
    // PROXY
    private String proxyHost;
    private int proxyPort = 80;
    private String proxyUsername;
    private String proxyPassword;
    
    private boolean isSoap = false;
    private boolean isPipeline = false;
    private IHTTPOutputProtocolAdapter protocolAdapter = null;
   
    public void init(){
		System.out.println("-- HTTP Cliebt Component Init --");
		this.locationURI = propertyConfigurator.getString("locationURI");
		this.clientTimeout = propertyConfigurator.getLong("timeout", (long)60000);
		this.mep = propertyConfigurator.getString("mep", SpagicConstants.IN_OUT_MEP);
		this.isSoap = propertyConfigurator.getBoolean("isSoap", false);
		
		
		this.sslParameters = getSslParameters(this.propertyConfigurator);
		
		if (!isSoap){
			this.protocolAdapter = new PlainHTTPOutputProtocolAdapter(); 
		}else{
			this.protocolAdapter = new SOAPProtocolOutputAdapter();
			this.wsdl = propertyConfigurator.getResource("wsdl", null);
		}
		validate();
		
	}

	private void validate() {
		if (isSoap) {
			if (wsdl != null) {

				try {
					SAXReader reader = new SAXReader();
					Document wsdlDocument = reader.read(wsdl.openStream());
					Element rootElement = wsdlDocument.getRootElement();
					if (WSDLUtils.WSDL1_NAMESPACE.equals(rootElement
							.getNamespaceURI())) {
						//
						// It's a WSDL 1 Namespace
						//
						checkWsdl11();
					} else if (WSDLUtils.WSDL2_NAMESPACE.equals(rootElement
							.getNamespaceURI())) {
						//
						// It's a WSDL2 Namespace
						//
						checkWsdl2();
					} else {
						throw new RuntimeException(
								"Unrecognized wsdl namespace: "
										+ rootElement.getNamespaceURI());
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				((SOAPProtocolOutputAdapter) this.protocolAdapter)
						.setBinding(new Wsdl1SoapBindingImpl(Soap11
								.getInstance()));
				((SOAPProtocolOutputAdapter) this.protocolAdapter).setLocationURI(this.locationURI);
			}
		}
	}
    
    
    public void checkWsdl2(){
		// USE WOODEN
	}
	public void checkWsdl11(){
        try{
        	WSDLFactory wsdlFactory = WSDLFactory.newInstance();
    		WSDLReader reader = wsdlFactory.newWSDLReader();
    		Definition def = reader.readWSDL(wsdl.toString());
    		
    		/*
    		WSIBPValidator validator = new WSIBPValidator(def);
            if (!validator.isValid()) {
                throw new RuntimeException("WSDL is not WS-I BP compliant: " + validator.getErrors());
            } 
            */ 
            Service svc = null;
            if (getService() != null) {
                svc = def.getService(getService());
                if (svc == null) {
                    throw new RuntimeException("Could not find service '" + getService() + "' in wsdl");
                }
            } else if (def.getServices().size() == 1) {
                svc = (Service)def.getServices().values().iterator().next();
                setService(svc.getQName());
            } else {
                throw new RuntimeException("If service is not set, the WSDL must contain a single service definition");
            }
            Port port;
            if (getPort() != null) {
                port = svc.getPort(getPort());
                if (port == null) {
                    throw new RuntimeException("Cound not find port '" + getPort()
                                                  + "' in wsdl for service '" + getService() + "'");
                }
            } else if (svc.getPorts().size() == 1) {
                port = (Port)svc.getPorts().values().iterator().next();
                setPort(port.getName());
            } else {
                throw new RuntimeException("If endpoint is not set, the WSDL service '" + getService()
                                              + "' must contain a single port definition");
            }
            SOAPAddress soapAddress = WSDLUtils.getExtension(port, SOAPAddress.class);
            if (soapAddress != null) {
            	((SOAPProtocolOutputAdapter)this.protocolAdapter).setLocationURI(this.locationURI);
            } else {
                SOAP12Address soap12Address = WSDLUtils.getExtension(port, SOAP12Address.class);
                if (soap12Address != null) {
                	((SOAPProtocolOutputAdapter)this.protocolAdapter).setLocationURI(this.locationURI);
                }
            }
            ((SOAPProtocolOutputAdapter)this.protocolAdapter).setBinding(BindingFactory.createBinding(port));
           
        }catch (WSDLException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void start() throws Exception {
		
		jettyClient = new HttpClient();
		
		if (ssl){
			jettyClient.setKeyStoreLocation(sslParameters.getKeyStore());
			jettyClient.setKeyStorePassword(sslParameters.getKeyStorePassword());
		
			jettyClient.setTrustStoreLocation(sslParameters.getTrustStore());
			jettyClient.setTrustStorePassword(sslParameters.getTrustStorePassword());
		}
		
        jettyClient.setThreadPool(new QueuedThreadPool( jettyClientThreadPoolSize));
        jettyClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        
        if (proxyHost != null) {
            jettyClient.setProxy(new Address(proxyHost, proxyPort));
            if (proxyUsername != null) {
                jettyClient.setProxyAuthentication(new ProxyAuthorization(proxyUsername, proxyPassword));
            }
        }
        jettyClient.setSoTimeout((int)this.clientTimeout);
        
		
		if (this.jettyClient != null)
			this.jettyClient.start();
		
	}
	
	
	@Override
	public void stop() throws Exception {
		if (this.jettyClient != null)
			jettyClient.stop();
		
	}
	@Override
	public void process(Exchange exchange) {
		if (exchange.getStatus() == Status.Active) { 
			if (isPipeline && exchange.getPattern() != Pattern.InOnly){
				throw new RuntimeException("If isPipeline flag is true exchange must be an InOnly Exchange");
			}
		 
	        Message nm = exchange.getIn(false);
	        if (nm == null) {
	                throw new IllegalStateException("Exchange has no input message");
	        }
	            
	            
	        SpagicJettyHTTPExchange spagicJettyHTTPExchange = new NMREchangeAwareSpagicJettyExchange(exchange);
	        protocolAdapter.fillJettyExchange(exchange, spagicJettyHTTPExchange);
	        
	        try{
	        	this.jettyClient.send(spagicJettyHTTPExchange);
	        }catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
	    }

		
	}
	
	private static SslParameters getSslParameters(PropertyConfigurator serviceProperties){
		return null;
	}
	
	protected void handleResponse(SpagicJettyHTTPExchange spagicJettyContentExchange, Exchange nmrExchange) {
        try {
            protocolAdapter.handleResponse(nmrExchange,spagicJettyContentExchange);
        } catch (Exception e) {
            nmrExchange.setError(e);
        }
        configureForResponse(nmrExchange);
        if (nmrExchange.getPattern() == Pattern.InOut){
        	send(nmrExchange);
        }else{
        	// It was an InOnly Exchange send back the done
        	nmrExchange.setStatus(Status.Done);
        	send(nmrExchange);
        }
        
       
    }

    protected void handleException(SpagicJettyHTTPExchange spagicJettyContentExchange, Exchange nmrExchange, Throwable ex)  {
    	try {
            protocolAdapter.handleException(nmrExchange,spagicJettyContentExchange,ex);
        } catch (Exception e) {
            nmrExchange.setError(e);
        }
       
        configureForResponse(nmrExchange);
        if (nmrExchange.getPattern() == Pattern.InOut){
        	send(nmrExchange);
        }else{
        	// It was an InOnly Exchange send back the done
        	nmrExchange.setStatus(Status.Done);
        	send(nmrExchange);
        }
    }

    
    protected class NMREchangeAwareSpagicJettyExchange extends SpagicJettyHTTPExchange {
        private Exchange nmrExchange;

        public NMREchangeAwareSpagicJettyExchange(Exchange nmrExchange) {
            this.nmrExchange = nmrExchange;
        }

        protected void onResponseComplete() throws IOException {
            handleResponse(this, nmrExchange);
        }

        protected void onConnectionFailed(Throwable throwable) {
            handleException(this, nmrExchange, throwable);
        }

        protected void onException(Throwable throwable) {
            handleException(this, nmrExchange, throwable);
        }
    }
    
    public QName getService() {
		return service;
	}
	public void setService(QName service) {
		this.service = service;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	
    

}
