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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.soap.api.Fault;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.api.model.Operation;
import org.apache.servicemix.soap.bindings.soap.SoapFault;
import org.apache.servicemix.soap.bindings.soap.model.wsdl1.Wsdl1SoapBinding;
import org.apache.servicemix.soap.bindings.soap.model.wsdl1.Wsdl1SoapMessage;
import org.apache.servicemix.soap.bindings.soap.model.wsdl1.Wsdl1SoapOperation;
import org.apache.servicemix.soap.bindings.soap.model.wsdl1.Wsdl1SoapPart;
import org.apache.servicemix.soap.core.AbstractInterceptor;
import org.apache.servicemix.soap.util.DomUtil;
import org.apache.servicemix.soap.util.QNameUtil;
import org.apache.servicemix.soap.util.stax.StaxUtil;
import org.apache.servicemix.soap.util.stax.StaxSource;
import org.apache.servicemix.soap.util.stax.DOMStreamReader;
import org.apache.servicemix.soap.util.stax.FragmentStreamReader;
import org.apache.servicemix.soap.util.stax.ExtendedXMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
 */
public class NmrWSDL1Interceptor extends AbstractInterceptor {

    private final boolean server;
    
    public NmrWSDL1Interceptor(boolean server) {
        this.server = server;
    }
    
    public void handleMessage(Message message) {
        // Ignore faults messages
        if (message.getContent(Exception.class) != null) {
            return;
        }
        XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);
        if (xmlReader != null) {
                message.setContent(Source.class, StaxUtil.createSource(xmlReader));
        }
        return;
    }

    protected Wsdl1SoapOperation getOperation(Message message) {
        Operation operation = message.get(Operation.class);
        if (operation == null) {
            throw new Fault("Operation not bound on this message");
        }
        if (operation instanceof Wsdl1SoapOperation == false) {
            throw new Fault("Message is not bound to a WSDL 1.1 SOAP operation");
        }
        return (Wsdl1SoapOperation) operation;
    }

    /**
     * Extract the content as a jaxp Source
     */
    protected Element getBodyElement(Message message) {
        XMLStreamReader xmlReader = message.getContent(XMLStreamReader.class);
        return xmlReader != null ? StaxUtil.createElement(xmlReader) : null; 
    }
}
