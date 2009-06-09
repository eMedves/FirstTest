package org.spagic3.core;

import java.net.URI;

public class SpagicConstants {
	public final static String CORRELATION_ID = "SPAGIC.CORRELATION_ID";
	
	public final static String SPAGIC_SENDER = "SPAGIC_SENDER";
	
	public final static String SPAGIC_TARGET = "SPAGIC_TARGET";	
	public final static String EXCHANGE_ID = "EXCHANGE_ID";
	
	public final static String SPAGIC_BASE_TOPIC = "SPAGIC/MESSAGES/";

	public final static String IN_ONLY_MEP = "in-only";
	public final static String IN_OUT_MEP = "in-out";
	
	public final static String OUT_PROPERTY = "OUTP";
	public final static String IN_PROPERTY = "INP";
	public final static String FAULT_PROPERTY = "FAULTP";
	public final static String EXCHANGE_PROPERTY = "EP";
	public final static String INBODY = "INBODY";
	public final static String OUTBODY = "OUTBODY";
	public final static String FAULTBODY = "FAULTBODY";
	public final static String EXCHANGE_MEP = "MEP";
	public final static String EXCHANGE_STATUS = "STATUS";
	
	public final static String STATUS_ACTIVE = "ACTIVE";
	public final static String STATUS_DONE = "DONE";
	public final static String STATUS_ERROR = "ERROR";
	
	
	
	
	public static final String SOAP_FAULT_CODE = "spagic.soap.fault.code";
    public static final String SOAP_FAULT_SUBCODE = "spagic.soap.fault.subcode";
    public static final String SOAP_FAULT_REASON = "spagic.soap.fault.reason";
    public static final String SOAP_FAULT_NODE = "spagic.soap.fault.node";
    public static final String SOAP_FAULT_ROLE = "spagic.soap.fault.role";
    
    
    public static final URI IN_ONLY_URI = URI.create("http://www.w3.org/2004/08/wsdl/in-only");
    public static final URI IN_OUT_URI = URI.create("http://www.w3.org/2004/08/wsdl/in-out");
    public static final URI IN_OPTIONAL_OUT_URI = URI.create("http://www.w3.org/2004/08/wsdl/in-opt-out");
    public static final URI ROBUST_IN_ONLY_URI = URI.create("http://www.w3.org/2004/08/wsdl/robust-in-only");

    
    public static final String PROTOCOL_HEADERS = "nmr.messaging.protocol.headers";
}
