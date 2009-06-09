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

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaMain {
	private static final int PORT = 10222;
	private static String strHeader = "THE_HEADER";
	private static String strTrailer = "THE_TRAILER";
	private static ByteBuffer header;
	private static ByteBuffer trailer;
	
	static {
		header = ByteBuffer.allocate(strHeader.length());
		header.put(strHeader.getBytes());
		header.flip();
		trailer = ByteBuffer.allocate(strTrailer.length());
		trailer.put(strTrailer.getBytes());
		trailer.flip();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
        IoAcceptor acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        ((SocketAcceptorConfig)config).setReuseAddress(true);
        DefaultIoFilterChainBuilder chain = config.getFilterChain();

        /*
        chain.addLast( "codec", new ProtocolCodecFilter(
        	new TCPMsgCodecFactory(header, trailer, true, ByteOrder.BIG_ENDIAN)
        ));
        */
        
        addLogger( chain );
        
        // Bind
        acceptor.bind(
                new InetSocketAddress( PORT ),
                new EchoProtocolHandler(),
                config );

        //System.out.println( "Listening on port " + PORT );
	}
    
    private static void addLogger( DefaultIoFilterChainBuilder chain ) throws Exception
    {
        chain.addLast( "logger", new LoggingFilter() );
        //System.out.println( "Logging ON" );
    }
    
}
class EchoProtocolHandler extends IoHandlerAdapter
{
    private final Logger log = LoggerFactory.getLogger( EchoProtocolHandler.class );

    public void sessionCreated( IoSession session )
    {
        if( session.getTransportType() == TransportType.SOCKET )
        {
            ( ( SocketSessionConfig ) session.getConfig() ).setReceiveBufferSize( 2048 );
        }
        
        session.setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        
        // We're going to use SSL negotiation notification.
        session.setAttribute( SSLFilter.USE_NOTIFICATION );
    }
    
    public void sessionIdle( IoSession session, IdleStatus status )
    {
        log.info(
                "*** IDLE #" +
                session.getIdleCount( IdleStatus.BOTH_IDLE ) +
                " ***" );
    }

    public void exceptionCaught( IoSession session, Throwable cause )
    {
        cause.printStackTrace();
        session.close();
    }

    public void messageReceived( IoSession session, Object message ) throws Exception
    {
        if( !( message instanceof ByteBuffer ) )
        {
            return;
        }

        ByteBuffer rb = ( ByteBuffer ) message;
        String strMsg = rb.getString(Charset.forName( "UTF-8" ).newDecoder());
        log.info("ECHO_RECEIVED: "+strMsg);
        // Write the received data back to remote peer
        ByteBuffer wb = ByteBuffer.allocate( rb.remaining() );
        wb.put( rb );
        wb.flip();
        session.write( wb );
    }
}
