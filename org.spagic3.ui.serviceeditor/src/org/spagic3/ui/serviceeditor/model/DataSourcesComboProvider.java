package org.spagic3.ui.serviceeditor.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stp.im.resources.IImResource;
import org.eclipse.stp.im.resources.ImResourcesActivator;
import org.eclipse.stp.im.resources.datasources.DataSourceImResource;

public class DataSourcesComboProvider implements IComboProvider {

	public DataSourcesComboProvider(String config) {
	}

	@Override
	public List<String> getComboItems() {
		List<IImResource> resourcesList =  ImResourcesActivator.getImResourcesWithType(DataSourceImResource.DATA_SOURCE_IM_RESOURCE_TYPE);
		List<String> values = new ArrayList<String>();
		for (IImResource resource : resourcesList){
			values.add(resource.getId());
		}
		return values;
	}

}
