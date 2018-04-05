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

package com.embotics.vlm.plugin.mock;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;

import com.embotics.vlm.rest.v30.client.VCommanderClient;
import com.embotics.vlm.rest.v30.client.model.DeployedComponentInfo;
import com.embotics.vlm.rest.v30.client.model.VCommanderException;

/**
 * A mock Class for vCommander REST API v3 client
 *  
 * @author btarczali
 */
public class VCommanderClientMock extends VCommanderClient {

	private Long requestID = 0L;
	private List<DeployedComponentInfo> newServiceRequestResult = Collections.emptyList();
	private String publishedServiceName = null;
	private Long workflowDefinitionId = 0L;	
	private String runCommandWorkflowResult = null;
	private Long workflowId = 0L;
	
	private boolean throwException = false;
	private String exceptionText = StringUtils.EMPTY;

	
	public VCommanderClientMock() {
		super();
	}

	@Override
	public void close() {
	}

	@Override
	public String getSecurityToken() throws JSONException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return null;
	}

	@Override
	public Long getPublishedService(String serviceName) throws JSONException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return null;
	}

	@Override
	public String getPublishedServiceNameByPayload(String payload) throws JSONException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return publishedServiceName;
	}

	@Override
	public String getServiceRequestPayload(Long serviceID) throws JSONException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return null;
	}

	@Override
	public Long requestService(String payload) throws JSONException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return requestID;
	}

	@Override
	public List<DeployedComponentInfo> waitForServiceRequestToBeCompleted(Long requestId, long timeout, long polling, PrintStream logger) throws JSONException, InterruptedException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return newServiceRequestResult;
	}

	@Override
	public Long getWorkflowDefinition(String workflowName) throws JSONException, VCommanderException {
		return workflowDefinitionId;
	}

	@Override
	public String runCommandWorkflow(Long workflowId, String targetType, String targetName) throws JSONException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return runCommandWorkflowResult;
	}
	
	@Override
	public Long waitForWorkflowToBeCompleted(String taskId, long timeout, long polling, PrintStream logger) throws JSONException, InterruptedException, VCommanderException {
		if(throwException) {
			throw new VCommanderException(exceptionText);
		}
		return workflowId;
	}
	
	
	/////////
	//Setters

	public void setRequestID(Long requestID) {
		this.requestID = requestID;
	}

	public void setNewServiceRequestResult(List<DeployedComponentInfo> newServiceRequestResult) {
		this.newServiceRequestResult = newServiceRequestResult;
	}

	public void setThrowException(boolean throwException, String exceptionText) {
		this.throwException = throwException;
		this.exceptionText = exceptionText;
	}

	public void setPublishedServiceName(String publishedServiceName) {
		this.publishedServiceName = publishedServiceName;
	}

	public Long getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	public void setWorkflowDefinitionId(Long workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
	}

	public void setRunCommandWorkflowResult(String runCommandWorkflowResult) {
		this.runCommandWorkflowResult = runCommandWorkflowResult;
	}

	public void setWorkflowId(Long workflowId) {
		this.workflowId = workflowId;
	}
	
	
}
