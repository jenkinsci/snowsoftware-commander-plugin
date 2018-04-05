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

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.embotics.vlm.rest.v30.client.model.VCommanderException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * vCommander REST API Java client - for sessions
 * 
 * @author btarczali
 *
 */
public class SessionsClient {
	private static final String TOKEN_PATH	= "tokens";
	
	private static final String PROPERTY_USERNAME = "username";
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_ORGANIZATION = "organization";
	private static final String PROPERTY_TOKEN = "token";
	
	private final String username;
	private final String password;
	private final String organization;
	
	private final WebResource webResource;


	public SessionsClient(WebResource webResource, String username, String password, String organization) {
		this.webResource = webResource;
		this.username = username;
		this.password = password;
		this.organization = organization;
	}


	/**
	 * Get a new security token
	 * 
	 * The token can be used for subsequent calls
	 * This method can be used to validate basic-auth credential
	 * 
	 * @return security token
	 * 
	 * @throws JSONException 		If there is a syntax error
	 * @throws VCommanderException 	If something goes wrong
	 */
	public String getSecurityToken() throws JSONException, VCommanderException {
		
		// making new request
		JSONObject newTokenRequest = new JSONObject();
		newTokenRequest.put(PROPERTY_USERNAME, username);
		newTokenRequest.put(PROPERTY_PASSWORD, password);
		if(StringUtils.isNotBlank(organization)) {
			newTokenRequest.put(PROPERTY_ORGANIZATION, organization);
		}
		
		ClientResponse newTokenResponse = webResource.path(TOKEN_PATH).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, newTokenRequest.toString());
		ClientUtils.checkResponse(newTokenResponse, Status.CREATED.getStatusCode());

		String tokenStr = newTokenResponse.getEntity(String.class);
		JSONObject tokenJSON = new JSONObject(tokenStr);
		return tokenJSON.getString(PROPERTY_TOKEN);
	}

}
