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
package org.spagic3.connectors.tcp.codec;

import java.nio.ByteOrder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.util.SessionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encode outgoing messages wrapping them with 'header' and 'trailer' 
 * 
 * @author buso
 *
 */
public class TCPMsgEncoder implements ProtocolEncoder {
	private static final Logger log = LoggerFactory.getLogger(TCPMsgEncoder.class);
	
	private ByteBuffer header;
	private ByteBuffer trailer;
	private ByteOrder order;
	
	public TCPMsgEncoder(ByteBuffer header, ByteBuffer trailer, ByteOrder order) {
		log.debug("********************** TCP Message Encoder Instanziato ************************************ ");
		this.header = header;
		this.trailer = trailer;
		this.order = order;
	}

	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		log.debug("********************** TCP Message Encoder Disposed ************************************ ");

	}

	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug(session.getAttribute(SessionLog.PREFIX)+"encode start");
		}
		byte[] byteMsg;
		try {
			byteMsg = (byte[])message;
		} catch (ClassCastException cce) {
			log.info("Unable to handle message of type: "+message.getClass().getName());
			log.info("the toString representation will be used: ("+message.toString()+")");
			byteMsg = message.toString().getBytes();
		}
		
		
		header.clear();
		trailer.clear();
		
		ByteBuffer request = ByteBuffer.allocate(2048).setAutoExpand(true);
		request.order(order);
		
		request.put(header);
		request.put(byteMsg);
		request.put(trailer);
		
		request.flip();
		out.write(request);
		if (log.isDebugEnabled()) {
			log.debug(session.getAttribute(SessionLog.PREFIX)+"encode stop");
		}
	}

}
