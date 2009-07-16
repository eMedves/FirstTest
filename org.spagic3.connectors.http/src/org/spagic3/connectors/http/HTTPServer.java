package org.spagic3.connectors.http;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.apache.servicemix.soap.wsdl.BindingFactory;
import org.apache.servicemix.soap.wsdl.WSDLUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.spagic3.connectors.http.adapters.IHTTPInputProtocolAdapter;
import org.spagic3.connectors.http.adapters.PlainHTTPInputProtocolAdapter;
import org.spagic3.connectors.http.adapters.SOAPInputProtocolAdapter;
import org.spagic3.connectors.http.ssl.SslParameters;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.AbstractSpagicConnector;
import org.spagic3.core.PropertyConfigurator;
import org.spagic3.core.SpagicUtils;
import org.spagic3.core.resources.IResource;



public class HTTPServer extends AbstractSpagicConnector {
	
	
	private String locationURI = null;
	private boolean ssl = false; 
	private SslParameters sslParameters = null;
	private ExchangeContinuationsTracker exchangeContinuationsTracker = new ExchangeContinuationsTracker();
	private String mep = null;
	private long timeout = -1;
	private boolean isSoap = false;
	private IResource wsdl = null;
	
	private QName service = null;
	private String port = null;

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public QName getService() {
		return service;
	}

	public void setService(QName service) {
		this.service = service;
	}

	private IHTTPInputProtocolAdapter adapter = null; 
	
	public boolean isSoap() {
		return isSoap;
	}

	public void setSoap(boolean isSoap) {
		this.isSoap = isSoap;
	}

	
	
	public void init(){
		System.out.println("-- HTTP Server Component Init --");
		this.locationURI = propertyConfigurator.getString("locationURI");
		this.timeout = propertyConfigurator.getLong("timeout", (long)30000);
		this.mep = propertyConfigurator.getString("mep", SpagicConstants.IN_OUT_MEP);
		this.isSoap = propertyConfigurator.getBoolean("isSoap", false);
		
		
		this.sslParameters = getSslParameters(this.propertyConfigurator);
		
		if (!isSoap){
			this.adapter = new PlainHTTPInputProtocolAdapter();
		}else{
			this.adapter = new SOAPInputProtocolAdapter();
			this.wsdl = propertyConfigurator.getResource("wsdl");
		}
		validate();
	}
	
	
	private void validate() {
		if (isSoap) {
			if (wsdl == null) {
				throw new RuntimeException(
						"If HttpServer service is a SOAP Service wsdl property must be set");
			}
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
					throw new RuntimeException("Unrecognized wsdl namespace: "
							+ rootElement.getNamespaceURI());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
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
    		
    		Definition def = reader.readWSDL(wsdl.asURL().toString());
    		
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
                soapAddress.setLocationURI(this.locationURI);
            } else {
                SOAP12Address soap12Address = WSDLUtils.getExtension(port, SOAP12Address.class);
                if (soap12Address != null) {
                    soap12Address.setLocationURI(this.locationURI);
                }
            }
            ((SOAPInputProtocolAdapter)this.adapter).setBinding(BindingFactory.createBinding(port));
        }catch (WSDLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	@Override
	public void start() throws Exception {
			
			boolean isSsl = isSslConfigured();
			
			Server server = HttpServerManager.configureServer(this.locationURI, this.propertyConfigurator, isSsl, sslParameters);
			ClassLoader bundleClassLoader = this.getClass().getClassLoader();
			ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(bundleClassLoader);
			if (!server.isStarted()){
		    	server.start();
		    }
			ContextHandler context = HttpServerManager.configureContext(server, this.locationURI, this.propertyConfigurator, isSsl, sslParameters, this);
		    
			if (!context.isStarted()){
		    	context.start();
			}
			Thread.currentThread().setContextClassLoader(oldClassLoader);
	}
	
	@Override
	public void stop() throws Exception {
		
		
			HttpServerManager.unconfigureContext(this.locationURI);
			
	       
	}
	
	private boolean isSslConfigured() throws Exception {
		URL locationURL = new URL(this.locationURI);
		boolean isSsl = false;
        if (locationURL.getProtocol().equals("https")) {
            if (sslParameters == null) {
                throw new IllegalArgumentException("https require SSL Properties");
            }
            isSsl = true;
        } else if (!locationURL.getProtocol().equals("http")) {
            throw new UnsupportedOperationException("Only http and https Protocol Are supported");
        } 
        return isSsl;
	}

	private static SslParameters getSslParameters(PropertyConfigurator serviceProperties){
		return null;
	}
	
	
	public void processHttp(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try{
			Continuation continuation = ContinuationSupport.getContinuation(request, null);
			exchangeContinuationsTracker.handle(request, response, continuation, this.timeout, this);
		
		} catch (RetryRequest e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try{
				responseError(null, e, request, response);
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		exchangeContinuationsTracker.exchangeArrived(exchange);
	}
	
	
	public Exchange createExchange(HttpServletRequest request) throws Exception {
		return adapter.createExchange(request, mep, getSpagicId(), this.target);
    }

    public void responseAccepted(Exchange exchange, HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
    	 adapter.sendAccepted(exchange, request, response);
    }

    public void responseError(Exchange exchange, Exception error, HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
    	adapter.sendError(null,error, request, response);
    		
    }

    public void responseFault(Exchange exchange, Message fault, HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
    	adapter.sendFault(exchange, fault, request, response);
    }

    public void responseOut(Exchange exchange, Message outMsg, HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
    	adapter.sendOut(exchange, outMsg, request, response);
    	
    }

    
}
