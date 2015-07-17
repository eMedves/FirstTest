package org.spagic3.connectors.jbf.helpers;

import java.util.Properties;

import org.eclipse.ebpm.core.ProcessContext;
import org.eclipse.ebpm.messaging.api.Exchange;
import org.eclipse.ebpm.messaging.api.Message;
import org.eclipse.ebpm.util.resources.IResource;
import org.eclipse.ebpm.util.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JBFCaller {
	
	protected Logger logger = LoggerFactory.getLogger(JBFCaller.class);
	
	private Properties properties = null;
	private boolean balanced  = false;
	
	public boolean isBalanced() {
		return balanced;
	}

	public void setBalanced(boolean balanced) {
		this.balanced = balanced;
	}

	public JBFCaller(String spagicUri){
		this(spagicUri,false);
	}
	
	public JBFCaller(Properties properties){
		this(properties,false);
	}
	
	public JBFCaller(String spagicUri,  boolean balanced){
		IResource resource = new Resource(spagicUri);
		this.properties = new Properties();
		try{
			properties.load(resource.openStream());
		}catch (Exception e) {
			e.printStackTrace();
		}
		this.balanced = balanced;
		
	}
	
	
	
	public JBFCaller(Properties properties, boolean balanced){
		this.properties = properties;
		this.balanced = balanced;
	}
	
	public boolean  handleError( int code, JBFHelper helper, ProcessContext pc, Exchange exchange, Message in, Message out){

		if (code == JBFConstants.JBF_CONNECTION_ERROR){
			String error = helper.getJbfError();
			out.setHeader(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.KO);
			out.setHeader(JBFConstants.JBF_ERRORE_HEADER_NAME, error);
			
			pc.setVariable(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.KO);
			pc.setVariable(JBFConstants.JBF_ERRORE_HEADER_NAME, error);
			out.setBodyText(helper.getJbfResponse());
			
			return true;
		}else if (code == JBFConstants.JBF_APPLICATION_ERROR){
			String error = helper.getJbfError();
			out.setHeader(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.KO);
			out.setHeader(JBFConstants.JBF_ERRORE_HEADER_NAME, error);
			
			pc.setVariable(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.KO);
			pc.setVariable(JBFConstants.JBF_ERRORE_HEADER_NAME, error);
			out.setBodyText(helper.getJbfResponse());
			
			return true;
		}else{
			return false;
		}
		
	}
	public void call(Exchange exchange, Message in, Message out){
		ProcessContext pc = new ProcessContext(exchange);
		JBFHelper jbfHelper = new JBFHelper(properties);
		jbfHelper.setBalanced(balanced);
	
		// OPEN SESSION 
		logger.debug("-- JBFCaller -- Opening session");
		int openSession = jbfHelper.openSession();
		if (handleError(openSession, jbfHelper, pc, exchange, in, out))
			return;
		
	
		
		// CALL
		logger.debug("-- JBFCaller -- Calling WS");
		jbfHelper.setJbfRequest(in.getBodyText());
		int call = jbfHelper.callWS();
		if (handleError(call, jbfHelper, pc, exchange, in, out))
			return;
		
		out.setHeader(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.OK);
		pc.setVariable(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.OK);
		out.setBodyText(jbfHelper.getJbfResponse());
		
		
		// CLOSE SESSION
		logger.debug("-- JBFCaller -- Closing session");
		int closeSession = jbfHelper.closeSession();
		if (handleError(closeSession, jbfHelper, pc, exchange, in, out))
			return;
		
	}
}
