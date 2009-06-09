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
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decode bytes received by the socket into messages for the protocol.
 * Messages are recognize having an 'header' and a 'trailer' that bound them.
 * Data is passed to the ProtocolDecoderOutput as ByteBuffer.
 * 
 * TODO
 * Now data is passed as ByteBuffer but should be encapsulated into objects
 * that rappresent messages.
 *
 */
public class TCPMsgDecoder implements ProtocolDecoder {
	
	private static final String CONTEXT = "TCPMSGDECODER_CONTEXT".intern();
	private static final Logger log = LoggerFactory.getLogger(TCPMsgDecoder.class);
	
	private boolean stripped = false;
	private ByteBuffer header;
	private ByteBuffer trailer;
	private ByteOrder order;
	
	public TCPMsgDecoder(ByteBuffer header, ByteBuffer trailer, boolean strip,
		ByteOrder endianness) {
		this.header = header.duplicate();
		this.trailer = trailer.duplicate();
		stripped = strip;
		order = endianness;
		
		this.header.order(order);
		this.trailer.order(order);
	}
	
	
	public void decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
		 log.debug("----------------------------------------------------------------");
		 if (getHeadCount(session) == null){
			 log.debug("Resetting Head Count");
			 setHeadCount(session, 0);
		 }
		 if (getTrailCount(session) == null){
			 log.debug("Resetting TrailCount Count");
			 setTrailCount(session, 0);
		 }
		 int headMatchCount = getHeadCount(session);
		 int trailMatchCount = getTrailCount(session);
		 log.debug("HeadCount -> " + headMatchCount);
		 log.debug("TrailCount -> " + trailMatchCount);
		 ByteBuffer sessionBuffer = getSessionBuffer(session);
		 //log.debug("Session Buffer -> "+ ((sessionBuffer != null) ? "SessionBuffer is Not Null" : "SessionBuffer is Null"));
		 boolean finished = false;
		 ByteBuffer internalBuffer = ByteBuffer.allocate(1024).setAutoExpand(true).order(order);
		 internalBuffer.put(in);
		 internalBuffer.flip();
		 int bytesReaded = 0;
		 for(  bytesReaded = 0; internalBuffer.hasRemaining(); bytesReaded++){
			 byte b = internalBuffer.get();
			 
			 if ((!isHeaderReaded(session)) 
				 && (header.get(headMatchCount) == b)) {
				 
				 log.debug("Is Header"); 
				 headMatchCount++;
				 if (headMatchCount == header.limit()){
					 	log.debug("Header Finished Strip ["+stripped+"]");
					 	sessionBuffer = createAndSetSessionBufferIfIsNull(session, sessionBuffer);
					 	setHeaderReaded(session);
						if (!stripped) { 
							sessionBuffer.put(b);
	                	}
				 }else{
					 if (!stripped) { 
						 sessionBuffer.put(b);
					 }
				 }
			 }	else if ( trailer.get( trailMatchCount ) == b ) {
					 log.debug(" Is Trailer "); 
					 trailMatchCount++;
					 log.debug("trail -->" + trailMatchCount + "trailer.limit() --> " + trailer.limit());
					 if (trailMatchCount == trailer.limit()) {
	         				log.debug("Trailer Finished"); 
	         				if (!stripped) {
	         					sessionBuffer.put(b);
	        		        }
	         				sessionBuffer.flip();
	         				log.debug("Message Finished");
	         				out.write(sessionBuffer);
	         				
	         				setSessionBuffer(session, null);
	         				setTrailCount(session, 0);
	         				setHeadCount(session, 0);
	         				finished = true;
	         				resetHeaderReaded(session);
					 }else{
						 if (!stripped) {
	         					sessionBuffer.put(b);
	        		     }
					 }
			  } else{
				     if (sessionBuffer == null)
				    	 log.debug("Session Buffer Is Null Writing Message Mody");
					 sessionBuffer.put(b);
			  }
		}
		log.debug("Bytes Readed ["+ bytesReaded + "] Finished ["+finished+"]");
		if (!finished){
		
			setHeadCount(session, headMatchCount);
			setTrailCount(session, trailMatchCount);
			 
		
		 }
		 
		 internalBuffer.release();
		 internalBuffer = null;
		 
	}
	
	
	public ByteBuffer createAndSetSessionBufferIfIsNull(IoSession session, ByteBuffer bb){
		if (bb != null)
			return bb;
		else{
			log.debug("Creating Session Buffer");
			ByteBuffer newBuffer =  ByteBuffer.allocate(1024).setAutoExpand(true).order(order); 
			setSessionBuffer(session, newBuffer);
			return getSessionBuffer(session);
		}
			
	}
	public ByteBuffer getSessionBuffer(IoSession session){
		
		return (ByteBuffer)session.getAttribute("SessionMessage");
	}
	
	public void setSessionBuffer(IoSession session, ByteBuffer bb){
		
		session.setAttribute("SessionMessage", bb );

	}
	
	public Integer getHeadCount(IoSession session){
		
		return (Integer)session.getAttribute("HeadCount");
	}
	
	public void setHeadCount(IoSession session, int hc){
		 log.debug("Set HeadCount" + hc);
		 session.setAttribute("HeadCount", hc);
	}
	
	
	public Integer getTrailCount(IoSession session){
		
		return (Integer)session.getAttribute("TrailCount");
	}
	
	public void setTrailCount(IoSession session, int hc){
		
		 session.setAttribute("TrailCount", hc);
	}
	
	public void setHeaderReaded(IoSession session){
		
		 session.setAttribute("HeaderReaded", 1);
	}
	
	public boolean isHeaderReaded(IoSession session){
		
		 return session.getAttribute("HeaderReaded") != null;
	}
	
	public void resetHeaderReaded(IoSession session){
		 if (isHeaderReaded(session))
			 session.removeAttribute("HeaderReaded");
	}
	
	
	
	
	/** OLD METHOD BY NICOLA BUSO
	public void decode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		
		//dumpMemoryInfo();
		if (log.isDebugEnabled()) {
			log.debug(session.getAttribute(SessionLog.PREFIX)+"decode start");
		}
		log.debug(" decode :: InMessage  "+ in.capacity());
		//log.debug(" decode :: Getting Context Object ");
		
		Context ctx = getContext(session);
		
		int headMatchCount = ctx.getHeadMatchCount();
		int trailMatchCount = ctx.getTrailMatchCount();
		
	   
		
		
		ByteBuffer buffer = ByteBuffer.allocate(1024).setAutoExpand(true).order(order); 
		ByteBuffer msg = ByteBuffer.allocate(1024).setAutoExpand(true).order(order); 
		
	
		buffer.put(ctx.getBuffer());
	
		buffer.flip();
		
		
		
		ByteBuffer internalInBuffer = ByteBuffer.allocate(1024).setAutoExpand(true).order(order);
		internalInBuffer.put(buffer);
		internalInBuffer.put(in);
		internalInBuffer.flip();
		
		ByteBuffer trailerBuffer = ByteBuffer.allocate(trailer.capacity()).order(order);
		
		
		headMatchCount = 0;
        boolean messageFinished = false;
        
        
        while( internalInBuffer.hasRemaining() ) {
        	//log.debug("decode :: Reading Header into buffer var ");
        	messageFinished = false;
            byte b = internalInBuffer.get();
            buffer.put(b);
            if( header.get( headMatchCount ) == b ) {
            	headMatchCount ++;
                if( headMatchCount == header.limit() )
                { // the header terminate
                	//log.debug("decode :: Header Terminated");
                	if (!stripped) {
                		msg.put(b);
                	}
                	//log.debug("decode :: Header Reading Meassage Body Looking for trailer");
                	// retrieve the body looking for trailer
                	while (internalInBuffer.hasRemaining() && !messageFinished) {
                		b = internalInBuffer.get();
                		buffer.put(b);
                		if ( trailer.get( trailMatchCount ) == b ) {
                			trailMatchCount++;
                			trailerBuffer.put(b);
                			if (trailMatchCount == trailer.limit()) {
                				// the message is ended

                				if (!stripped) {
                		        	trailerBuffer.flip();
                		        	msg.put(trailerBuffer);
                		        }
                		        msg.flip();
                		        
                		       
                		        log.debug("decode :: Message Terminated "+ msg.capacity());
                		       
                				out.write(msg);
                				
                				clearContext(session);
                				
                				buffer.release();
                				
                				msg = ByteBuffer.allocate(1024).setAutoExpand(true).order(order);
                				buffer.position(0);
                				buffer.limit(0);
                				trailerBuffer.clear();
                				messageFinished = true;
                				headMatchCount = 0;
                				trailMatchCount = 0;
                			}
                		} else {
                			// this is part of the body
                			//log.debug("decode :: Message Is Not Terminated ");
                			if (trailMatchCount > 0) {
                				// somethings that start as the trailer was found
                				trailerBuffer.flip();
                				msg.put(trailerBuffer);
                				trailerBuffer.clear();
                			}
                			
                			trailMatchCount = 0;
                			msg.put(b);
                		}
                	}
                } else {
                	//log.debug("decode :: Header Is Not Terminated ");
                	if (!stripped) {
                		msg.put(b);
                	}
                }
            } else {
            	headMatchCount = 0;
            }
        }
        
        log.debug(" After Reading Internal Buffer ");
        if ( messageFinished ){
        	log.debug(" Message is Finished ");
        }else{
        	log.debug(" Message is Not Finished ");
        }
        
        log.debug(" - headMatchCount="+headMatchCount);
	    log.debug(" - trailMatchCount="+trailMatchCount);
      
    	if (!messageFinished) {
    		log.debug("Stop reading, but message is not finished! " +
    				"received messages: "+session.getReadMessages());
    		// the readed buffer is putted in session and re-decoded on next call

    		log.debug("Chunked buffer size (limit): "+buffer.limit());
    	}
		// set the headMatchCount and trailMatchCount on Context
        ctx.setHeadMatchCount(headMatchCount);
        ctx.setTrailMatchCount(trailMatchCount);

        // put remainder on context buffer
        ctx.setBuffer(buffer);
        if (log.isDebugEnabled()) {
			log.debug(session.getAttribute(SessionLog.PREFIX)+"decode finished");
        }
        
	}
	*/
	
	
	private void logBuffer(ByteBuffer buff) throws Exception {
		int pos = buff.position();
		int limit = buff.limit();
		log.debug("Buffer.toString(): "+buff);
		log.debug("Buffer: "+buff.rewind().getString(java.nio
			.charset.Charset.forName( "UTF-8" ).newDecoder()));
		buff.position(pos);
		buff.limit(limit);
	}
	
	
	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
		// TODO Auto-generated method stub

	}
	
	
	
}
