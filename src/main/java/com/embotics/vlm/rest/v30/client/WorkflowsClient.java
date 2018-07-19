/*************************************************************************
 *
 *	Copyright (c) 2017 Embotics Corporation. All Rights Reserved.
 *
 *	No part of this software may be reproduced, used in any
 *	information storage and retrieval system, or transmitted in
 *	any form or by any means, electronic, mechanical or otherwise,
 *	without the written permission of Embotics Corporation.
 *
 ***************************************************************************/

package com.embotics.vlm.rest.v30.client;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.embotics.vlm.rest.v30.client.model.Comment;
import com.embotics.vlm.rest.v30.client.model.VCommanderException;
import com.embotics.vlm.rest.v30.client.model.WorkflowTargetType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * vCommander REST API Java client - for workflows
 * 
 * @author btarczali
 *
 */
public class WorkflowsClient {
	private static final String WORKFLOW_DEFINITIONS_PATH 		= "workflow-definitions";
	private static final String TASKS_PATH 						= "tasks";
	private static final String WORKFLOWS_PATH 					= "workflows";
	private static final String WORKFLOW_COMMENTS_PATH 			= "comments";

	private static final String WORKFLOW_DEFINITION_NAME = "name";
	private static final String WORKFLOW_DEFINITION_TYPE = "type";
	private static final String WORKFLOW_DEFINITION_ID = "id";
	private static final String WORKFLOW_DEFINITION_TARGET_TYPE = "target_type";
	private static final String WORKFLOW_DEFINITION_RUN = "run";
	private static final String WORKFLOW_DEFINITION_RUN_ASYNC = "async";
	
	private static final String WORKFLOW_DEFINITION_TYPE_COMMAND = "COMMAND";
	private static final String WORKFLOW_DEFINITION_RUN_REQUEST_BODY_FORMAT_WITH_TARGET = "{\"target_name\":\"%s\",\"target_type\":\"%s\"}";
	private static final String WORKFLOW_DEFINITION_RUN_REQUEST_BODY_FORMAT = "{\"target_name\":null,\"target_type\":\"%s\"}";
	
	private static final String TASK_RELATED_OBJECTS = "related_objects";
	private static final String TASK_ID = "id";
	private static final String TASK_STATE = "state";

	//not all the states are listed - only the final ones
	private static final String TASK_STATE_COMPLETE = "COMPLETE";
	private static final String TASK_STATE_FAILED = "FAILED";
	private static final String TASK_STATE_CANCELLED = "CANCELLED";
	private static final String TASK_STATE_CANCEL_REQUESTED = "CANCEL_REQUESTED";
	
	private static final String WORKFLOW_STATUS = "status";
	
	//not all the statuses are listed - only the final ones
	private static final String WORKFLOW_STATUS_COMPLETED = "COMPLETED";
	private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
	private static final String WORKFLOW_STATUS_ERROR = "ERROR";
	
	private static final String MANAGED_OBJECT_TYPE = "type";
	private static final String MANAGED_OBJECT_TYPE_WORKFLOW = "WORKFLOW";
	private static final String MANAGED_OBJECT_ID = "id";

	private final WebResource webResource;


	public WorkflowsClient(WebResource webResource) {
		this.webResource = webResource;
	}

	
	/**
	 * Retrieve the command workflow definition with the given name.
	 * If the command workflow definition is not found exception will occur.
	 * 
	 * @param workflowName			The name of the workflow definition
	 * 
	 * @return workflowDefinitionID
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public Long getWorkflowDefinition(String workflowName) throws JSONException, VCommanderException {
		if (StringUtils.isBlank(workflowName)) {
			throw new VCommanderException("No workflowName provided.");
		}

		//build the REST filter to filter workflow definitions by name and type
		String typeFilter = ClientUtils.buildFilter(WORKFLOW_DEFINITION_TYPE, ClientUtils.RestFilterOperator.eq, WORKFLOW_DEFINITION_TYPE_COMMAND);
		String nameFilter = ClientUtils.buildFilter(WORKFLOW_DEFINITION_NAME, ClientUtils.RestFilterOperator.eq, ClientUtils.encapsulateInQuots(workflowName));
		String filter = ClientUtils.buildFilter(typeFilter, ClientUtils.RestFilterOperator.and, nameFilter);
		
		ClientResponse workflowDefinitionsResponse = webResource.path(WORKFLOW_DEFINITIONS_PATH)
				.queryParam(ClientUtils.REST_FILTER, filter)
				.accept(MediaType.APPLICATION_JSON)
				.get(ClientResponse.class);

		
		ClientUtils.checkResponse(workflowDefinitionsResponse, Status.OK.getStatusCode());

		String workflowDefinitionsStr = workflowDefinitionsResponse.getEntity(String.class);
		JSONObject workflowDefinitionsJSON = new JSONObject(workflowDefinitionsStr);
		JSONArray workflowDefinitions = workflowDefinitionsJSON.getJSONArray(ClientUtils.COMMON_ITEMS);
		if (workflowDefinitions.length() == 0) {
			throw new VCommanderException("Command Workflow Definition not found.");
		}
		//no need to check if multiple found, since name/type is unique in vCommander

		JSONObject workflowDefinition = (JSONObject) workflowDefinitions.get(0);
		return workflowDefinition.getLong(WORKFLOW_DEFINITION_ID);
	}
	
	/**
	 * Retrieve the command workflow target type for the specified workflow definition ID.
	 * If the command workflow definition is not found exception will occur.
	 * 
	 * @param workflowDefinitionId	The ID of the workflow definition
	 * 
	 * @return targetType
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public String getWorkflowDefinitionTargetType(Long workflowDefinitionId) throws JSONException, VCommanderException {
		if (workflowDefinitionId == null) {
			throw new VCommanderException("No workflowDefinitionId provided.");
		}

		ClientResponse workflowDefinitionResponse = webResource.path(WORKFLOW_DEFINITIONS_PATH)
														.path(workflowDefinitionId.toString())
														.accept(MediaType.APPLICATION_JSON)
														.get(ClientResponse.class);
		
		ClientUtils.checkResponse(workflowDefinitionResponse, Status.OK.getStatusCode());

		String workflowDefinitionStr = workflowDefinitionResponse.getEntity(String.class);
		JSONObject workflowDefinitionJSON = new JSONObject(workflowDefinitionStr);

		return workflowDefinitionJSON.getString(WORKFLOW_DEFINITION_TARGET_TYPE);
	}

	/**
	 * Run a Command Workflow in vCommander.
	 * This call will create a task in vCommander, which will be scheduled based on the queue.
	 * A task ID is returned, and user should query the task for updates.
	 * 
	 * @param workflowId			The ID of the workflow definition
	 * @param targetType			The type of the target
	 * @param targetName			The name of the target
	 * 
	 * @return String				Task ID
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public String runCommandWorkflow(Long workflowId, String targetType, String targetName) throws JSONException, VCommanderException {

		// validate target type
		if (StringUtils.isBlank(targetType)) {
			throw new VCommanderException("No targetType provided.");
		}
		
		// validate target name
		// for workflows without target
		if(WorkflowTargetType.NO_INVENTORY_TARGET.name().equals(targetType)) {
			// name cannot be specified
			if (StringUtils.isNotBlank(targetName)) {
				throw new VCommanderException("When targetType is NO_INVENTORY_TARGET, then targetName must be null: " + targetName);
			}
			
		// for workflows with target
		} else {
			// name must be specified
			if (StringUtils.isBlank(targetName)) {
				throw new VCommanderException("No targetName provided.");
			}	
		}

		// validate workflow ID
		if (workflowId == null) {
			throw new VCommanderException("No workflowId provided.");
		}

		// running the workflow
		String requestBody = WorkflowTargetType.NO_INVENTORY_TARGET.name().equals(targetType) 
								? String.format(WORKFLOW_DEFINITION_RUN_REQUEST_BODY_FORMAT, targetType)
								: String.format(WORKFLOW_DEFINITION_RUN_REQUEST_BODY_FORMAT_WITH_TARGET, targetName, targetType);
								
		ClientResponse runCommandResponse = webResource.path(WORKFLOW_DEFINITIONS_PATH).path(workflowId.toString()).path(WORKFLOW_DEFINITION_RUN).queryParam(WORKFLOW_DEFINITION_RUN_ASYNC, "true").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, requestBody);
		ClientUtils.checkResponse(runCommandResponse, Status.ACCEPTED.getStatusCode());

		JSONObject task = new JSONObject(runCommandResponse.getEntity(String.class)); 
		return task.getString(TASK_ID);
	}
	
	/**
	 * Waits until the workflow is completed or failed.
	 * If the workflow will not finish in the given timeout, an exception will be thrown
	 * When logger is not null, lookup messages will be printed
	 * 
	 * @param taskId 				The response received when the workflow was submitted
	 * @param timeout				Timeout in minutes
	 * @param polling				Polling interval in seconds
	 * @param logger				A PrintStream, used for logging
	 * 
	 * @return The workflow ID
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws InterruptedException	If the wait is interrupted
	 * @throws VCommanderException 	If something goes wrong
	 */
	public Long waitForWorkflowToBeCompleted(String taskId, long timeout, long polling, PrintStream logger) throws JSONException, InterruptedException, VCommanderException {
		if (StringUtils.isBlank(taskId)) {
			throw new VCommanderException("No taskId provided.");
		}

		Long workflowId = null;
		String workflowStatus = null;
		long startTime = System.currentTimeMillis();
		Set<String> loggedComments = new HashSet<>();

		do {
			if (workflowId == null) {
				workflowId = checkWorkflowTaskIfStartedAndWait(taskId, startTime, timeout, polling, logger);
			} else {
				workflowStatus = checkWorkflowIfCompletedAndWait(workflowId, loggedComments, startTime, timeout, polling, logger);
			}
		} while (!WORKFLOW_STATUS_COMPLETED.equals(workflowStatus));

		return workflowId;
	}

	private Long checkWorkflowTaskIfStartedAndWait(String taskId, long startTimeInMillis, long timeoutInMinutes, long pollingInSeconds, PrintStream logger) throws VCommanderException, JSONException, InterruptedException {
		ClientUtils.log(logger, "\tLooking up task with ID: " + taskId);

		JSONObject task = getTask(taskId, logger);
		String taskState = getTaskState(task);
		
		switch (taskState) {
		case TASK_STATE_FAILED:
			throw new VCommanderException("Task failed.");
			
		case TASK_STATE_CANCEL_REQUESTED:
		case TASK_STATE_CANCELLED:
			throw new VCommanderException("Task cancelled.");
			
		case TASK_STATE_COMPLETE:
		default:
			break;
		}
		
		Long workflowId = getWorkflowIdFromTask(task);

		if (workflowId != null) {
			ClientUtils.log(logger, "Task was started and has the related workflow ID: " + workflowId);
		} else {
			if(TASK_STATE_COMPLETE.equals(taskState)) {
				throw new VCommanderException("Task finished but doesn't have the workflow ID.");
			}

			long currentTimeInMillis = System.currentTimeMillis();
			if (startTimeInMillis + timeoutInMinutes * DateUtils.MILLIS_PER_MINUTE <= currentTimeInMillis) {
				throw new VCommanderException("Task doesn't have the workflow ID, and timeout reached: " + timeoutInMinutes + " minutes");
			} else {
				long toWait = Math.min(pollingInSeconds * DateUtils.MILLIS_PER_SECOND, startTimeInMillis + timeoutInMinutes * DateUtils.MILLIS_PER_MINUTE - currentTimeInMillis);
				ClientUtils.log(logger, "Task doesn't have the related workflow ID. Wait " + toWait / DateUtils.MILLIS_PER_SECOND + " seconds ...");
				Thread.sleep(toWait);
			}
		}
		return workflowId;
	}
	
	private String checkWorkflowIfCompletedAndWait(Long workflowId, Set<String> loggedComments, long startTimeInMillis, long timeoutInMinutes, long pollingInSeconds, PrintStream logger) throws VCommanderException, JSONException, InterruptedException {
		ClientUtils.log(logger, "Looking up workflow with ID: " + workflowId);
		JSONObject workflowJSON = getWorkflow(workflowId, logger);
		String workflowStatus = getWorkflowStatus(workflowJSON);
		
		if (logger != null) {
			// fetch the workflow comments, and add it to the log if they were not added yet
			List<Comment> comments = getWorkflowComments(workflowId, logger);
			for (Comment comment : comments) {
				String commentStr = comment.toString();
				if (!loggedComments.contains(commentStr)) {
					logger.println();
					logger.println("Comment: ");
					logger.println(commentStr);
					logger.println();

					loggedComments.add(commentStr);
				}
			}
		}
		
		switch (workflowStatus) {
		case WORKFLOW_STATUS_COMPLETED:
			break;

		case WORKFLOW_STATUS_ERROR:
			throw new VCommanderException("Workflow failed.");
			
		case WORKFLOW_STATUS_REJECTED:
			throw new VCommanderException("Workflow rejected.");

		default:
			long currentTimeInMillis = System.currentTimeMillis();
			if (startTimeInMillis + timeoutInMinutes * DateUtils.MILLIS_PER_MINUTE <= currentTimeInMillis) {
				throw new VCommanderException("Workflow did not completed in the given timeout: " + timeoutInMinutes + " minutes");
			} else {
				long toWait = Math.min(pollingInSeconds * DateUtils.MILLIS_PER_SECOND, startTimeInMillis + timeoutInMinutes * DateUtils.MILLIS_PER_MINUTE - currentTimeInMillis);
				ClientUtils.log(logger, "Workflow status is: " + workflowStatus + ". Wait " + toWait / DateUtils.MILLIS_PER_SECOND + " seconds ...");
				Thread.sleep(toWait);
			}
			break;
		}
		
		return workflowStatus;
	}
	
	private List<Comment> getWorkflowComments(Long workflowId, PrintStream logger) {
		try {
			ClientResponse commentsResponse = webResource.path(WORKFLOWS_PATH).path(workflowId.toString()).path(WORKFLOW_COMMENTS_PATH).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			return ClientUtils.getComments(commentsResponse);
		} catch (Exception e) {
			ClientUtils.log(logger, "\tError while looking up workflow comments for workflow with ID: " + workflowId + " Message: " + e.getMessage());
			return Collections.emptyList();
		}
	}
	
	private JSONObject getTask(String taskId, PrintStream logger) {
		try {
			ClientResponse requestResponse = webResource.path(TASKS_PATH).path(taskId).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			ClientUtils.checkResponse(requestResponse, Status.OK.getStatusCode());
	
			String requestStr = requestResponse.getEntity(String.class);
			return new JSONObject(requestStr);
		} catch (Exception e) {
			ClientUtils.log(logger, "\tError while looking up task with ID: " + taskId + " Message: " + e.getMessage());
			return null;
		}
	}
	
	private String getTaskState(JSONObject task) throws JSONException {
		if(task != null) {
			return task.getString(TASK_STATE);
		}
		return StringUtils.EMPTY;
	}
	
	private Long getWorkflowIdFromTask(JSONObject task) throws JSONException {
		if(task!=null && !task.isNull(TASK_RELATED_OBJECTS)) {
			JSONArray relatedObjects = task.getJSONArray(TASK_RELATED_OBJECTS);
			for(int i=0; i<relatedObjects.length(); i++) {
				JSONObject relatedObject = (JSONObject) relatedObjects.get(i);
				if(MANAGED_OBJECT_TYPE_WORKFLOW.equals(relatedObject.getString(MANAGED_OBJECT_TYPE))) {
					return relatedObject.getLong(MANAGED_OBJECT_ID);
				}
			}
		}
		return null;
	}
	
	private String getWorkflowStatus(JSONObject workflow) throws JSONException {
		if(workflow != null) {
			return workflow.getString(WORKFLOW_STATUS);
		}
		return StringUtils.EMPTY;
	}
	
	private JSONObject getWorkflow(Long workflowId, PrintStream logger) {
		try {
			ClientResponse requestResponse = webResource.path(WORKFLOWS_PATH).path(workflowId.toString()).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			ClientUtils.checkResponse(requestResponse, Status.OK.getStatusCode());
	
			String requestStr = requestResponse.getEntity(String.class);
			return new JSONObject(requestStr);
		} catch (Exception e) {
			ClientUtils.log(logger, "\tError while looking up workflow with ID: " + workflowId + " Message: " + e.getMessage());
			return null;
		}
	}

}
