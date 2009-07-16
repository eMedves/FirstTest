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
package org.apache.servicemix.soap.interceptors.nmr;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.api.model.Operation;
import org.apache.servicemix.soap.bindings.soap.SoapFault;
import org.apache.servicemix.soap.core.AbstractInterceptor;
import org.apache.servicemix.soap.util.QNameUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.dom4j.io.STAXEventReader;
import org.spagic3.constants.SpagicConstants;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.SpagicUtils;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
 */
public class NmrInInterceptor extends AbstractInterceptor {

    public static final String OPERATION_MEP = "MEP";
    
    private final boolean server;
    
    public NmrInInterceptor(boolean server) {
        this.server = server;
    }
    
    public void handleMessage(Message message)  {
    	try{
            Operation operation = message.get(Operation.class);
            Exchange exchange;
            org.apache.servicemix.nmr.api.Message nm;
            // Create message
            if (server) {
                exchange = createExchange(message);
                if (operation != null) {
                    exchange.setOperation(operation.getName());
                }
                nm = exchange.getIn();
                message.setContent(Exchange.class, exchange);
            } else {
                exchange = message.getContent(Exchange.class);
                if (exchange == null) {
                    throw new IllegalStateException("Content of type " + Exchange.class + " not found on message");
                }
                if (message.getContent(Exception.class) == null) {
                    nm = exchange.getOut(true);
                } else {
                    exchange.setFault(exchange.getFault(true));
                    nm = exchange.getFault();
                }
            }
            // Put headers
            toNMSHeaders(nm, message);
            // Put attachments
            toNMSAttachments(nm, message);
            // Put subject
            nm.setSecuritySubject(message.get(Subject.class));
            // Put main source
            getContent(nm, message);
            // Register new content
            message.setContent(Message.class, nm);
    	}catch (Exception e) {
			throw new RuntimeException(e);
		}
        
    }

    /**
     * Create the JBI exchange
     */
    private Exchange createExchange(Message message) throws Exception {
        URI mep;
        Operation operation = message.get(Operation.class);
        if (operation != null) {
            mep = operation.getMep();
        } else {
            mep = (URI) message.get(OPERATION_MEP);
        }
        if (mep == null) {
            throw new NullPointerException("MEP not found");
        }
        
        String sender = (String)message.get(SpagicConstants.SPAGIC_SENDER);
        String target = (String)message.get(SpagicConstants.SPAGIC_TARGET);
        Exchange exchange = null;
        if (mep.equals(SpagicConstants.IN_ONLY_URI)){
        	return ExchangeUtils.createExchange(sender, target, Pattern.InOnly);
        }else if (mep.equals(SpagicConstants.IN_OUT_URI)){
        	return ExchangeUtils.createExchange(sender, target, Pattern.InOut);
        }
        
        if (exchange == null)
        	throw new RuntimeException("Unable to Create Exchange");
        
        return exchange;
        
    }

    /**
     * Convert SoapMessage headers to NormalizedMessage headers
     */
    private void toNMSHeaders(org.apache.servicemix.nmr.api.Message normalizedMessage, Message soapMessage) {
        Map<String, Object> headers = new HashMap<String, Object>();
        for (Map.Entry<QName, DocumentFragment> entry : soapMessage.getSoapHeaders().entrySet()) {
            headers.put(QNameUtil.toString(entry.getKey()), entry.getValue());
        }
        headers.putAll(soapMessage.getTransportHeaders());

        normalizedMessage.setHeader(SpagicConstants.PROTOCOL_HEADERS, headers);
    }

    /**
     * Convert SoapMessage attachments to NormalizedMessage attachments
     */
    private void toNMSAttachments(org.apache.servicemix.nmr.api.Message normalizedMessage, Message soapMessage) {
        for (Map.Entry<String, DataHandler> entry : soapMessage.getAttachments().entrySet()) {
            normalizedMessage.addAttachment(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Extract the content as a jaxp Source
     */
    private void getContent(org.apache.servicemix.nmr.api.Message normalizedMessage, Message message)  {
        Exception e = message.getContent(Exception.class);
        if (e == null) {     	
        	Source source = message.getContent(Source.class);
        	normalizedMessage.setBody(SpagicUtils.toString(source));
        } else if (e instanceof SoapFault) {
            SoapFault fault = (SoapFault) e;
            normalizedMessage.setBody(fault.getDetails());
            normalizedMessage.setHeader(SpagicConstants.SOAP_FAULT_CODE, fault.getCode());
            normalizedMessage.setHeader(SpagicConstants.SOAP_FAULT_NODE, fault.getNode());
            normalizedMessage.setHeader(SpagicConstants.SOAP_FAULT_REASON, fault.getReason());
            normalizedMessage.setHeader(SpagicConstants.SOAP_FAULT_ROLE, fault.getRole());
            normalizedMessage.setHeader(SpagicConstants.SOAP_FAULT_SUBCODE, fault.getSubcode());
        }
    }

}
