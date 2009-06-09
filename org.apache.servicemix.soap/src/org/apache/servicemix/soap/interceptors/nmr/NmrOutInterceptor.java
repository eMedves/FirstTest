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
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.common.util.DOM4JUtils;
import org.apache.servicemix.common.util.DOMUtil;
import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.soap.api.Fault;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.api.model.Binding;
import org.apache.servicemix.soap.api.model.Operation;
import org.apache.servicemix.soap.core.AbstractInterceptor;
import org.apache.servicemix.soap.util.QNameUtil;
import org.apache.servicemix.soap.util.stax.StaxSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentSource;
import org.spagic3.core.SpagicConstants;
import org.w3c.dom.DocumentFragment;

/**
 * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
 */
public class NmrOutInterceptor extends AbstractInterceptor {

    private final boolean server;
    
    public NmrOutInterceptor(boolean server) {
        this.server = server;
    }
    
    public void handleMessage(Message message) {
        try{
    	org.apache.servicemix.nmr.api.Message nm = message.getContent(org.apache.servicemix.nmr.api.Message.class);

        Document doc = DocumentHelper.parseText((String)nm.getBody());
       
        message.setContent(Document.class,  doc);
        fromNMSAttachments(message, nm);
        fromNMSHeaders(message, nm);

        if (!server) {
            Exchange me = message.getContent(Exchange.class);
            Binding binding = message.get(Binding.class);
            Operation operation = binding.getOperation(me.getOperation());
            if (operation != null) {
                if (!areMepsEquals(me.getPattern(), operation.getMep())) {
                    throw new Fault("Received incorrect exchange mep.  Received " + me.getPattern()
                                    + " but expected " + operation.getMep() + " for operation "
                                    + operation.getName());
                }
                message.put(Operation.class, operation);
                message.put(org.apache.servicemix.soap.api.model.Message.class,
                            server ? operation.getOutput() : operation.getInput());
            }
        }
        }catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    
    private URI getURIMep(Pattern mepPattern){
    	if (mepPattern == Pattern.InOnly)
    		return SpagicConstants.IN_ONLY_URI;
    	else if (mepPattern == Pattern.InOut)
    		return SpagicConstants.IN_OUT_URI;
    	else
    		return null;
    }
    private boolean areMepsEquals(Pattern mepPattern, URI mep2) {
        
    	URI mep1 = getURIMep(mepPattern);
    	if (mep1 == null)
    		return false;
    	
    	String s1 = mep1 != null ? mep1.toString() : "";
        String s2 = mep2 != null ? mep2.toString() : "";
        int i1 = s1.lastIndexOf('/');
        int i2 = s2.lastIndexOf('/');
        if (i1 >= 0 && i2 >= 0) {
            return s1.substring(i1).equals(s2.substring(i2));
        }
        return false;
    }

    /**
     * Copy NormalizedMessage attachments to SoapMessage attachments
     */
    private void fromNMSAttachments(Message message, org.apache.servicemix.nmr.api.Message normalizedMessage) {
        
    	Map<String, Object> attachmentMap = normalizedMessage.getAttachments();
        
        for ( String attachmentId : attachmentMap.keySet()){
        	 DataHandler handler = (DataHandler)attachmentMap.get(attachmentId);
             message.getAttachments().put(attachmentId, handler);
        }
        
    }

    /**
     * Copy NormalizedMessage headers to SoapMessage headers
     */
    @SuppressWarnings("unchecked")
    private void fromNMSHeaders(Message message, org.apache.servicemix.nmr.api.Message normalizedMessage) {
        if (normalizedMessage.getHeader(SpagicConstants.PROTOCOL_HEADERS) != null) {
            Map<String, ?> headers = (Map<String, ?>) normalizedMessage.getHeader(SpagicConstants.PROTOCOL_HEADERS);
            for (Map.Entry<String, ?> entry : headers.entrySet()) {
                QName name = QNameUtil.parse(entry.getKey());
                if (name != null) {
                    message.getSoapHeaders().put(name, (DocumentFragment) entry.getValue());
                } else {
                    message.getTransportHeaders().put(entry.getKey(), (String) entry.getValue());
                }
            }
        }
    }

}
