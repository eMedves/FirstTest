/*******************************************************************************
 * Copyright (c) {2007, 2008} Engineering Ingegneria Informatica S.p.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Andrea Zoppello (Engineering) - initial API and implementation
 *******************************************************************************/
package org.spagic3.ui.servicerepository.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.spagic3.ui.servicerepository.IServiceResource;


public class ServiceRepositoryResource implements IServiceResource {
	
	private String id = null;
	private Map<String, String> serviceProperties = null;
	
	public static String SERVICE_ID = "serviceId";
	public static String NAME = "name";
	public static String TARGET = "target";

	public ServiceRepositoryResource(String id) {
		this.id = id;
		serviceProperties = new HashMap<String, String>();
		serviceProperties.put(SERVICE_ID, id);
	}
		

	public void delProperty(String propertyName) {
		serviceProperties.remove(propertyName);
	}

	public String getId() {
		return id;
	}

	public String getProperty(String propertyName) {
		
		return serviceProperties.get(propertyName);
	}

	public String[] getPropertyLabels() {
		String[] labels = new String[3];
		
		labels[0] = SERVICE_ID;
		labels[1] = NAME;
		labels[2] = TARGET;

		return labels;
	}

	public String[] getPropertyNames() {
		String[] names = new String[3];
		
		names[0] = SERVICE_ID;
		names[1] = NAME;
		names[2] = TARGET;
		
		return names;
	}

	public String getResourceType() {
		return IServiceResource.SERVICE_RESOURCE_TYPE;
	}

	public void setProperty(String propertyName, String propertyValue) {
		serviceProperties.put(propertyName, propertyValue);
	}
	
	

	
	
	
}
