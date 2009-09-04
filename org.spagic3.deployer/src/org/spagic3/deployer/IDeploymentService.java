package org.spagic3.deployer;

import java.util.Hashtable;
import java.util.List;

public interface IDeploymentService {
	
	public static final String SPAGIC_SERVICE = "SPAGIC_SERVICE";
	public static final String SPAGIC_CONNECTOR = "SPAGIC_CONNECTOR";
	public static final String SPAGIC_DATASOURCE = "SPAGIC_DATASOURCE";
		
	public void deployService(String spagicId, String factoryName, Hashtable properties);
	public void undeployService(String spagicId);
	
	public void deployConnector(String spagicId, String factoryName, Hashtable properties);
	public void undeployConnector(String spagicId);
	
	public void deployDatasource(String dataSourceID, Hashtable properties);
	public void undeployDatasource(String dataSourceID);
	
	public void deployRoutes(List<String> updateRoutes, List<String> oldRoutes);
	
	
	
}
