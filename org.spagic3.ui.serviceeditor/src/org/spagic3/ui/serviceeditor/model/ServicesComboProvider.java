package org.spagic3.ui.serviceeditor.model;

import java.util.ArrayList;
import java.util.List;

import org.spagic3.ui.servicerepository.IServiceResource;
import org.spagic3.ui.servicerepository.ServiceRepositoryActivator;

public class ServicesComboProvider implements IComboProvider {

	public ServicesComboProvider(String config) {
	}

	@Override
	public List<String> getComboItems() {
		List<IServiceResource> resourcesList = ServiceRepositoryActivator.getResourcesWithType(IServiceResource.SERVICE_RESOURCE_TYPE);
		List<String> values = new ArrayList<String>();
		for (IServiceResource resource : resourcesList){
			values.add(resource.getId());
		}
		return values;
	}

}
