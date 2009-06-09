package org.spagic3.deployer;

import java.io.File;
import java.util.Hashtable;

import org.osgi.service.component.ComponentFactory;

public interface IDeploymentService {
	
	public void deploy(String spagicId, String factoryName, Hashtable properties);
	public void undeploy(String spagicId);
}
