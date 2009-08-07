package org.spagic3.constants;

import java.net.URI;

public class SpagicConstants {
	public final static String CORRELATION_ID = "SPAGIC.CORRELATION_ID";
	
	public final static String SPAGIC_ID_PROPERTY = "spagic.id";
	
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
    
    public static final String SPAGIC_HOME_PROPERTY="spagic.home";
	public static final String CONNECTORS_FOLDER="connectors";
	public static final String SERVICES_FOLDER="services";
	public static final String DATASOURCES_FOLDER="datasources";
	public static final String RESOURCES_FOLDER="resources";
	public static final String ROUTES_FOLDER="routes";
	public static final String LOG_BACK_FILE_SYS_PROP = "logback.configurationFile";
	
	public static final String[] SERVICE_DEPLOYMENTS_EXTENSIONS = {"service","scrappy"};
	public static final String[] CONNECTOR_DEPLOYMENTS_EXTENSIONS = {"connector","service","scrappy"};
	public static final String[] DATASOURCE_DEPLOYMENTS_EXTENSIONS = {"datasource","ds","scrappy"};
	public static final String[] ROUTES_DEPLOYMENTS_EXTENSIONS = {"route","doo"};
	
	
	
	
	//This variables could be setted into the exchange to modify wf engine behavour
    
    //
    // This will set/update the variable called VAR_A in Workflow Process Instance
    // exchange.setProperty("WF_VARIABLE.VAR_A", "ValueOfVariableA");
    //
    public final static String WF_VARIABLE_PREFIX = "WF_VARIABLE";
    
    //
    // This will say to OSGiService Invoker do not update the value of the XML_MESSAGE_VARIABLE_WHEN_RETURNING
    // from workflow
    // exchange.setProperty("WF_NO_UPDATE_XML_MESSAGE", "true");
    //
    public final static String WF_NO_UPDATE_XML_MESSAGE = "WF_NO_UPDATE_XML_MESSAGE";
   
    // If this property id valorized when the Workflow Process Instance is end the result is not porpagated to
    // the target connectors of orchestration service
    public final static String WF_IS_PROCESS_TERMINATED = "WF_IS_PROCESS_TERMINATED";

	public static final String SYNC_EXCHANGE = "SYNC_EXCHANGE";

	
	
	public static final String 	_IS_INTERNAL_EVENT = "_IS_INTERNAL_EVENT";
	
	public static final String 	_INTERNAL_EVENT_TYPE = "_INTERNAL_EVENT_TYPE";
	public static final String  _INTERNAL_EVENT_DS_DEPLOYED = "_INTERNAL_EVENT_DS_DEPLOYED";
	public static final String  _INTERNAL_EVENT_DS_UNDEPLOYED = "_INTERNAL_EVENT_DS_UNDEPLOYED";
	public static final String  _INTERNAL_EVENT_REFERRING_ID = "_INTERNAL_EVENT_REFERRING_ID";
	
	public static final String  _INTERNAL_EVENT_CONNECTOR_PAUSED = "_INTERNAL_EVENT_CONNECTOR_PAUSED";
	public static final String  _INTERNAL_EVENT_CONNECTOR_STARTED = "_INTERNAL_EVENT_CONNECTOR_STARTED";
	public static final String  _INTERNAL_EVENT_CONNECTOR_STOPPED = "_INTERNAL_EVENT_CONNECTOR_STOPPED";
	
}
