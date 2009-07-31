package org.spagic3.core.routing;

import java.util.List;

public interface IDynamicRouter {

	public String getTarget(String source);
	
	
	public void updateRoutes(List<String> routes, List<String> oldRoutes);
	
	
}
