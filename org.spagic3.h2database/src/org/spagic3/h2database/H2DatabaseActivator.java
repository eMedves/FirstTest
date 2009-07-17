package org.spagic3.h2database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Console;
import org.h2.util.JdbcUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class H2DatabaseActivator implements BundleActivator {


	
	private static final String DDL_NAME = "spagic-metadb-h2.ddl";
	private static final String SETUP_SQL_NAME = "setup-h2.sql";
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 * 
	 */
	public void start(BundleContext context) throws Exception {
		Thread th = new Thread(new H2Starter());
		th.run();
		
		/*
		String spagicDbUrl = "jdbc:h2:tcp://localhost/~/spagic";
		String jbpmDbUrl = "jdbc:h2:tcp://localhost/~/jbpm";
		
		System.out.println(" Creating Spagic User ");
		executeSQL(spagicDbUrl, "sa", "", "CREATE USER IF NOT EXISTS  spagic PASSWORD 'spagic'");
		executeSQL(spagicDbUrl, "sa", "", "ALTER USER spagic ADMIN TRUE");
		System.out.println(" User Spagic Created ");
		
		
		executeSQL(jbpmDbUrl, "sa", "", "CREATE USER IF NOT EXISTS  jbpm PASSWORD 'jbpm'");
		executeSQL(jbpmDbUrl, "sa", "", "ALTER USER jbpm ADMIN TRUE");
		
		prepareSpagicScripts(context);
		String tmpFolder = System.getProperty("java.io.tmpdir");
		
		File tmpDDLFile = new File(tmpFolder + File.separator + DDL_NAME);
		File tmpSetupSQLFile = new File(tmpFolder + File.separator + SETUP_SQL_NAME);
		
		String script  = tmpDDLFile.getCanonicalPath();
		runScript(spagicDbUrl, "spagic", "spagic", script);
		
		script  = tmpSetupSQLFile.getCanonicalPath();
		runScript(spagicDbUrl, "spagic", "spagic", script);
		*/
	}


       
  
  
	
	private void prepareSpagicScripts(BundleContext context) {
		try{
			String tmpFolder = System.getProperty("java.io.tmpdir");
			URL ddlEntry = context.getBundle().getEntry("/"+DDL_NAME);
			URL setupSQLEntry = context.getBundle().getEntry("/"+SETUP_SQL_NAME);
		
			File tmpDDLFile = new File(tmpFolder + File.separator + DDL_NAME);
			File tmpSetupSQLFile = new File(tmpFolder + File.separator + SETUP_SQL_NAME);
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(ddlEntry.openStream()));
			PrintWriter writer = new PrintWriter(new FileWriter(tmpDDLFile));
			String line = null;
			while ( (line = reader.readLine()) != null){
				writer.println(line);
			}
			writer.flush();
			writer.close();
			
			reader = new BufferedReader(new InputStreamReader(setupSQLEntry.openStream()));
			writer = new PrintWriter(new FileWriter(tmpSetupSQLFile));
			line = null;
			while ( (line = reader.readLine()) != null){
				writer.println(line);
			}
			writer.flush();
			writer.close();
		}catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	private static void runScript(String url, String user, String password, String fileName) throws SQLException {
       
            String sql = "RUNSCRIPT FROM '" + fileName + "' ";
            executeSQL(url,user,password, sql);
    }

	private static void executeSQL(String url, String user, String password, String sql) throws SQLException {
		 	Connection conn = null;
	        java.sql.Statement stat = null;
	        try {
	            conn = DriverManager.getConnection(url, user, password);
	            stat = conn.createStatement();
	           
	            stat.execute(sql);
	        }catch (Throwable t) {
	        	t.printStackTrace();
				
	        } finally {
	            JdbcUtils.closeSilently(stat);
	            JdbcUtils.closeSilently(conn);
	        }
	}






	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

	

	
}
