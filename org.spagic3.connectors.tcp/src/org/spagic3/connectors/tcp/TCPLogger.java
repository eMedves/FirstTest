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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;

import org.apache.mina.common.ByteBuffer;

/**
 * Simple logger that uses commons logging to log TCP sessions and data
 * 
 * @author buso
 *
 */
/*
 * TODO check to use log4j with a dinamic configuration
 */
public class TCPLogger {
	private PrintWriter writer;
	private boolean initiated = false;
	private TCPBCConfig cfg;
	private String name;
	
	public TCPLogger(TCPBCConfig config, String nn) {
		try {
			cfg = config;
			writer = new PrintWriter(new FileWriter(
				cfg.getConnectionLogFileName(), true),
				true);
			name = nn;
			initiated = true;
		} catch (Exception ioe) {
			if (cfg.isLogConnections() || cfg.isLogData()) {
//				System.out.println("Error initializing TCP logging. Log will " +
//					"not be performed");
				ioe.printStackTrace();
			}
		}
	}
	
	private Object getByteBufferDump(Object msg) {
		if (msg instanceof ByteBuffer) {
	        ByteBuffer rb = ( ByteBuffer ) msg;
	        rb.rewind();
	        String strMsg = "";
	        if (cfg.isLogDataAsHex()) {
	        	strMsg = rb.getHexDump();
	        } else {
	        	try {
	        		strMsg = rb.getString(Charset.forName( "UTF-8" ).newDecoder());
	        	} catch (CharacterCodingException cce) {
	        		// ignore
	        	}
	        }
	        rb.rewind();
			return strMsg;
		} else {
			return msg;
		}		
	}
	
	public void logDataSent(Object msg) {
		if (cfg.isLogData()) {
			if (!initiated) { return; }
			// do hex conversion if needed
			log("SENT", getByteBufferDump(msg));
		}
	}

	
	public void logDataReceived(Object msg) {
		if (cfg.isLogData()) {
			if (!initiated) { return; }
			// do hex conversion if needed
			log("RECEIVED", getByteBufferDump(msg));
		}
	}
	
	public void logSession(Object msg) {
		if (cfg.isLogConnections()) {
			if (!initiated) { return; }
			log(msg);
		}
	}
	
	private void log(String prefix, Object msg) {
		StringBuffer localMessage = new StringBuffer();
		if (Boolean.parseBoolean(cfg.getExtraInformation())) {
			localMessage.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
				.format(new Date())).append(" ");
		}
		localMessage.append(" ").append(name).append(" ");
		if (prefix != null && !prefix.equals("")) {
			localMessage.append(prefix);
		}
		if (msg != null) {
			localMessage.append(" ").append(msg.toString());
		} else {
			localMessage.append(" ").append(msg);
		}
		writer.println(localMessage);
	}
	
	private void log(Object msg) {
		log("", msg);
	}
	
	public void dispose() {
		try {
			writer.close();
		} catch (Exception e) {
			// ignore
		}
	}
}
