package org.spagic3.connectors.jbf.helpers;

import java.io.StringWriter;
import java.util.Properties;


import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

public class JBFHelper {

	private Properties properties = null;
	private boolean balanced  = false;
	
	private String sessionId = null;
	private String cookieLblSessionId = null; //GLC add
	private String cookieOpenSession = null;
	
	private String jbfRequest;
	private String jbfResponse;
	private String jbfError;

	
	public String getCookieLblSessionId() {
		return cookieLblSessionId;
	}


	public void setCookieLblSessionId(String cookieLblSessionId) {
		this.cookieLblSessionId = cookieLblSessionId;
	}

	
	public String getJbfError() {
		return jbfError;
	}




	public void setJbfError(String jbfError) {
		this.jbfError = jbfError;
	}




	public Properties getProperties() {
		return properties;
	}




	public void setProperties(Properties properties) {
		this.properties = properties;
	}




	public String getJbfRequest() {
		return jbfRequest;
	}




	public void setJbfRequest(String jbfRequest) {
		this.jbfRequest = jbfRequest;
	}




	public String getJbfResponse() {
		return jbfResponse;
	}




	public void setJbfResponse(String jbfResponse) {
		this.jbfResponse = jbfResponse;
	}




	public JBFHelper(Properties properties) {
		this.properties = properties;
	}
	
	
	public String getProperty(String key){
		System.out.println(" JBF Helper -> Getting Property ["+key+"]");
		return (this.properties.getProperty(key) != null ? this.properties.getProperty(key) : "" );
	}
	
	public int openSession(){
		try{
		
         Document openSessionDoc = DocumentHelper.createDocument();
         
         
//         <Input>
//         		<OpenSession>
//         		<user>XMPI</user>
//         		<password>XMPIXMPI</password>
//         		<azienda></azienda>
//         		<ufficio></ufficio>
//         		<dataLavoro></dataLavoro>
//         		<entita>XMPI</entita>
//         		<OpenSession>
//         <Input>
         		
         		
         		
         Element inputElement = openSessionDoc.addElement("Input");
         Element openSession = inputElement.addElement("OpenSession");
         
         Element userEl = openSession.addElement("user");
         userEl.setText(getProperty(JBFConstants.JBF_USER));
         Element passwordEl = openSession.addElement("password");
         passwordEl.setText(getProperty(JBFConstants.JBF_PASSWORD));
         Element aziendaEl = openSession.addElement("azienda");
         aziendaEl.setText(getProperty(JBFConstants.JBF_AZIENDA));
         Element ufficioEl = openSession.addElement("ufficio");
         ufficioEl.setText(getProperty(JBFConstants.JBF_UFFICIO));
         Element dataLavoroEl = openSession.addElement("dataLavoro");
         dataLavoroEl.setText("");
         Element entitaEl = openSession.addElement("entita");
         entitaEl.setText(getProperty(JBFConstants.JBF_ENTITA));

         
         
         SOAPMessage jbfSOAPCallRequest = createJBFSoapMessageRequest(inputElement);
   		 
   		 SOAPMessage response = call(this.properties.getProperty(JBFConstants.JBF_URL),jbfSOAPCallRequest);
   		 
   		 String body = getBodyAsString(response);
   		 System.out.println( " Response From Open Session ");
   		 System.out.println(body);
   		 
//   		 // get cookie per bilanciamento joem 30 11 2009
//			CookieOpenSession = 
//					response.getMimeHeaders().getHeader("Set-Cookie");
//			System.out.println("\nThe COOKIE OPEN SESSION IS : .... : "+ 
//					CookieOpenSession[0]);
//			// get cookie per bilanciamento joem 30 11 2009
   		 
			if (isBalanced()) {
				
				this.cookieOpenSession = response.getMimeHeaders().getHeader(
						"Set-Cookie")[0];
				//GLC
				int counterHeader = response.getMimeHeaders().getHeader("Set-Cookie").length;
				if(counterHeader>1){
					this.cookieLblSessionId = response.getMimeHeaders().getHeader("Set-Cookie")[1];
					System.out.println("\nGLC: .... : " + this.cookieLblSessionId);
				}
				//GLC controllo se esiste LBL nei coockie
				//NB. se dovranno essere passati piu coockie, far diventare cookieOpenSession come String [], e usare il for each:
				//for(String fullCookieStr : response.getMimeHeaders().getHeader("Set-Cookie") )
				//		System.out.println("\nGLC: .... : " + fullCookieStr);
				//this.lblSessionId = response.getMimeHeaders().getHeader("lblSessionId")[0];
			}
			
			
			if (isErrorResponse(body)) {
				setJbfError(retrieveConnectionError(body));
				setJbfResponse(connectionRefused());
				return JBFConstants.JBF_CONNECTION_ERROR;
				
			} else if (isException(body)) {
				setJbfError(retrieveExceptionDescription(body));
				setJbfResponse(retrieveRealResponse(body));
				return JBFConstants.JBF_APPLICATION_ERROR;
				
			} else {
				this.sessionId = retrieveSessionId(body);
				//this.lblSessionId = retrieveLBLSessionId(body); //GLC non serve lato app
				return JBFConstants.JBF_OK;
			}
		} catch (Throwable e) {
			throw new RuntimeException("Error Invoking Spagic", e);
		}
	}
	public int callWS(){
		try{
			String payloadSessionId = this.jbfRequest.replace("SESSIONEJBFID",this.sessionId);
		
			Document payloadDoc = DocumentHelper.parseText(payloadSessionId);
			SOAPMessage jbfSOAPCallRequest = createJBFSoapMessageRequest(payloadDoc.getRootElement());
	   		SOAPMessage response = call(this.properties.getProperty(JBFConstants.JBF_URL),jbfSOAPCallRequest);
	   		
	   		String body = getBodyAsString(response);
	   		if (isErrorResponse(body)){
	   			setJbfError(retrieveConnectionError(body));
	   			setJbfResponse(connectionRefused());
	   			return JBFConstants.JBF_CONNECTION_ERROR;
			}else if (isException(body)){
				setJbfError(retrieveExceptionDescription(body));
				setJbfResponse(retrieveRealResponse(body));
				return JBFConstants.JBF_APPLICATION_ERROR;
	   		}else{
	   			 this.jbfResponse = retrieveRealResponse(body);
	   			 return JBFConstants.JBF_OK;
	   		}
		}catch (Throwable e) {
			throw new RuntimeException("Error Invoking JBF::callWS", e);
		}
	}
	
	public String connectionRefused(){
		return "<Output><Exception><Description>Connection refused</Description></Exception></Output>";
	}
	public int closeSession() {
		try {
			Document openSessionDoc = DocumentHelper.createDocument();
			Element inputElement = openSessionDoc.addElement("Input");
			inputElement.addAttribute("SessionID", this.sessionId);
			//GLC serve ache per la chiusura? viene aggiunto nella createJBFSoapMessageRequest
			//inputElement.addAttribute("lblSessionID", this.lblSessionId);
			inputElement.addElement("CloseSession");

			SOAPMessage jbfSOAPCallRequest = createJBFSoapMessageRequest(inputElement);

			SOAPMessage response = call(this.properties.getProperty(JBFConstants.JBF_URL), jbfSOAPCallRequest);

		} catch (Throwable e) {
			throw new RuntimeException("Error Invoking JBF::closeSession", e);
		}
		return JBFConstants.JBF_OK;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getCookieOpenSession() {
		return cookieOpenSession;
	}
	public void setCookieOpenSession(String cookieOpenSession) {
		this.cookieOpenSession = cookieOpenSession;
	}

	
	public SOAPMessage createJBFSoapMessageRequest(Element el) throws Exception {
		
		 System.out.println("Input JBF");
		 System.out.println(el.asXML());
		 MessageFactory messageFactory = MessageFactory.newInstance();
         SOAPMessage message = messageFactory.createMessage();
         SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
         SOAPHeader soapHeader = envelope.getHeader();
         
         SOAPBody soapBody = envelope.getBody();
         
         SOAPBodyElement callElement;
		 SOAPElement requestElement;
   		 callElement= soapBody.addBodyElement(envelope.createName("call"));
   		 requestElement= callElement.addChildElement("callRequest");
   		 requestElement.addTextNode(el.asXML());
   		 message.getMimeHeaders().addHeader("SOAPAction", "");
   		 
   		 if ((this.cookieOpenSession != null) && isBalanced()){
   			message.getMimeHeaders().setHeader("Cookie", cookieOpenSession);
   			//GLC add
   			message.getMimeHeaders().addHeader("Cookie", cookieLblSessionId);
   		 }
   		
   		 return message;
	}
	
	
	
	public String retrieveSessionId(String body) throws Exception {
		Document bodyDocument = DocumentHelper.parseText(body);
		org.dom4j.Node callret = bodyDocument.selectSingleNode("/callResponse/callReturn");
		String callRetText = callret.getText();
		
		Document callRetDoc = DocumentHelper.parseText(callRetText);
		org.dom4j.Node sessIdDoc = callRetDoc.selectSingleNode("/Output/OpenSession/SessionID");
		
		return sessIdDoc.getText();
		
	}
	//GLC LBL non serve lato app
/*	public String retrieveLBLSessionId(String body) throws Exception {
		Document bodyDocument = DocumentHelper.parseText(body);
		org.dom4j.Node callret = bodyDocument.selectSingleNode("/callResponse/callReturn");
		String callRetText = callret.getText();
		
		Document callRetDoc = DocumentHelper.parseText(callRetText);
		org.dom4j.Node lblSessIdDoc = callRetDoc.selectSingleNode("/Output/OpenSession/lblSessionID");
		
		return lblSessIdDoc.getText();
		
	}	*/
	public String retrieveRealResponse(String body) throws Exception {
		Document bodyDocument = DocumentHelper.parseText(body);
		org.dom4j.Node callret = bodyDocument.selectSingleNode("/callResponse/callReturn");
		String callRetText = callret.getText();
		
		Document callRetDoc = DocumentHelper.parseText(callRetText);
	
		
		return callRetDoc.getRootElement().asXML();
		
	}
	
	public String retrieveExceptionDescription(String body) throws Exception {
		Document bodyDocument = DocumentHelper.parseText(body);
		org.dom4j.Node callret = bodyDocument.selectSingleNode("/callResponse/callReturn");
		String callRetText = callret.getText();
		
		Document callRetDoc = DocumentHelper.parseText(callRetText);
	
		Node exDescription = callRetDoc.selectSingleNode("/Output/Exception/Description");
		
		return exDescription != null ? exDescription.getText() : "Generic Exception";
	
	}
	
	public String retrieveConnectionError(String body) throws Exception {
		
		
		return "JBF Connection Refused";
	
	}
	public boolean isErrorResponse(String body){
		return body.indexOf("faultstring")>0;
	}
	
	public boolean isException(String body){
		return body.indexOf("Exception")>0;
	}
	public static SOAPMessage call(String wsEndpoint, SOAPMessage soapRequest) throws SOAPException {
		SOAPConnection soapConnection = null;
		try{
			SOAPConnectionFactory soapConnectionFactory = javax.xml.soap.SOAPConnectionFactory.newInstance();

			soapConnection = soapConnectionFactory.createConnection();

			
			javax.xml.soap.SOAPMessage soapResponse = soapConnection.call(soapRequest, wsEndpoint);
			return soapResponse;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			soapConnection.close();
		}
		

	}
	private String getBodyAsString(SOAPMessage soapMessage) throws Exception {
		

		// This is the "jbfRequest" element
		SOAPBody body = soapMessage.getSOAPBody();
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(body.getFirstChild()),new StreamResult(buffer));
		String str = buffer.toString();
		
		return str;
	
	}
	
	
	
	
	
	
	protected String getSoapFaultDetail(SOAPBody body){
		SOAPFault fault = body.getFault();
		if (fault != null){
			Detail detail = fault.getDetail();
			return detail.getTextContent();
		}else{
			return null;
			
		}
	}
	
	public boolean isBalanced() {
		return balanced;
	}

	public void setBalanced(boolean balanced) {
		this.balanced = balanced;
	}
	
}
