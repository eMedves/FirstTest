/**

    Copyright 2007, 2009 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 **/
package org.spagic3.components.bpm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.activation.DataHandler;
import javax.annotation.PreDestroy;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic.workflow.api.CmdObject;
import org.spagic.workflow.api.IControlAPI;
import org.spagic.workflow.api.IControlExtendedAPI;
import org.spagic.workflow.api.IProcessEngine;
import org.spagic.workflow.api.IQueryAPI;
import org.spagic.workflow.api.Process;
import org.spagic.workflow.api.ProcessInstance;
import org.spagic.workflow.api.ResultObject;
import org.spagic.workflow.api.SubProcessTask;
import org.spagic.workflow.api.Task;
import org.spagic.workflow.api.TaskInstance;
import org.spagic.workflow.api.Variable;
import org.spagic3.components.bpm.BPMComponent;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.resources.IResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;

public class BPMService extends BaseSpagicService {

	protected Logger log = LoggerFactory.getLogger(BPMComponent.class);
	
	private final AtomicReference<IProcessEngine> delegatedEngine = new AtomicReference<IProcessEngine>(null);
	
	//protected IProcessEngine delegatedEngine = null;
	private static String notifyUrl = null;
	private static final Properties jbpmNotifyProps = new Properties();
	
	private static final String PARAM_PROCESSINSTANCEID = "processInstanceId".intern();
	private static final String PARAM_PROCESSNAME = "processName".intern();
	private static final String PARAM_TASKNAME = "taskName".intern();
	private static final String PARAM_STOP_FLAG = "stopFlag".intern();

	public IProcessEngine getDelegatedEngine() {
		return delegatedEngine.get();
	}

	public void unsetDelegatedEngine(IProcessEngine processEngine) {
		this.delegatedEngine.compareAndSet(processEngine, null);
	}
	
	public void setDelegatedEngine(IProcessEngine processEngine) {
		this.delegatedEngine.set(processEngine);
	}
	
	public void init() {
		log.info(" Initializing BPM Service ");
		IResource res = propertyConfigurator.getResource("properties://jbpmse.properties");
		if (res != null){
			try {
				jbpmNotifyProps.load(res.openStream());
				log.info(" Initialization OK ");
			} catch (Exception e) {
				log.warn("Unable to load properties file \"jbpmse.properties\"", e);
			}
		}
			

	}

	@Override
	public boolean run(Exchange exchange, Message in, Message out) throws Exception {
		ResultObject resObj = new ResultObject();

		try {
			log.debug(" BPM Service Engine :: transform -> start");

			resObj = execute(getCommandObject(in));
			resObj.setErrorsOccured(false);
		} catch (Throwable e) {

			resObj.setErrorMessage(e.getMessage());
			resObj.setErrorsOccured(true);
		}

		setResponseInOutputMessage(out, resObj);

		log.debug(" BPM Service Engine :: transform end ");
		return true;
	}

	public CmdObject getCommandObject(Message in) throws Exception {
		log.debug(" JBPM Service Engine :: getCommandObject -> start");

		CmdObject cmdObject = null;
		XStream xStreamCmdObject = new XStream(new Dom4JDriver());
		xStreamCmdObject.alias("command", CmdObject.class);

		String xmlInputMessage = (String)in.getBody();

		Document doc = DocumentHelper.parseText(xmlInputMessage);
		String cmdObjectAsEncodedXMLText = doc.getRootElement().getText();
		String cmdObjectXML = DocumentHelper.parseText(cmdObjectAsEncodedXMLText).asXML();
		log.debug(" JBPM Service Engine :: getCommandObject -> Command Object as XML  -> " + cmdObjectXML);
		log.debug(" JBPM Service Engine :: getCommandObject -> Returning command Object ");
		cmdObject = (CmdObject) xStreamCmdObject.fromXML(cmdObjectXML);
		return cmdObject;

	}

	public void setResponseInOutputMessage(Message out, ResultObject resObj) throws Exception {
		XStream xStreamResObject = new XStream(new Dom4JDriver());
		xStreamResObject.alias("result", ResultObject.class);
		String xmlResponse = xStreamResObject.toXML(resObj);

		Document responseDoc = DocumentHelper.createDocument();
		QName qName = new QName("response", new Namespace("spagic", "urn:eng:spagic"));
		responseDoc.addElement(qName).addText(xmlResponse);

		out.setBody(responseDoc.asXML());
		if (resObj.getAttachments() != null) {
			List<DataHandler> attachments = resObj.getAttachments();
			for (DataHandler attachment : attachments) {
				out.addAttachment(attachment.getName(), attachment);
			}
		}
	}

	@PreDestroy
	public void destroy() {
		getDelegatedEngine().stop();
	}

	public ResultObject execute(CmdObject cmdObj) {
		String method = cmdObj.getCompleteMethodName();
		ResultObject resObject = new ResultObject();
		try {
			boolean executed = executeControApi(method, cmdObj, resObject);
			if (!executed)
				executed = executeControExtendAPI(method, cmdObj, resObject);
			if (!executed)
				executed = executeQueryApi(method, cmdObj, resObject);
			if (!executed) {
				resObject.setErrorsOccured(true);
				resObject.setErrorMessage("-- Method not Recognized --");
				log.error(" -- Method not Recognized -- ");
			}
		} catch (Throwable t) {
			resObject.setErrorsOccured(true);
			resObject.setErrorMessage(t.getMessage());
			log.error("Error in JBPM ServiceEngine", t);
		}
		return resObject;
	}

	protected boolean executeControApi(String methodName, CmdObject commandObject, ResultObject resultObject) {
		boolean executed = false;
		if (methodName.equalsIgnoreCase(CmdObject.START_BY_PROCESS_NAME)) {
			String processName = (String) commandObject.parametersMap.get(PARAM_PROCESSNAME);
			long pid = getControlAPI().startByProcessName(processName);
			if (notifyUrl != null) {
				getControlAPI().setGlobalVariable(pid, "_SPAGIC_NOTIFY_URL", notifyUrl);
			}
			resultObject.setResult(pid);
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.ABORT)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			getControlAPI().abort(processInstanceId);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.RESTART)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			getControlAPI().restart(processInstanceId);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.STOP_ON_AUTOMATIC_TASK)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			boolean stop = (Boolean) commandObject.parametersMap.get(PARAM_STOP_FLAG);
			getControlAPI().stopOnAutomaticTask(processInstanceId, stop);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.EXECUTE_TASK_BY_TASK_NAME)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().executeTask(processInstanceId, taskName, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.EXECUTE_TASK_BY_TASK_ID)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskId = (Long) commandObject.parametersMap.get("taskId");
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().executeTask(processInstanceId, taskId, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.EXECUTE_TASK_WITH_STATUS_BY_TASK_NAME)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			String taskStatus = (String) commandObject.parametersMap.get("taskStatus");
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().executeTaskWithStatus(processInstanceId, taskName, taskStatus, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.EXECUTE_TASK_WITH_STATUS_BY_TASK_ID)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskId = (Long) commandObject.parametersMap.get("taskId");
			String taskStatus = (String) commandObject.parametersMap.get("taskStatus");
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().executeTaskWithStatus(processInstanceId, taskId, taskStatus, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.EXECUTE_TASK_BY_ACTOR_AND_TASK_NAME)) {
			String actor = (String) commandObject.parametersMap.get("actor");
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().executeTaskByActor(actor, processInstanceId, taskName, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.EXECUTE_TASK_BY_ACTOR_AND_TASK_ID)) {
			String actor = (String) commandObject.parametersMap.get("actor");
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskId = (Long) commandObject.parametersMap.get("taskId");
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().executeTaskByActor(actor, processInstanceId, taskId, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.GET_GLOBAL_VARIABLE)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String variableName = (String) commandObject.parametersMap.get("variableName");
			Variable result = getControlAPI().getGlobalVariable(processInstanceId, variableName);
			resultObject.setResult(result);
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.GET_GLOBAL_VARIABLES)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			Variable[] result = getControlAPI().getGlobalVariables(processInstanceId);
			resultObject.setResult(result);
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.SET_GLOBAL_VARIABLE)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String variableName = (String) commandObject.parametersMap.get("variableName");
			Object variableValue = commandObject.parametersMap.get("variableValue");
			getControlAPI().setGlobalVariable(processInstanceId, variableName, variableValue);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.SET_GLOBAL_VARIABLES)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			Variable[] variables = (Variable[]) commandObject.parametersMap.get("variables");
			getControlAPI().setGlobalVariables(processInstanceId, variables);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.GET_TASK_VARIABLE)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String variableName = (String) commandObject.parametersMap.get("variableName");
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable result = getControlAPI().getTaskVariable(processInstanceId, taskName, variableName);
			resultObject.setResult(result);
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.GET_TASK_VARIABLES)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable[] result = getControlAPI().getTaskVariables(processInstanceId, taskName);
			resultObject.setResult(result);
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.SET_TASK_VARIABLE)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			String variableName = (String) commandObject.parametersMap.get("variableName");
			Object variableValue = commandObject.parametersMap.get("variableValue");
			getControlAPI().setTaskVariable(processInstanceId, taskName, variableName, variableValue);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.SET_TASK_VARIABLES)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable[] variables = (Variable[]) commandObject.parametersMap.get("variables");
			getControlAPI().setTaskVariables(processInstanceId, taskName, variables);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.LINK_PROCESSES)) {
			long firstProcessInstanceId = (Long) commandObject.parametersMap.get("firstProcessInstanceId");
			long secondProcessInstanceId = (Long) commandObject.parametersMap.get("secondProcessInstanceId");
			getControlAPI().linkProcesses(firstProcessInstanceId, secondProcessInstanceId);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.START_TASK_BY_TASK_NAME)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().startTask(processInstanceId, taskName, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.START_TASK_BY_TASK_ID)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskId = (Long) commandObject.parametersMap.get("taskId");
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().startTask(processInstanceId, taskId, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.START_TASK_BY_ACTOR_AND_TASK_NAME)) {
			String actor = (String) commandObject.parametersMap.get("actor");
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().startTaskByActor(actor, processInstanceId, taskName, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.START_TASK_BY_ACTOR_AND_TASK_ID)) {
			String actor = (String) commandObject.parametersMap.get("actor");
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskId = (Long) commandObject.parametersMap.get("taskId");
			Variable[] vars = (Variable[]) commandObject.parametersMap.get("vars");
			getControlAPI().startTaskByActor(actor, processInstanceId, taskId, vars);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.ADVANCE_TASK_COMPLETION_BY_TASK_NAME)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String taskName = (String) commandObject.parametersMap.get(PARAM_TASKNAME);
			Integer completionPercentage = (Integer) commandObject.parametersMap.get("completionPercentage");
			getControlAPI().advanceTaskCompletion(processInstanceId, taskName, completionPercentage);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.ADVANCE_TASK_COMPLETION_BY_TASK_ID)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskId = (Long) commandObject.parametersMap.get("taskId");
			Integer completionPercentage = (Integer) commandObject.parametersMap.get("completionPercentage");
			getControlAPI().advanceTaskCompletion(processInstanceId, taskId, completionPercentage);
			resultObject.setResult("VOID");
			executed = true;
		}
		return executed;
	}

	protected boolean executeControExtendAPI(String methodName, CmdObject commandObject, ResultObject resultObject) {
		boolean executed = false;
		if (methodName.equalsIgnoreCase(CmdObject.FORCE_TOKEN_WITH_SINGLE_TOKEN)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String destTaskName = (String) commandObject.parametersMap.get("destTaskName");
			getControlExtendedAPI().forceToken(processInstanceId, destTaskName);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.FORCE_TOKEN_WITH_MULTIPLE_TOKENS)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String sourceTaskName = (String) commandObject.parametersMap.get("sourceTaskName");
			String destTaskName = (String) commandObject.parametersMap.get("destTaskName");
			getControlExtendedAPI().forceToken(processInstanceId, sourceTaskName, destTaskName);
			resultObject.setResult("VOID");
			executed = true;
		} else if (methodName.equalsIgnoreCase(CmdObject.ARE_NODES_ON_SAME_TOKEN)) {
			long processInstanceId = (Long) commandObject.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String sourceTaskName = (String) commandObject.parametersMap.get("sourceTaskName");
			String destTaskName = (String) commandObject.parametersMap.get("destTaskName");
			boolean result = getControlExtendedAPI().areNodesOnSameToken(processInstanceId, sourceTaskName,
					destTaskName);
			resultObject.setResult(new Boolean(result));
			executed = true;
		}
		return executed;
	}

	protected boolean executeQueryApi(String method, CmdObject cmdObj, ResultObject resObject) {
		boolean executed = false;
		if (method.equalsIgnoreCase(CmdObject.GET_PROCESS_GRAPH)) {
			long processId = (Long) cmdObj.parametersMap.get("processId");
			String graph = getQueryAPI().getProcessGraph(processId);
			resObject.setResult(graph);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_PROCESS_BY_ID)) {
			long processId = (Long) cmdObj.parametersMap.get("processId");
			Process process = getQueryAPI().getProcess(processId);
			resObject.setResult(process);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_PROCESS_BY_NAME)) {
			String processName = (String) cmdObj.parametersMap.get(PARAM_PROCESSNAME);
			Process process = getQueryAPI().getProcess(processName);
			resObject.setResult(process);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_ALL_PROCESSES)) {
			Process[] result = getQueryAPI().getProcesses();
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_ALL_PROCESSES_FILTERED_BY)) {
			String attributeName = (String) cmdObj.parametersMap.get("attributeName");
			String attributeValue = (String) cmdObj.parametersMap.get("attributeValue");
			boolean onlyStandalone = (Boolean) cmdObj.parametersMap.get("onlyStandalone");
			Process[] result = getQueryAPI().getProcessesByFilter(attributeName, attributeValue,
					onlyStandalone);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASKS_BY_PROCESS_ID)) {
			long processId = (Long) cmdObj.parametersMap.get("processId");
			Task[] result = getQueryAPI().getHumanTask(processId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASKS_BY_PROCESS_NAME)) {
			String processName = (String) cmdObj.parametersMap.get(PARAM_PROCESSNAME);
			Task[] result = getQueryAPI().getHumanTask(processName);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_SUBPROCESS_BY_PROCESS_ID)) {
			long processId = (Long) cmdObj.parametersMap.get("processId");
			Process[] result = getQueryAPI().getSubProcesses(processId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_SUBPROCESS_BY_PROCESS_NAME)) {
			String processName = (String) cmdObj.parametersMap.get(PARAM_PROCESSNAME);
			Process[] result = getQueryAPI().getSubProcesses(processName);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_ALL_PROCESSES_INSTANCES)) {
			ProcessInstance[] result = getQueryAPI().getProcessInstances();
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_ALL_PROCESSES_INSTANCES_FOR_PROCESS_BY_ID)) {
			long processId = (Long) cmdObj.parametersMap.get("processId");
			ProcessInstance[] result = getQueryAPI().getProcessInstances(processId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_ALL_PROCESSES_INSTANCES_FOR_PROCESS_BY_NAME)) {
			String processName = (String) cmdObj.parametersMap.get(PARAM_PROCESSNAME);
			ProcessInstance[] result = getQueryAPI().getProcessInstances(processName);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_SUBPROCESS_INSTANCES_BY_PARENT_PROCESS_ID)) {
			long parentProcessInstanceId = (Long) cmdObj.parametersMap.get("parentProcessInstanceId");
			ProcessInstance[] result = getQueryAPI().getSubProcessInstances(parentProcessInstanceId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_PROCESS_INSTANCE_BY_ID)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			ProcessInstance result = getQueryAPI().getProcessInstanceById(processInstanceId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASKS_INSTANCES_FOR_PROCESS_INSTANCE)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			TaskInstance[] result = getQueryAPI().getHumanTaskInstances(processInstanceId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASKS_INSTANCES_FOR_PROCESS_INSTANCE_FILTERED)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String attributeName = (String) cmdObj.parametersMap.get("attributeName");
			String attributeValue = (String) cmdObj.parametersMap.get("attributeValue");
			TaskInstance[] result = getQueryAPI().getHumanTaskInstances(processInstanceId, attributeName,
					attributeValue);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_PROCESS_INSTANCE_GRAPH)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String graph = getQueryAPI().getProcessInstanceGraph(processInstanceId);
			resObject.setResult(graph);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_LINKED_PROCESS_INSTANCES_FOR_PROCESS_INSTANCE)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			ProcessInstance[] result = getQueryAPI().getLinkedProcessInstances(processInstanceId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASK_INSTANCE_STATUS)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			long taskInstanceId = (Long) cmdObj.parametersMap.get("taskInstanceId");
			String result = getQueryAPI().getTaskInstanceStatus(processInstanceId, taskInstanceId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_PROCESS_INSTANCE_STATUS)) {
			long processInstanceId = (Long) cmdObj.parametersMap.get(PARAM_PROCESSINSTANCEID);
			String result = getQueryAPI().getProcessInstanceStatus(processInstanceId);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASK_OUTGOING_VALUES_BY_PROCESS_NAME_AND_TASK_NAME)) {
			String processName = (String) cmdObj.parametersMap.get(PARAM_PROCESSNAME);
			String taskName = (String) cmdObj.parametersMap.get(PARAM_TASKNAME);
			String[] result = getQueryAPI().getHumanTaskOutgoingValues(processName, taskName);
			resObject.setResult(result);

			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_HUMAN_TASK_OUTGOING_VALUES_BY_PROCESS_ID_AND_TASK_NAME)) {
			Long processId = (Long) cmdObj.parametersMap.get("processId");
			String taskName = (String) cmdObj.parametersMap.get(PARAM_TASKNAME);
			String[] result = getQueryAPI().getHumanTaskOutgoingValues(processId, taskName);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_LATEST_DEFINITION_PROCESSES)) {
			Process[] result = getQueryAPI().getLatestDefinitionProcesses();
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_LATEST_DEFINITION_PROCESSES_ONLY_STANDALONE)) {

			Boolean onlyStandalone = (Boolean) cmdObj.parametersMap.get("onlyStandalone");
			Process[] result = getQueryAPI().getLatestDefinitionProcesses(onlyStandalone);

			resObject.setResult(result);

			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_LATEST_DEFINITION_PROCESSES_BY_FILTER)) {

			String attributeName = (String) cmdObj.parametersMap.get("attributeName");
			String attributeValue = (String) cmdObj.parametersMap.get("attributeValue");
			Boolean onlyStandalone = (Boolean) cmdObj.parametersMap.get("onlyStandalone");

			Process[] result = getQueryAPI().getLatestDefinitionProcessesByFilter(attributeName,
					attributeValue, onlyStandalone);
			resObject.setResult(result);

			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_ALL_PROCESSES_ONLY_STANDALONE)) {

			Boolean onlyStandalone = (Boolean) cmdObj.parametersMap.get("onlyStandalone");

			Process[] result = getQueryAPI().getProcesses(onlyStandalone);
			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_SUBPROCESS_TASKS_BY_PROCESS_ID)) {

			long processId = (Long) cmdObj.parametersMap.get("processId");
			SubProcessTask[] result = getQueryAPI().getSubprocessTasks(processId);

			resObject.setResult(result);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_SUBPROCESS_TASKS_BY_PROCESS_NAME)) {

			String processName = (String) cmdObj.parametersMap.get(PARAM_PROCESSNAME);
			SubProcessTask[] result = getQueryAPI().getSubprocessTasks(processName);

			resObject.setResult(result);

			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_TASK_DOCUMENTATION)) {
			Map<String, Object> params = cmdObj.parametersMap;
			String processName = (String) params.get(PARAM_PROCESSNAME);
			String taskName = (String) params.get(PARAM_TASKNAME);
			IQueryAPI queryAPI = getQueryAPI();
			DataHandler dh = queryAPI.getTaskDocumentation(processName, taskName);
			List<DataHandler> attachment = new ArrayList<DataHandler>();
			attachment.add(dh);
			resObject.setAttachments(attachment);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_GENERATED_HUMAN_TASKS_INSTANCES)) {
			IQueryAPI queryAPI = getQueryAPI();
			TaskInstance[] ti = queryAPI.getGeneratedHumanTaskInstances();
			resObject.setResult(ti);
			executed = true;
		} else if (method.equalsIgnoreCase(CmdObject.GET_GENERATED_HUMAN_TASKS_INSTANCES_FILTERED)) {
			Map<String, Object> params = cmdObj.parametersMap;
			IQueryAPI queryAPI = getQueryAPI();
			String processName = (String) params.get(PARAM_PROCESSNAME);
			String taskName = (String) params.get(PARAM_TASKNAME);
			Date createFrom = (Date) params.get("createFrom");
			Date createTo = (Date) params.get("createTo");
			Date dueDateFrom = (Date) params.get("dueDateFrom");
			Date dueDateTo = (Date) params.get("dueDateTo");
			Integer state = (Integer) params.get("state");
			Long processInstanceId = (Long) params.get(PARAM_PROCESSINSTANCEID);
			TaskInstance[] ti = queryAPI.getGeneratedHumanTaskInstances(processName, taskName, createFrom, createTo,
					dueDateFrom, dueDateTo, state, processInstanceId);
			resObject.setResult(ti);
			executed = true;
		}
		return executed;
	}
	
	protected IQueryAPI getQueryAPI(){
		return getDelegatedEngine().getQueryAPI();
	}
	
	protected IControlAPI getControlAPI(){
		return getDelegatedEngine().getControlAPI();
	}
	
	protected IControlExtendedAPI getControlExtendedAPI(){
		return getDelegatedEngine().getControlExtendedAPI();
		
	}

}
