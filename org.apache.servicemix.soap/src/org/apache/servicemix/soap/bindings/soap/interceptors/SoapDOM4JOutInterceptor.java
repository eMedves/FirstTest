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
package org.apache.servicemix.soap.bindings.soap.interceptors;

import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.servicemix.soap.api.Fault;
import org.apache.servicemix.soap.api.InterceptorChain;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.bindings.soap.SoapVersion;
import org.apache.servicemix.soap.core.AbstractInterceptor;
import org.apache.servicemix.soap.util.DomUtil;
import org.apache.servicemix.soap.util.stax.DOMStreamReader;
import org.apache.servicemix.soap.util.stax.StaxUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
 */
public class SoapDOM4JOutInterceptor extends AbstractInterceptor {

    private final SoapVersion soapVersion;
    
    public SoapDOM4JOutInterceptor() {
        this(null);
    }
    
    public SoapDOM4JOutInterceptor(SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
    }
    
    public void handleMessage(Message message) {
        OutputStream os = message.getContent(OutputStream.class);
       
        if (os == null) {
            throw new NullPointerException("XMLStreamWriter content not found");
        }
        SoapVersion soapVersion = message.get(SoapVersion.class); 
        if (soapVersion == null) {
            soapVersion = this.soapVersion;
            if (soapVersion == null) {
                throw new IllegalStateException("No soap version specified");
            }
        }

        try {
        	 XMLWriter writer = new XMLWriter(os);
        	Document doc = DocumentHelper.createDocument();
        	
        	Namespace soapNameSpace = new Namespace(soapVersion.getPrefix(), soapVersion.getNamespace());
        	
        	QName soapEnvelope = new QName(soapVersion.getEnvelope().getLocalPart(), soapNameSpace); 
        	org.dom4j.Element soapEnvelopeNode = doc.addElement(soapEnvelope);
            
            if (message.getSoapHeaders().size() > 0) {
                for (Iterator it = message.getSoapHeaders().values().iterator(); it.hasNext();) {
                    //DocumentFragment df = (DocumentFragment) it.next();
                    //Element e = DomUtil.getFirstChildElement(df);
                }
                
            }
            QName soapBody = new QName(soapVersion.getBody().getLocalPart(), soapNameSpace);
            org.dom4j.Element soapBodyNode =  soapEnvelopeNode.addElement(soapBody);
            
            
            Document nmDoc = message.getContent(Document.class);
            org.dom4j.Element bodyEl = nmDoc.getRootElement();
            bodyEl.detach();
            soapBodyNode.add(bodyEl);
            writer.write( doc );          
            writer.flush();
        } catch (Exception e) {
            throw new Fault(e);
        }
    }
    
    
}
