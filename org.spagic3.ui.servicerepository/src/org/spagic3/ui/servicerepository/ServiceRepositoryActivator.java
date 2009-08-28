package org.spagic3.ui.servicerepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.stp.im.resources.IImResource;
import org.eclipse.stp.im.util.ImLogger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.spagic3.ui.servicerepository.dialogs.ServiceRepositoryResource;

/**
 * The activator class controls the plug-in life cycle
 */
public class ServiceRepositoryActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.spagic3.ui.servicerepository";

	// The shared instance
	private static ServiceRepositoryActivator plugin;
	
	private static Properties resourceProperties = null;

	/**
	 * The constructor
	 */
	public ServiceRepositoryActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ServiceRepositoryActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	private static File getImResourcePropertiesFile(){
		IPath path = getDefault().getStateLocation();
		String imResourcesFileName = path.toOSString() + File.separator + "imresources.properties";
		File imResourcesFile = new File (imResourcesFileName);
		return imResourcesFile;
	}
	
	public static void loadResourceProperties(){
		File imResourcesFile = getImResourcePropertiesFile();
		resourceProperties = new Properties();
		if (!imResourcesFile.exists()){
			return;
		}
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(imResourcesFile);
			resourceProperties.load(fis);
		}catch (Exception e) {
			ImLogger.error(PLUGIN_ID, e.getMessage(),e);
		}finally{
			if (fis != null)
				try{
					fis.close();
				}catch (IOException ioe) {
					ImLogger.error(PLUGIN_ID, ioe.getMessage(),ioe);
				}
		}
		
	}
	
	public static Properties getResourceProperties(){
		if (resourceProperties == null)
			loadResourceProperties();
		return resourceProperties;
	}
	
	public static void saveResourceProperties() {
		File imResourcesFile = getImResourcePropertiesFile();
		if (imResourcesFile.exists())
			imResourcesFile.delete();
		
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(imResourcesFile);
			resourceProperties.store(fos, "Andrea");
		}catch (Exception e) {
			ImLogger.error(PLUGIN_ID, e.getMessage(),e);
		}finally{
			if (fos != null)
				try{
					fos.close();
				}catch (IOException ioe) {
					ImLogger.error(PLUGIN_ID, ioe.getMessage(),ioe);
				}
		}
	}

	public static void addService(IServiceResource serviceBean){
		Properties props = getResourceProperties();
		String name = "im.service."+serviceBean.getId();
		props.setProperty(name, serviceBean.getId());
		props.setProperty(name + ".property.name", serviceBean.getProperty(ServiceRepositoryResource.NAME));
		props.setProperty(name + ".property.target", serviceBean.getProperty(ServiceRepositoryResource.TARGET));
		saveResourceProperties();
	}
	
	public static void deleteService(IServiceResource serviceBean){
		Properties props = getResourceProperties();
		String name = "im.service."+serviceBean.getId();
		props.remove(name);
		props.remove(name + ".property.name");
		props.remove(name + ".property.target");
		saveResourceProperties();
	}
	
	public static List<IServiceResource> getServices(){
		return ServiceRepositoryActivator.getResourcesWithType(IServiceResource.SERVICE_RESOURCE_TYPE);
	}
	
	public static IImResource getResourceByIdAndType(String resourceID, String resourceType){
		Properties props = getResourceProperties();
		ServiceRepositoryResource serviceBean = null;
		String idServ = null;
		for ( Object key : props.keySet()){
			String name = (String)key;
			if (name.startsWith("im."+resourceType+".") && (!name.contains("property"))){
				idServ = props.getProperty(name);
				
				if (idServ.equals(resourceID)){
					serviceBean = new ServiceRepositoryResource(props.getProperty(name));
					serviceBean.setProperty(ServiceRepositoryResource.NAME, props.getProperty(name + ".property.name"));
					serviceBean.setProperty(ServiceRepositoryResource.TARGET, props.getProperty(name + ".property.target"));
				
					return serviceBean;
					
				}
			}
		}
		return null;
	}
	
	public static List<IServiceResource> getResourcesWithType(String resourceType){
		Properties props = getResourceProperties();
		
		List <IServiceResource> resources = new ArrayList<IServiceResource>();
	
		ServiceRepositoryResource sericeBean = null;
		for ( Object key : props.keySet()){
			String name = (String)key;
			if (name.startsWith("im."+resourceType+".") && (!name.contains("property"))){
				if (resourceType.equalsIgnoreCase("service")){
					sericeBean = new ServiceRepositoryResource(props.getProperty(name));
					sericeBean.setProperty(ServiceRepositoryResource.NAME, props.getProperty(name + ".property.name"));
					sericeBean.setProperty(ServiceRepositoryResource.TARGET, props.getProperty(name + ".property.target"));
				
					resources.add(sericeBean);
				}
			}
		}
		return resources;
	}

	
}
