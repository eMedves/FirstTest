package org.spagic3.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.spagic3.constants.SpagicConstants;

public class ScrappyActivator implements BundleActivator {
	
	public void stop(BundleContext context) throws Exception {
	}
	
	public void start(BundleContext context) throws Exception {
		debugSystemProperties();
		
		String home = System.getProperty(SpagicConstants.SPAGIC_HOME_PROPERTY);
		
		if (home == null){
			home = System.getProperty("user.dir");
			System.out.println("Scrappy Startup --> SPAGIC_HOME NOT SET USE THE CURRENT FOLDER ["+home+"]");
			System.setProperty(SpagicConstants.SPAGIC_HOME_PROPERTY, home);
		}
		
		String logBackFileLocation = System.getProperty(SpagicConstants.LOG_BACK_FILE_SYS_PROP);
		
		if (logBackFileLocation == null){
			logBackFileLocation = home + File.separator + "logback.xml";
			System.out.println("Scrappy Startup --> LOG_BACK FILE NOT SET USE ["+logBackFileLocation+"]");
		
			File logBackFile = new File(logBackFileLocation);
			if (!logBackFile.exists()){
				logBackFile.createNewFile();
				InputStream in = null;
				OutputStream out = null;
				try{
					in = context.getBundle().getEntry("/logback.xml").openStream();
					out = new FileOutputStream(logBackFile);
					copy(in, out);
					out.flush();

				}catch(Exception e){
					e.printStackTrace();
				}finally{
					in.close();
					out.close();
				}
			}
			System.setProperty(SpagicConstants.LOG_BACK_FILE_SYS_PROP, logBackFileLocation);
		}
		
		
		
		
		String servicesFolder = home + File.separator + SpagicConstants.SERVICES_FOLDER;
		System.out.println("Scrappy Startup --> SERVICE FOLDER ["+servicesFolder+"]");
		checkOrCreateFolder(servicesFolder);
		
		
		String connectorFolder = home + File.separator + SpagicConstants.CONNECTORS_FOLDER;
		System.out.println("Scrappy Startup --> CONNECTOR FOLDER ["+connectorFolder+"]");
		checkOrCreateFolder(connectorFolder);
		
		String datasourcesFolder = home + File.separator + SpagicConstants.DATASOURCES_FOLDER;
		System.out.println("Scrappy Startup --> DATASOURCE FOLDER ["+datasourcesFolder+"]");
		checkOrCreateFolder(datasourcesFolder);
		
		String resourceFolder = home + File.separator + SpagicConstants.RESOURCES_FOLDER;
		System.out.println("Scrappy Startup --> RESOURCE FOLDER ["+resourceFolder+"]");
		checkOrCreateFolder(resourceFolder);
		
		
		
		
	}

	private void checkOrCreateFolder(String fName) throws Exception {
		File f = new File(fName + File.separator);
		if (!f.exists())
			f.mkdirs();
		
	}
	

	public void debugSystemProperties(){
		System.out.println("[=========================================================================]");
		System.out.println("[                      ENVIRONMENT INFO                                  =]"); 
		System.out.println("[=========================================================================]");
		Set<String> sysPropertiesNames = (Set<String>) System.getProperties().stringPropertyNames();
		
		
		List<String> pNames = new ArrayList<String>();
		pNames.addAll(sysPropertiesNames);
		
		Collections.sort(pNames);
		
		for (String name : pNames){
			System.out.println("["+name+"] -> ["+System.getProperty(name)+"]");
		}
		
	}
	
	void copy(InputStream in , OutputStream out) throws IOException {
        
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
       
    }

}
