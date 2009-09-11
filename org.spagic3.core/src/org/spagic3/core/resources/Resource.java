package org.spagic3.core.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.constants.SpagicConstants;

public class Resource implements IResource {

	private Logger logger = LoggerFactory.getLogger(Resource.class);

	private static List<String>  customProtocols = new ArrayList<String>();
	
	static {
		customProtocols.add("xslt");
		customProtocols.add("xsl");
		customProtocols.add("script");
		customProtocols.add("wsdl");
		customProtocols.add("xsd");
		customProtocols.add("rule");
		customProtocols.add("script");
		customProtocols.add("resource");
		customProtocols.add("properties");
	}
	
	
	
	private String resource = null;
	
	public Resource(String resource){
		this.resource = resource;
	}

	public InputStream openStream() {
		try{
			URI uri = new URI(resource);
			String scheme = uri.getScheme();
			if (customProtocols.contains(uri.getScheme())){
				String resourceLocation = System.getProperty(SpagicConstants.SPAGIC_HOME_PROPERTY) + File.separator + SpagicConstants.RESOURCES_FOLDER + File.separator + scheme + File.separator + uri.getHost();
				System.out.println("Resource Resolved to "+ resourceLocation);
				return new FileInputStream(resourceLocation);
			}else{
				return uri.toURL().openStream();
			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public URL asURL(){
		try{
			URI uri = new URI(resource);
			String scheme = uri.getScheme();
			if (customProtocols.contains(uri.getScheme())){
				String resourceLocation = System.getProperty(SpagicConstants.SPAGIC_HOME_PROPERTY) + File.separator + SpagicConstants.RESOURCES_FOLDER + File.separator + scheme + File.separator + uri.getHost();
				System.out.println("Resource Resolved to "+ resourceLocation);
				File f = new File(resourceLocation);
				URI fileURI =  f.toURI();
				return fileURI.toURL();
			}else{
				return uri.toURL();			}
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	
	
	
	
}
