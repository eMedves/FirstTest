package org.spagic3.connectors.jbf.helpers;

import java.util.Properties;

import org.eclipse.ebpm.core.ProcessContext;
import org.eclipse.ebpm.messaging.api.Exchange;
import org.eclipse.ebpm.messaging.api.Message;
import org.eclipse.ebpm.util.resources.IResource;
import org.eclipse.ebpm.util.resources.Resource;


public class JBFCaller {
	
	private Properties properties = null;
	
	public JBFCaller(String spagicUri){
		IResource resource = new Resource(spagicUri);
		this.properties = new Properties();
		try{
			properties.load(resource.openStream());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public JBFCaller(Properties properties){
		this.properties = properties;
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
		
	
		// OPEN SESSION 
		int openSession = jbfHelper.openSession();
		if (handleError(openSession, jbfHelper, pc, exchange, in, out))
			return;
		
	
		
		// CALL
		jbfHelper.setJbfRequest(in.getBodyText());
		int call = jbfHelper.callWS();
		if (handleError(call, jbfHelper, pc, exchange, in, out))
			return;
		
		out.setHeader(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.OK);
		pc.setVariable(JBFConstants.JBF_ESITO_HEADER_NAME, JBFConstants.OK);
		out.setBodyText(jbfHelper.getJbfResponse());
		
		
		// CLOSE SESSION
		
		int closeSession = jbfHelper.closeSession();
		if (handleError(closeSession, jbfHelper, pc, exchange, in, out))
			return;
		
	}
}