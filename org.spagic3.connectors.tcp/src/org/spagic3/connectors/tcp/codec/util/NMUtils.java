/**

    Copyright 2007, 2008 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
**/
package org.spagic3.connectors.tcp.codec.util;

import org.apache.servicemix.nmr.api.Message;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.xpath.DefaultXPath;

public class NMUtils {
    
    public static Object retrieveMessageInEnvelope(Message in, String xpathString)
   		throws Exception {
    	
    	String body = in.getBody(String.class);
   
    	Document doc = DocumentHelper.parseText(body);
    	
    	org.dom4j.XPath xPath  = new DefaultXPath(xpathString);
    
    	String nmEnvelope = xPath.valueOf(doc);
    	if (nmEnvelope == null) {
    		throw new Exception("Normalize message envelope ("+xpathString+") not found in Input NM");
    	}
    	return nmEnvelope.getBytes();

    }
}
