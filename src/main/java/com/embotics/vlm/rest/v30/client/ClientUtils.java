package com.embotics.vlm.rest.v30.client;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.embotics.vlm.rest.v30.client.model.Comment;
import com.embotics.vlm.rest.v30.client.model.VCommanderException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Utility class with helper methods for the REST API clients
 * @author btarczali
 *
 */
public class ClientUtils {
	
	static final int 	JSON_NICEFORMAT_SPACING = 2;
	static final String COMMON_ITEMS = "items";
	
	public static final String REST_FILTER = "filter";			//the single query string parameter used to filter RESTv3 collections
	public enum RestFilterOperator {eq, contains, ge, and};		//the operators supported in the RESTv3 filter grammar
	
	static void checkResponse(ClientResponse response, int expectedStatusCode) throws VCommanderException {
		if (response.getStatus() != expectedStatusCode) {
			if(response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
				throw new VCommanderException("Access is denied due to invalid credentials.");	
			} else {
				StringBuilder errorMessage = new StringBuilder();
				errorMessage.append("status_code=").append(response.getStatus()).append(", error_message=").append(response.getEntity(String.class));
				throw new VCommanderException(errorMessage.toString());
			}
		}
	}
	
	static void log(PrintStream logger, String message) {
		if (logger != null) {
			logger.println(message);
		}
	}
	
	static List<Comment> getComments(ClientResponse commentsResponse) throws JSONException, VCommanderException {
		checkResponse(commentsResponse, Status.OK.getStatusCode());
		
		String commentsStr = commentsResponse.getEntity(String.class);
		JSONObject commentsJSON = new JSONObject(commentsStr);
		JSONArray comments = commentsJSON.getJSONArray(COMMON_ITEMS);

		List<Comment> commentsList = new ArrayList<>();
		// iterate backwards, so the comments to be logged the right order
		for (int i = comments.length() - 1; i >= 0; i--) {
			JSONObject comment = (JSONObject) comments.get(i);
			commentsList.add(new Comment(comment));
		}

		return commentsList;
	}
	
	/**
	 * Filter helper that encapsulates in quotes
	 *  
	 * Any user input that may contain spaces need to be encapsulated in quotes.     
	 * 
	 * @param in input 
	 * @return encapsulated string
	 */
	public static String encapsulateInQuots(String in) {
		StringBuilder string = new StringBuilder();
		return string.append("'").append(in.replace("'", "\'")).append("'").toString();
	}
	
	/**
	 * Helper that builds a filter for RESTv3 collections
	 * 
	 * Filters follow a pattern like:
	 * 
	 * GET /rest/v3/workflows?filter=name -eq myWorkflow
	 * GET /rest/v3/workflows?filter=(name -eq myWorkflow) -and (type -eq COMMAND)
	 * 
	 * About the filter grammar:
	 * - operators need to be prefixed with -
	 * - operator and operands within a filter are delimited by " "
	 * - multiple filters are delimited by ()
	 * 
	 * @param left
	 * @param operator
	 * @param right
	 * @return
	 */
	public static String buildFilter(String left, RestFilterOperator operator, String right) {
		StringBuilder filter = new StringBuilder();
		filter.append("(").append(left).append(" ").append("-").append(operator).append(" ").append(right).append(")");
		return filter.toString();
	}
	
}
