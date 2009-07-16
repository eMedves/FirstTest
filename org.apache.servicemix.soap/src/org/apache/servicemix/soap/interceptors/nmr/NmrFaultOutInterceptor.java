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

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.common.util.DOM4JUtils;
import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.bindings.soap.SoapFault;
import org.apache.servicemix.soap.core.AbstractInterceptor;
import org.spagic3.constants.SpagicConstants;

/**
 * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
 */
public class NmrFaultOutInterceptor extends AbstractInterceptor {

    public void handleMessage(Message message) {
        /*
    	org.apache.servicemix.nmr.api.Message nm = message.getContent(org.apache.servicemix.nmr.api.Message.class);
        boolean isFault = false;
        if ( isFault )
            SoapFault fault = createFault(nm);
            throw fault;
        }
        */
    }

    private SoapFault createFault(org.apache.servicemix.nmr.api.Message nm) throws Exception {
    	Source src = DOM4JUtils.getDOM4JDocumentSource(nm);
        
        QName code = (QName) nm.getHeader(SpagicConstants.SOAP_FAULT_CODE);
        QName subcode = (QName) nm.getHeader(SpagicConstants.SOAP_FAULT_SUBCODE);
        String reason = (String) nm.getHeader(SpagicConstants.SOAP_FAULT_REASON);
        URI node = (URI) nm.getHeader(SpagicConstants.SOAP_FAULT_NODE);
        URI role = (URI) nm.getHeader(SpagicConstants.SOAP_FAULT_ROLE);
        SoapFault fault = new SoapFault(code, subcode, reason, node, role, src);
        return fault;
    }

}
