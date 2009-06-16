package org.spagic3.components.bpm;

import java.util.concurrent.ConcurrentHashMap;

public class BPMContextSingleton {
	/**
	 * The name of the variable that in JBPM carries the XML Messages
	 */
	public static final String XML_MESSAGE = "XML_MESSAGE";
	
	public static final String WORKFLOW_UPDATER_CLASS = "WORKFLOW_UPDATER_CLASS";

	/**
	 * When a JBPM Process is started by BPMComponent we set two variable
	 * Originating ConnectorID
	 */
	
	public static final String ORCHESTRATION_SERVICE_ID = "ORCHESTRATION_SERVICE_ID";
	
	public static final String TOKEN_ID_PROPERTY = "Token";
	
	
	
	
	
	
	public static BPMContextSingleton instance = new BPMContextSingleton();
	public ConcurrentHashMap<String, BPMComponent> bpmComponents = new ConcurrentHashMap<String, BPMComponent>();
	public ConcurrentHashMap<String, BPMComponent> getBpmComponents() {
		return bpmComponents;
	}

	public void setBpmComponents(
			ConcurrentHashMap<String, BPMComponent> bpmComponents) {
		this.bpmComponents = bpmComponents;
	}

	public static BPMContextSingleton getInstance(){
		return instance;
	}
	
	public void register(BPMComponent bpmnComponent){
		getInstance().getBpmComponents().put(bpmnComponent.getSpagicId(), bpmnComponent);
	}
	
	public void unregister(BPMComponent bpmnComponent){
		getInstance().getBpmComponents().remove(bpmnComponent.getSpagicId());
	}
	
	public static void callBack(String orchestrationServiceId,  Long processInstanceId, String xmlMessage){
		BPMComponent bpmnComponent = getInstance().getBpmComponents().get(orchestrationServiceId);
		bpmnComponent.callBack(processInstanceId,  xmlMessage);
	}
	

	
}
