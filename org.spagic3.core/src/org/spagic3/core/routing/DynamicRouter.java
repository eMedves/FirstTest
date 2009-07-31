package org.spagic3.core.routing;

import java.util.ArrayList;
import java.util.List;

public class DynamicRouter implements IDynamicRouter {

	protected List<String> dynamicRouting = new ArrayList<String>();
	
	public String getTarget(String source) {
		for (String route : dynamicRouting){
			if (route.startsWith(source)){
				return route.substring(route.indexOf(";") + 1);
			}
		}
		return null;
	}

	
	public void updateRoutes(List<String> updatedRoutes, List<String> oldRoutes) {
		synchronized (dynamicRouting) {
			if (oldRoutes != null)
				dynamicRouting.removeAll(oldRoutes);
			if (updatedRoutes != null){
				dynamicRouting.addAll(updatedRoutes);
			}
		}
	}
	
}

