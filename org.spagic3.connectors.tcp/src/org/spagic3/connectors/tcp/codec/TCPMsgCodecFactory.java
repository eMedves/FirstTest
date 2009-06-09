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


import org.apache.mina.common.ByteBuffer;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class TCPMsgCodecFactory implements ProtocolCodecFactory {
	private TCPMsgDecoder decoder;
	private TCPMsgEncoder encoder;
	
	public TCPMsgCodecFactory(org.spagic3.connectors.tcp.TCPBCConfig config) {

		ByteBuffer header = ByteBuffer.allocate(config.getIncomingHeaderBytes().length).setAutoExpand(true);
		header.put(config.getIncomingHeaderBytes());
		header.flip();
		
		ByteBuffer trailer = ByteBuffer.allocate(config.getIncomingTrailerBytes().length).setAutoExpand(true);
		trailer.put(config.getIncomingTrailerBytes());
		trailer.flip();

		decoder = new TCPMsgDecoder(header, trailer, config.isStripWrapping(),
			config.getIncomingEndiannessOrder());
		
		header = null;
		header = ByteBuffer.allocate(config.getOutgoingHeaderBytes().length).setAutoExpand(true);
		header.put(config.getOutgoingHeaderBytes());
		header.flip();

		trailer = null;
		trailer = ByteBuffer.allocate(config.getOutgoingTrailerBytes().length).setAutoExpand(true);
		trailer.put(config.getOutgoingTrailerBytes());
		trailer.flip();
		encoder = new TCPMsgEncoder(header,trailer,
			config.getOutgoingEndiannessOrder());
	}
	

	

	public ProtocolDecoder getDecoder() throws Exception {
		return decoder;
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

}
