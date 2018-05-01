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
import java.util.ArrayList;
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
import com.embotics.vlm.rest.v30.client.model.DeployedComponentInfo;
import com.embotics.vlm.rest.v30.client.model.VCommanderException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * vCommander REST API Java client - for service requests
 * 
 * @author btarczali
 *
 */
public class ServiceRequestsClient {
	private static final String SERVICES_PATH 					= "services";
	private static final String SERVICE_REQUESTS_PATH 			= "service-requests";
	private static final String SERVICE_REQUEST_COMMENTS_PATH 	= "comments";


	private static final String PUBLISHED_SERVICE_NAME = "name";
	private static final String PUBLISHED_SERVICE_FORM = "request-form";
	private static final String PUBLISHED_SERVICE_ID = "id";
	private static final String PUBLISHED_SERVICE_FORM_SERVICE_ID = "service_id";

	private static final String REQUESTED_SERVICE_SUMMARY = "summary";
	private static final String REQUESTED_SERVICE_ID = "id";
	private static final String REQUESTED_SERVICE_STATE = "state";
	private static final String REQUESTED_SERVICE_SERVICES = "services";
	private static final String REQUESTED_SERVICE_SERVICE_COMPONENTS = "components";
	private static final String REQUESTED_SERVICE_SERVICE_COMPONENT_SUMMARY = "summary";
	private static final String REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT = "deployed_object";
	private static final String REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT_NAME = "name";
	private static final String REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT_TYPE = "type";

	//not all the states are listed - only the final ones
	private static final String REQUESTED_SERVICE_STATE_COMPLETED = "COMPLETED";
	private static final String REQUESTED_SERVICE_STATE_REJECTED = "REJECTED";
	private static final String REQUESTED_SERVICE_STATE_FAILED = "FAILED";
	
	
	private final WebResource webResource;

	
	public ServiceRequestsClient(WebResource webResource) {
		this.webResource = webResource;
	}


	/**
	 * Retrieve the Published service with the given name.
	 * If service not found or if multiple are found with the same name, exception will occur.
	 * 
	 * @param serviceName			The name of the published service
	 * 
	 * @return publishedServiceID
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public Long getPublishedService(String serviceName) throws JSONException, VCommanderException {
		if (StringUtils.isBlank(serviceName)) {
			throw new VCommanderException("No serviceName provided.");
		}

		String filter = ClientUtils.buildFilter(PUBLISHED_SERVICE_NAME, ClientUtils.RestFilterOperator.eq, ClientUtils.encapsulateInQuots(serviceName));
		ClientResponse servicesResponse = webResource.path(SERVICES_PATH).queryParam(ClientUtils.REST_FILTER, filter).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		ClientUtils.checkResponse(servicesResponse, Status.OK.getStatusCode());

		String servicesStr = servicesResponse.getEntity(String.class);
		JSONObject servicesJSON = new JSONObject(servicesStr);
		JSONArray services = servicesJSON.getJSONArray(ClientUtils.COMMON_ITEMS);
		if (services.length() == 0) {
			throw new VCommanderException("Service not found.");
		} else if (services.length() > 1) {
			// throw exception
			throw new VCommanderException("More than 1 Service found with name: " + serviceName);
		}

		JSONObject publishedService = (JSONObject) services.get(0);
		return publishedService.getLong(PUBLISHED_SERVICE_ID);
	}

	/**
	 * Retrieve the Published service name for the given payload
	 * 
	 * @param payload				The service request payload
	 * 
	 * @return publishedServiceName
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public String getPublishedServiceNameByPayload(String payload) throws JSONException, VCommanderException {
		if (StringUtils.isBlank(payload)) {
			throw new VCommanderException("No payload provided.");
		}

		JSONObject payloadJSON = new JSONObject(payload);
		if (payloadJSON.isNull(PUBLISHED_SERVICE_FORM_SERVICE_ID)) {
			throw new VCommanderException("Payload is missing the '" + PUBLISHED_SERVICE_FORM_SERVICE_ID + "' element");
		}
		Long publishedServiceId = payloadJSON.getLong(PUBLISHED_SERVICE_FORM_SERVICE_ID);

		String filter = ClientUtils.buildFilter(PUBLISHED_SERVICE_ID, ClientUtils.RestFilterOperator.eq, publishedServiceId.toString());
		ClientResponse servicesResponse = webResource.path(SERVICES_PATH).queryParam(ClientUtils.REST_FILTER, filter).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		
		ClientUtils.checkResponse(servicesResponse, Status.OK.getStatusCode());

		String servicesStr = servicesResponse.getEntity(String.class);
		JSONObject servicesJSON = new JSONObject(servicesStr);
		JSONArray servicesListJSON = servicesJSON.getJSONArray(ClientUtils.COMMON_ITEMS);
		if(servicesListJSON.length()>0) {
			JSONObject serviceJSON = (JSONObject) servicesListJSON.get(0);
			return serviceJSON.getString(PUBLISHED_SERVICE_NAME);
		} else {
			throw new VCommanderException("Published service not found with ID: " + publishedServiceId);	
		}
	}

	/**
	 * Retrieve the form required for a published service
	 * 
	 * @param serviceID				The published service ID
	 * 
	 * @return The service request form in JSON format
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public String getServiceRequestPayload(Long serviceID) throws JSONException, VCommanderException {
		if (serviceID == null) {
			throw new VCommanderException("No serviceID provided.");
		}

		ClientResponse publishedServicePayloadResponse = webResource.path(SERVICES_PATH).path(serviceID.toString()).path(PUBLISHED_SERVICE_FORM).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		ClientUtils.checkResponse(publishedServicePayloadResponse, Status.OK.getStatusCode());

		// retrieve serviceRequestPayload
		String publishedServicePayloadStr = publishedServicePayloadResponse.getEntity(String.class);
		JSONObject publishedServicePayloadJSON = new JSONObject(publishedServicePayloadStr);
		return publishedServicePayloadJSON.toString(ClientUtils.JSON_NICEFORMAT_SPACING);
	}

	/**
	 * Request a new Service Request from vCommander passing in the payload.
	 * The payload must contain the "service_id" element.
	 * 
	 * @param payload				The service request payload
	 * 
	 * @return serviceRequestID
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public Long requestService(String payload) throws JSONException, VCommanderException {
		if (StringUtils.isBlank(payload)) {
			throw new VCommanderException("No payload provided.");
		}

		// making new request
		JSONObject newRequestJSON = new JSONObject(payload);
		if (newRequestJSON.isNull(PUBLISHED_SERVICE_FORM_SERVICE_ID)) {
			throw new VCommanderException("Payload is missing the '" + PUBLISHED_SERVICE_FORM_SERVICE_ID + "' element");
		}

		ClientResponse newRequestResponse = webResource.path(SERVICE_REQUESTS_PATH).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, newRequestJSON.toString());
		ClientUtils.checkResponse(newRequestResponse, Status.CREATED.getStatusCode());

		String requestStr = newRequestResponse.getEntity(String.class);
		JSONObject requestJSON = new JSONObject(requestStr);
		JSONObject summary = requestJSON.getJSONObject(REQUESTED_SERVICE_SUMMARY);

		return summary.getLong(REQUESTED_SERVICE_ID);
	}

	/**
	 * Waits until the service request is successfully deployed.
	 * If an error occurs or if the request is rejected, an exception will be thrown.
	 * If the request will not finish in the given timeout, an exception will be thrown.
	 * When logger is not null, lookup messages will be printed.
	 * 
	 * @param requestId		The service request id
	 * @param timeout		Timeout in minutes
	 * @param polling		Polling interval in seconds
	 * @param logger		A PrintStream, used for logging
	 * 
	 * @return Information about the deployed components 
	 * 
	 * @throws JSONException		If there is a syntax error
	 * @throws InterruptedException	If the wait is interrupted
	 * @throws VCommanderException 	If something goes wrong
	 */
	public List<DeployedComponentInfo> waitForServiceRequestToBeCompleted(Long requestId, long timeout, long polling, PrintStream logger) throws JSONException, InterruptedException, VCommanderException {
		if (requestId == null) {
			throw new VCommanderException("No requestId provided.");
		}

		JSONObject requestJSON;
		String requestState;
		long startTime = System.currentTimeMillis();
		Set<String> loggedComments = new HashSet<>();

		do {
			requestJSON = checkServiceRequestIfCompletedAndWait(requestId, loggedComments, startTime, timeout, polling, logger);
			requestState = getRequestState(requestJSON);
		} while (!REQUESTED_SERVICE_STATE_COMPLETED.equals(requestState));

		List<DeployedComponentInfo> deployedComponents = new ArrayList<>();
		// requestJSON is never null at this point 
		JSONArray services = requestJSON.getJSONArray(REQUESTED_SERVICE_SERVICES);
		for (int i = 0; i < services.length(); i++) {
			JSONObject service = services.getJSONObject(i);
			JSONArray components = service.getJSONArray(REQUESTED_SERVICE_SERVICE_COMPONENTS);
			for (int j = 0; j < components.length(); j++) {
				JSONObject component = (JSONObject) components.get(j);
				JSONObject componentSummary = component.getJSONObject(REQUESTED_SERVICE_SERVICE_COMPONENT_SUMMARY);
				
				// custom components will not have deployed objects
				if(!componentSummary.isNull(REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT)) {
					JSONObject deployedComponent = componentSummary.getJSONObject(REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT);
					deployedComponents.add(new DeployedComponentInfo(i+1, j+1, 
											deployedComponent.getString(REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT_TYPE),
											deployedComponent.getString(REQUESTED_SERVICE_SERVICE_COMPONENT_DEPLOYED_OBJECT_NAME)));
				}
			}
		}

		return deployedComponents;
	}

	private JSONObject checkServiceRequestIfCompletedAndWait(Long requestId, Set<String> loggedComments, long startTimeInMillis, long timeoutInMinutes, long pollingInSeconds, PrintStream logger) throws VCommanderException, JSONException, InterruptedException {
		ClientUtils.log(logger, "Looking up service request with ID: " + requestId);

		JSONObject requestJSON = getRequest(requestId, logger);
		String requestState = getRequestState(requestJSON);

		if (logger != null) {
			// fetch the requests comments, and add it to the log if they were not added yet
			List<Comment> comments = getRequestComments(requestId, logger);
			for (Comment requestComment : comments) {
				String commentStr = requestComment.toString();
				if (!loggedComments.contains(commentStr)) {
					logger.println();
					logger.println("Request Comment: ");
					logger.println(commentStr);
					logger.println();

					loggedComments.add(commentStr);
				}
			}
		}

		switch (requestState) {
		case REQUESTED_SERVICE_STATE_COMPLETED:
			break;

		case REQUESTED_SERVICE_STATE_FAILED:
			throw new VCommanderException("Service request failed");

		case REQUESTED_SERVICE_STATE_REJECTED:
			throw new VCommanderException("Service request rejected");

		default:
			long currentTimeInMillis = System.currentTimeMillis();
			if (startTimeInMillis + timeoutInMinutes * DateUtils.MILLIS_PER_MINUTE <= currentTimeInMillis) {
				throw new VCommanderException("Service request did not completed in the given timeout: " + timeoutInMinutes + " minutes");
			} else {
				long toWait = Math.min(pollingInSeconds * DateUtils.MILLIS_PER_SECOND, startTimeInMillis + timeoutInMinutes * DateUtils.MILLIS_PER_MINUTE - currentTimeInMillis);
				ClientUtils.log(logger, "Service request state is: " + requestState + ". Wait " + toWait / DateUtils.MILLIS_PER_SECOND + " seconds ...");
				Thread.sleep(toWait);
			}
			break;
		}
		
		return requestJSON;
	}
	
	private JSONObject getRequest(Long requestId, PrintStream logger) {
		try {
			ClientResponse requestResponse = webResource.path(SERVICE_REQUESTS_PATH).path(requestId.toString()).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			ClientUtils.checkResponse(requestResponse, Status.OK.getStatusCode());
	
			String requestStr = requestResponse.getEntity(String.class);
			return new JSONObject(requestStr);
		} catch (Exception e) {
			ClientUtils.log(logger, "\tError while looking up request with ID: " + requestId + " Message: " + e.getMessage());
			return null;
		}
	}
	
	private String getRequestState(JSONObject request) throws JSONException {
		if(request != null) {
			JSONObject summary = request.getJSONObject(REQUESTED_SERVICE_SUMMARY);
			return summary.getString(REQUESTED_SERVICE_STATE);
		}
		return StringUtils.EMPTY;
	}
	
	private List<Comment> getRequestComments(Long requestId, PrintStream logger) {
		try {
			ClientResponse commentsResponse = webResource.path(SERVICE_REQUESTS_PATH).path(requestId.toString()).path(SERVICE_REQUEST_COMMENTS_PATH).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			return ClientUtils.getComments(commentsResponse);
		} catch (Exception e) {
			ClientUtils.log(logger, "\tError while looking up workflow comments for request with ID: " + requestId + " Message: " + e.getMessage());
			return Collections.emptyList();
		}
	}

}
