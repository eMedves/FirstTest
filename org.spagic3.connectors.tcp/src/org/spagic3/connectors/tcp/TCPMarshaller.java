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
package org.spagic3.connectors.tcp;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.codec.binary.Base64;
import org.apache.mina.common.ByteBuffer;
import org.apache.servicemix.nmr.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPMarshaller {
	
	private static final Logger log = LoggerFactory.getLogger(TCPMarshaller.class);
    private TCPBCConfig config;
    
   
    public TCPMarshaller(TCPBCConfig cfg) {
    	config = cfg;
    }
	
    /**
     * 
     * 
     * @param exchange
     * @param nms
     * @param tcpMsg
     * @throws JBIException
     */
	public void toNMS(Message nms,
		ByteBuffer tcpMsg)
		throws Exception {

		StringBuffer strExchange = new StringBuffer();
		if (config.isUseOutNMEnvelope()) {
			strExchange.append("<")
			.append(config.getOutNmEnvelope()).append(" base64encoded=\"")
			.append(config.isBase64encode()).append("\" >");
		}
		strExchange.append("<![CDATA[");

//		byte[] bytesTcpMsg = new byte[tcpMsg.limit()];
//		tcpMsg.get(bytesTcpMsg);
//		byte[] encoded = Base64.encodeBase64(bytesTcpMsg);
		
		try {
			if (config.isBase64encode()) {
				byte[] bytesTcpMsg = new byte[tcpMsg.limit()];
				tcpMsg.get(bytesTcpMsg);
				byte[] encoded = Base64.encodeBase64(bytesTcpMsg);

				strExchange.append(new String(encoded));
			} else {
				strExchange.append(tcpMsg.getString(Charset.forName("UTF-8").newDecoder()));
			}
			strExchange.append("]]>");
		} catch (UnsupportedCharsetException uce) {
			log.error("Error converting incoming message to UTF-8", uce);
		} catch (CharacterCodingException cce) {
			log.error("Error converting incoming message to UTF-8", cce);
		}

		if (config.isUseOutNMEnvelope()) {
			strExchange.append("</").append(config.getOutNmEnvelope()).append(">");
		}

		log.debug("toNMS -> ["+strExchange.toString()+"]");
		nms.setBody(strExchange.toString());
	}
	
	/*
	public void toTCPMessage(MessageExchange exchange, NormalizedMessage in,
		NormalizedMessage out, ByteBuffer tcpMsg) throws Exception {
		
	}
	*/
}
