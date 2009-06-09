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
package org.apache.servicemix.common.util;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class URIResolver {

    /**
     * The uri to resolve
     */
    private String uri;

    public URIResolver() {
    }

    public URIResolver(String uri) {
        this.uri = uri;
    }

    

    

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public static DocumentFragment createWSAEPR(String uri) {
        Document doc;
        try {
            doc = DOMUtil.newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DocumentFragment epr = doc.createDocumentFragment();
        Element root = doc.createElement("epr");
        Element address = doc.createElementNS(WSAddressingConstants.WSA_NAMESPACE_200508,
                                              WSAddressingConstants.WSA_PREFIX + ":" + WSAddressingConstants.EL_ADDRESS);
        Text txt = doc.createTextNode(uri);
        address.appendChild(txt);
        root.appendChild(address);
        epr.appendChild(root);
        return epr;
    }

   

    public static String[] split3(String uri) {
        char sep;
        uri = uri.trim();
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        int idx2 = uri.lastIndexOf(sep, idx1 - 1);
        if (idx1 < 0 || idx2 < 0) {
            throw new IllegalArgumentException("Bad syntax: expected [part0][sep][part1][sep][part2]");
        }
        String epName = uri.substring(idx1 + 1);
        String svcName = uri.substring(idx2 + 1, idx1);
        String nsUri   = uri.substring(0, idx2);
        return new String[] {nsUri, svcName, epName };
    }

    public static String[] split2(String uri) {
        char sep;
        uri = uri.trim();
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        if (idx1 < 0) {
            throw new IllegalArgumentException("Bad syntax: expected [part0][sep][part1]");
        }
        String svcName = uri.substring(idx1 + 1);
        String nsUri   = uri.substring(0, idx1);
        return new String[] {nsUri, svcName };
    }

}
