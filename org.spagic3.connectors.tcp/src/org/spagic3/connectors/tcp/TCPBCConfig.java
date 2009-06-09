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

import java.nio.ByteOrder;
import java.util.StringTokenizer;
/**
 * TODO I need to divide configurations so that a common (to server and client) configuration
 * can be passed to the marshaller/wrapper, that should be shared between the two BC component.
 * 
 * @org.apache.xbean.XBean element="tcpBCConfig"
 */
public class TCPBCConfig extends CommonConfig {
	public static final String EXTRA_INFO_NONE = "EXTRA_INFO_NONE".intern();
	public static final String EXTRA_INFO_LOG_TIME = "EXTRA_INFO_LOG_TIME".intern();
	
	public static final String WRAPPER_MINIMAL = "WRAPPER_MINIMAL".intern();
	public static final String WRAPPER_USER = "WRAPPER_USER".intern();
	
	public static final String BIG_ENDIAN_STR = ByteOrder.BIG_ENDIAN.toString();
	public static final String LITTLE_ENDIAN_STR = ByteOrder.LITTLE_ENDIAN.toString();

	public static final String CONN_MODE_DISCONNECT = "CONN_MODE_DISCONNECT".intern();
	public static final String CONN_MODE_MAINTAIN = "CONN_MODE_MAINTAIN".intern();
	public static final String CONN_MODE_MANTAIN = "CONN_MODE_MANTAIN".intern();
	
	
	public static final String TCP_DESTINATION_HOST = "org.apache.servicemix.tcp.destination.remoteHost".intern();
	public static final String TCP_DESTINATION_PORT = "org.apache.servicemix.tcp.destination.remotePort".intern();
		
	/* INPUT MODE */
	// listening config
	private int localPort;
	private String localAddress = "localhost";
	private String connectionMode = CONN_MODE_DISCONNECT;
	
	// server listening only
	private int listenBacklog;
	
	// client listening only
	private String remoteHost;
	private int remotePort;
	private boolean useSSL = false;
	private boolean useSSLClientMode = false;
	private String keyStoreFileName;
	private String keyStorePassword;
	private String keyStoreType;
	private String trustStoreFileName;
	private String trustStorePassword;
	private String trustStoreType;
	
	// log config
	private boolean logConnections;
	private boolean logData;
	private boolean logDataAsHex;
	private String connectionLogFileName;
	private String extraInformation = EXTRA_INFO_LOG_TIME;
	
	// protocol config
	private String incomingWrapper = WRAPPER_USER;
	private boolean stripWrapping;
	private byte[] incomingHeader;
	private byte[] incomingTrailer;
	private ByteOrder incomingEndianness = ByteOrder.BIG_ENDIAN;
	
	/* OUTPUT MODE */
	private String outgoingWrapper = WRAPPER_USER;
	private byte[] outgoingHeader;
	private byte[] outgoingTrailer;
	private ByteOrder outgoingEndianness = ByteOrder.BIG_ENDIAN;
	
	/* Client only config */
	private String tcpOutInReceiverClassName;
	
	public String getConnectionLogFileName() {
		return connectionLogFileName;
	}
	public void setConnectionLogFileName(String connectionLogFileName) {
		this.connectionLogFileName = connectionLogFileName;
	}

	public String getExtraInformation() {
		return extraInformation;
	}
	public void setExtraInformation(String extraInformation) {
		this.extraInformation = extraInformation;
	}
	public String getIncomingTrailer() throws Exception {
		return new String(incomingTrailer, "UTF-8");
	}
	public byte[] getIncomingTrailerBytes() {
		return incomingTrailer;
	}
	public void setIncomingTrailer(String incomingTrailerStr) {
		incomingTrailer = stringToBytes(incomingTrailerStr);
	}

//	private String bytesToString(byte[] bytes) {
//		StringBuffer buff = new StringBuffer();
//		
//		for (int idx = 0; idx < bytes.length; idx++) {
//			buff.append(Byte.parseByte(bytes[idx], 16)).append(" ");
//		}
//	}
	
	private byte[] stringToBytes(String byteStr) {
		StringTokenizer tkz = new StringTokenizer(byteStr, " ");
		byte[] byteArray = new byte[tkz.countTokens()];
		for (int idx = 0; tkz.hasMoreTokens(); idx++) {
			byteArray[idx] = Byte.decode(tkz.nextToken());
		}
		return byteArray;
	}
	
	public String getIncomingWrapper() {
		return incomingWrapper;
	}
	public void setIncomingWrapper(String incomingWrapper) {
		this.incomingWrapper = incomingWrapper;
	}
	public int getListenBacklog() {
		return listenBacklog;
	}
	public void setListenBacklog(int listenBacklog) {
		this.listenBacklog = listenBacklog;
	}
	public String getLocalAddress() {
		return localAddress;
	}
	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	public boolean isLogConnections() {
		return logConnections;
	}
	public void setLogConnections(boolean logConnections) {
		this.logConnections = logConnections;
	}
	public boolean isLogData() {
		return logData;
	}
	public void setLogData(boolean logData) {
		this.logData = logData;
	}
	public boolean isLogDataAsHex() {
		return logDataAsHex;
	}
	public void setLogDataAsHex(boolean logDataAsHex) {
		this.logDataAsHex = logDataAsHex;
	}
	public boolean isStripWrapping() {
		return stripWrapping;
	}
	public void setStripWrapping(boolean stripWrapping) {
		this.stripWrapping = stripWrapping;
	}
	public boolean isUseSSL() {
		return useSSL;
	}
	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}
	public boolean isUseSSLClientMode() {
		return useSSLClientMode;
	}
	public void setUseSSLClientMode(boolean useSSLClientMode) {
		this.useSSLClientMode = useSSLClientMode;
	}
	public String getIncomingEndianness() {
		return incomingEndianness.toString();
	}
	public ByteOrder getIncomingEndiannessOrder() {
		return incomingEndianness;
	}
	public void setIncomingEndianness(String incomingEndianness) {
		this.incomingEndianness = getByteOrder(incomingEndianness);
	}
	public String getIncomingHeader() throws Exception {
		return new String(incomingHeader, "UTF-8");
	}
	public byte[] getIncomingHeaderBytes() {
		return incomingHeader;
	}
	public void setIncomingHeader(String incomingHeaderStr) {
		this.incomingHeader = stringToBytes(incomingHeaderStr);
	}
	public String getOutgoingEndianness() {
		return outgoingEndianness.toString();
	}
	public ByteOrder getOutgoingEndiannessOrder() {
		return outgoingEndianness;
	}
	public void setOutgoingEndianness(String outgoingEndianness) {
		this.outgoingEndianness = getByteOrder(outgoingEndianness);
	}
	public String getOutgoingHeader() throws Exception {
		return new String(outgoingHeader, "UTF-8");
	}
	public byte[] getOutgoingHeaderBytes() {
		return outgoingHeader;
	}
	public void setOutgoingHeader(String outgoingUserHeaderStr) {
		this.outgoingHeader = stringToBytes(outgoingUserHeaderStr);
	}
	public String getOutgoingTrailer() throws Exception {
		return new String(outgoingTrailer, "UTF-8");
	}
	public byte[] getOutgoingTrailerBytes() {
		return outgoingTrailer;
	}
	public void setOutgoingTrailer(String outgoingUserTrailerStr) {
		this.outgoingTrailer = stringToBytes(outgoingUserTrailerStr);
	}
	public String getOutgoingWrapper() {
		return outgoingWrapper;
	}
	public void setOutgoingWrapper(String outgoingWrapper) {
		this.outgoingWrapper = outgoingWrapper;
	}
	
	private ByteOrder getByteOrder(String endianness) {
		if (BIG_ENDIAN_STR.equals(endianness)) {
			return ByteOrder.BIG_ENDIAN;
		}
		if (LITTLE_ENDIAN_STR.equals(endianness)) {
			return ByteOrder.LITTLE_ENDIAN;
		}
		throw new IllegalArgumentException("Unknow endiannes: '"+endianness+"'");
	}
	
	public String getRemoteHost() {
		return remoteHost;
	}
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	public String getTcpOutInReceiverClassName() {
		return tcpOutInReceiverClassName;
	}
	public void setTcpOutInReceiverClassName(String tcpOutInReceiverClassName) {
		this.tcpOutInReceiverClassName = tcpOutInReceiverClassName;
	}
	public String getKeyStoreFileName() {
		return keyStoreFileName;
	}
	public void setKeyStoreFileName(String keyStoreFileName) {
		this.keyStoreFileName = keyStoreFileName;
	}
	public String getTrustStoreFileName() {
		return trustStoreFileName;
	}
	public void setTrustStoreFileName(String trustStoreFileName) {
		this.trustStoreFileName = trustStoreFileName;
	}
	public String getKeyStorePassword() {
		return keyStorePassword;
	}
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}
	public String getTrustStorePassword() {
		return trustStorePassword;
	}
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
	public String getConnectionMode() {
		return connectionMode;
	}
	public void setConnectionMode(String connectionMode) {
		this.connectionMode = connectionMode;
	}
	public String getKeyStoreType() {
		return keyStoreType;
	}
	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
	public String getTrustStoreType() {
		return trustStoreType;
	}
	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}
}
