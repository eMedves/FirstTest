package org.spagic3.commands.equinox;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;




public class SpagicCommands implements CommandProvider {
	/*
	properties.put("spagic.id", spagicId);
	properties.put("factory.name", factoryName);
	*/
	public static final String SPAGIC_SERVICE = "SPAGIC_SERVICE";
	public static final String SPAGIC_CONNECTOR = "SPAGIC_CONNECTOR";
	public static final String SPAGIC_DATASOURCE = "SPAGIC_DATASOURCE";
	
	public static final String SPAGIC_TYPE = "SPAGIC_TYPE";
	
	private BundleContext context = null;
	protected void activate(ComponentContext componentContext){
		context = componentContext.getBundleContext();
	}
	
	protected void deactivate(ComponentContext componentContext){
		context  = null;
	}
	private void internalSpagicServiceCmd(String filter, CommandInterpreter intp) {
		try{
			ServiceReference[] services = context.getServiceReferences(null, filter);
			if (services != null) {
				int size = services.length;
				if (size > 0) {
					for (int j = 0; j < size; j++) {
						ServiceReference service = services[j];
						
						
						intp.println(service);
						intp.print("  "); //$NON-NLS-1$
						intp.print("Bundle ");
						intp.print(" "); //$NON-NLS-1$
						intp.println(service.getBundle());
						
					
					}
					
					return;
				}
			}
			//intp.println(ConsoleMsg.CONSOLE_NO_REGISTERED_SERVICES_MESSAGE);
			intp.println(" NO SPAGIC SERVICES ");
		}catch (Exception e) {
			intp.printStackTrace(e);
		}
	}
	/**
	 * Usage: spagic_connector
	 * Usage: spagic_connector <spagic_id>
	 */
	public void _spagic_connector(CommandInterpreter ci) {
		String filter = "(&(objectClass=org.spagic3.core.ISpagicService)(SPAGIC_TYPE=SPAGIC_CONNECTOR))";
		
		String next = ci.nextArgument();
		if (ci.nextArgument() != null)
			filter += "(spagic.id="+next+")";
		internalSpagicServiceCmd(filter, ci);
	}
		

	/**
	 * Usage: spagic_service 
	 * Usage: spagic_service  <spagic_id>
	 */
	public void _spagic_service (CommandInterpreter ci) {
		String filter = "(&(objectClass=org.spagic3.core.ISpagicService)(SPAGIC_TYPE=SPAGIC_SERVICE))";
		
		String next = ci.nextArgument();
		if (ci.nextArgument() != null)
			filter += "(spagic.id="+next+")";
		
		internalSpagicServiceCmd(filter, ci);
	}
	
	

	/**
	 * Usage: spagic_datasource
	 * Usage: spagic_datasource  <dsId>
	 */
	public void _spagic_datasource (CommandInterpreter ci) {
		String filter = "(&(objectClass=javax.sql.DataSource)(SPAGIC_TYPE=SPAGIC_DATASOURCE))";
		
		String next = ci.nextArgument();
		if (ci.nextArgument() != null)
			filter += "(spagic.id="+next+")";
		
		internalSpagicServiceCmd(filter, ci);
	}
	
	/**
	 * Usage: spagic_stop_connector <spagic_id>
	 */
	public void _spagic_stop_connector (CommandInterpreter ci) {
		
	}
	
	/**
	 * Usage: spagic_start_connector <spagic_id>
	 */
	public void _spagic_start_connector (CommandInterpreter ci) {
	}
	
	/**
	 * Usage: spagic_pause_connector <spagic_id>
	 */
	public void _spagic_pause_connector (CommandInterpreter ci) {
	}
	
	/**
	 * Usage: spagic_pause_connector <spagic_id>
	 */
	public void _spagic_components (CommandInterpreter ci) {
	}
	
	/**
	 * Usage: spagic_pause_connector <spagic_id>
	 */
	public void _spagic_pending_deployments (CommandInterpreter ci) {
	}
	
	
	public void _spagic_services(CommandInterpreter ci) {		
	}

	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---SPAGIC COMMAND HELP---\n");
		return buffer.toString();
	}
	
}