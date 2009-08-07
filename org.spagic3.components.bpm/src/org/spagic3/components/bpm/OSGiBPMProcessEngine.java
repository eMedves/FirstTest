package org.spagic3.components.bpm;

import org.osgi.service.component.ComponentContext;
import org.spagic.workflow.api.IControlAPI;
import org.spagic.workflow.api.IControlExtendedAPI;
import org.spagic.workflow.api.IProcessEngine;
import org.spagic.workflow.api.IQueryAPI;
import org.spagic.workflow.api.jbpm.ProcessEngine;

/**
 * 
 * @author zoppello
 *
 * This class wrap the JBPM Process Engine, making it available only when
 * the datasource with name JBPM is available
 */
public class OSGiBPMProcessEngine implements IProcessEngine {
	
	private IProcessEngine delegatetProcessEngine = null;
	
	protected void activate(ComponentContext componentContext){
		delegatetProcessEngine = new ProcessEngine();
	}
	
	protected void deactivate(ComponentContext componentContext){
		stop();
		delegatetProcessEngine = null;
	}

	@Override
	public IControlAPI getControlAPI() {
		return delegatetProcessEngine.getControlAPI();
	}

	@Override
	public IControlExtendedAPI getControlExtendedAPI() {
		return delegatetProcessEngine.getControlExtendedAPI();
	}

	@Override
	public IQueryAPI getQueryAPI() {
		
		return delegatetProcessEngine.getQueryAPI();
	}

	@Override
	public void stop() {
		delegatetProcessEngine.stop();
		
	}
	
	public void bindJBPM(javax.sql.DataSource ds){
		System.out.println("JBPM Datasource has been bound");
	}
	
	public void unbindJBPM(javax.sql.DataSource ds){
		System.out.println("JBPM Datasource has been unbound");
	}
	
	public void bindMetaDB(javax.sql.DataSource ds){
		System.out.println("Metadb Datasource has been bound");
	}
	
	public void unbindMetaDB(javax.sql.DataSource ds){
		System.out.println("Metadb Datasource has been unbound");
	}

}
