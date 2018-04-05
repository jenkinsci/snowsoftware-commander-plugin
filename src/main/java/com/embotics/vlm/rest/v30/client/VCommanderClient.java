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
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;

import com.embotics.vlm.rest.v30.client.model.DeployedComponentInfo;
import com.embotics.vlm.rest.v30.client.model.VCommanderException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * vCommander REST API Java client - connecting to REST API V3
 * Only methods relevant for the plugin are implemented
 * Authentication is based on basic-auth
 * 
 * @author btarczali
 *
 */
public class VCommanderClient {
	private static final String BASIC_AUTH_ENCODING = "UTF-8";
	private static final String BASIC_AUTH_HEATHER_KEY = "Authorization";
	private static final String BASIC_AUTH_HEATHER_VALUE_PREFIX = "Basic ";

	private static final String ROOT_SERVICE_PATH 				= "rest/v3";

	private String baseURL;
	private WebResource webResource;
	private Client client;
	private String userName;
	private String orgName;
	
	private SessionsClient sessionsClient;
	private ServiceRequestsClient serviceRequestsClient;
	private WorkflowsClient workflowsClient;

	
	public VCommanderClient(final String uri, final String userName, final String password, final String orgName) {
		this.baseURL = uri;
		this.userName = userName;
		this.orgName = orgName;

		String baseURL = uri;
		if (!baseURL.endsWith("/")) {
			baseURL = baseURL + "/";
		}

		// Java Runtime Environment has SSLv3 disabled by default so we need to enable
		// it
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

		/*
		 * Add security token to every request
		 */
		client = ClientHelper.createClient();
		client.addFilter(new ClientFilter() {
			@Override
			public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {

				//build the basic-auth authentication string 
				StringBuilder builder = new StringBuilder();
				builder.append(userName);
				if(StringUtils.isNotBlank(orgName)) {
					builder.append(";");
					builder.append(orgName);
				}
				builder.append(":");
				builder.append(password);
				
				//encode the authentication string 
				String authentication = builder.toString();
				byte[] base64Encoded = Base64.encodeBase64(authentication.getBytes(Charset.forName(BASIC_AUTH_ENCODING)));
				String base64String = new String(base64Encoded, Charset.forName(BASIC_AUTH_ENCODING));
				
				// add credentials as basic authentication to the header for each request
				cr.getHeaders().add(BASIC_AUTH_HEATHER_KEY, BASIC_AUTH_HEATHER_VALUE_PREFIX + base64String);

				return getNext().handle(cr);
			}
		});

		webResource = client.resource(baseURL).path(ROOT_SERVICE_PATH);
		sessionsClient = new SessionsClient(webResource, userName, password, orgName);
		serviceRequestsClient = new ServiceRequestsClient(webResource);
		workflowsClient = new WorkflowsClient(webResource);
	}

	/**
	 * Used for Unit Testing
	 * 
	 */
	protected VCommanderClient() {
	}

	/**
	 * Done with the rest client and clean-up
	 */
	public void close() {
		client.destroy();
	}
	
	/**
	 * @return A formatted string containing the configured vCommander address and user name
	 */
	public String getClientInfo() {
		String orgInfo = StringUtils.isNotBlank(orgName) ? String.format(" - in organization: %s", orgName) : StringUtils.EMPTY;
		return String.format("Configured vCommander is at: %s - with userName: %s%s", baseURL, userName, orgInfo);
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.SessionsClient#getSecurityToken()}
	 */
	public String getSecurityToken() throws JSONException, VCommanderException {
		return sessionsClient.getSecurityToken();
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.ServiceRequestsClient#getPublishedService(String)}
	 */
	public Long getPublishedService(String serviceName) throws JSONException, VCommanderException {
		return serviceRequestsClient.getPublishedService(serviceName);
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.ServiceRequestsClient#getPublishedServiceNameByPayload(String)}
	 */
	public String getPublishedServiceNameByPayload(String payload) throws JSONException, VCommanderException {
		return serviceRequestsClient.getPublishedServiceNameByPayload(payload);
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.ServiceRequestsClient#getServiceRequestPayload(Long)}
	 */
	public String getServiceRequestPayload(Long serviceID) throws JSONException, VCommanderException {
		return serviceRequestsClient.getServiceRequestPayload(serviceID);
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.ServiceRequestsClient#requestService(String)}
	 */
	public Long requestService(String payload) throws JSONException, VCommanderException {
		return serviceRequestsClient.requestService(payload);
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.ServiceRequestsClient#waitForServiceRequestToBeCompleted(Long, long, long, PrintStream)}
	 */
	public List<DeployedComponentInfo> waitForServiceRequestToBeCompleted(Long requestId, long timeout, long polling, PrintStream logger) throws JSONException, InterruptedException, VCommanderException {
		return serviceRequestsClient.waitForServiceRequestToBeCompleted(requestId, timeout, polling, logger);
	}
	
	/**
	 * {@link com.embotics.vlm.rest.v30.client.WorkflowsClient#getWorkflowDefinition(String)}
	 */
	public Long getWorkflowDefinition(String workflowName) throws JSONException, VCommanderException {
		return workflowsClient.getWorkflowDefinition(workflowName);
	}
	
	/**
	 * {@link com.embotics.vlm.rest.v30.client.WorkflowsClient#getWorkflowDefinitionTargetType(Long)}
	 */
	public String getWorkflowDefinitionTargetType(Long workflowDefinitionId) throws JSONException, VCommanderException {
		return workflowsClient.getWorkflowDefinitionTargetType(workflowDefinitionId);
	}

	/**
	 * {@link com.embotics.vlm.rest.v30.client.WorkflowsClient#runCommandWorkflow(Long, String, String)}
	 */
	public String runCommandWorkflow(Long workflowId, String targetType, String targetName) throws JSONException, VCommanderException {
		return workflowsClient.runCommandWorkflow(workflowId, targetType, targetName);
	}
	
	/**
	 * {@link com.embotics.vlm.rest.v30.client.WorkflowsClient#waitForWorkflowToBeCompleted(String, long, long, PrintStream)}
	 */
	public Long waitForWorkflowToBeCompleted(String taskId, long timeout, long polling, PrintStream logger) throws JSONException, InterruptedException, VCommanderException {
		return workflowsClient.waitForWorkflowToBeCompleted(taskId, timeout, polling, logger);
	}

}
