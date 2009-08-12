package org.spagic3.ui.serviceeditor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.spagic.ui.service.editor";

	public static final String IMG_SAMPLE = "sample";
	public static final String IMG_CONNECTORS = "connectors";
	public static final String IMG_CONNECTOR = "connector";
	public static final String IMG_SERVICES = "services";
	public static final String IMG_SERVICE = "service";

	
	// The shared instance
	private static Activator plugin;

	private FormColors formColors;
	
	/**
	 * The constructor
	 */
	public Activator() {
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
	public static Activator getDefault() {
		return plugin;
	}
	
	
	protected void initializeImageRegistry(ImageRegistry registry) {
		registerImage(registry, IMG_SAMPLE, "sample.gif");
		registerImage(registry, IMG_CONNECTORS, "connectors.gif");
		registerImage(registry, IMG_CONNECTOR, "connector.gif");
		registerImage(registry, IMG_SERVICES, "services.gif");
		registerImage(registry, IMG_SERVICE, "service.gif");
	}

	private void registerImage(ImageRegistry registry, String key,
			String fileName) {
		try {
			IPath path = new Path("icons/" + fileName);
			URL url = find(path);
			if (url!=null) {
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				registry.put(key, desc);
			}
		} catch (Exception e) {
		}
	}
	
	public Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	
	public ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

	public static File getFileFromPlugin(String filePath){
		Bundle bundle = Activator.getDefault().getBundle();
		Path path = new Path(filePath); 
		URL url = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
		URL fileUrl = null;
		try {
			fileUrl = FileLocator.toFileURL(url);
		} catch (IOException e) {
			// Will happen if the file cannot be read for some reason
			e.printStackTrace();
		}
		return new File(fileUrl.getPath());
	}
	
	public FormColors getFormColors(Display display) {
		if (formColors == null) {
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}



}
