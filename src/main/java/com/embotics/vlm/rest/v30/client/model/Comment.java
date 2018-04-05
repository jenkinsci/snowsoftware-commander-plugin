package com.embotics.vlm.rest.v30.client.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Data class for vComamnder comments used in service requests and workflows
 * 
 * @author btarczali
 */
public class Comment {
	private static final String REQUESTED_SERVICE_COMMENT_EVENT = "event";
	private static final String REQUESTED_SERVICE_COMMENT_TEXT = "text";
	private static final String REQUESTED_SERVICE_COMMENT_AUTHOR = "author";
	private static final String REQUESTED_SERVICE_COMMENT_DATE = "date";
	private static final String REQUESTED_SERVICE_COMMENT_AUTHOR_DEFAULT = "SYSTEM";
	
	private static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss z";
	
	private final String event;
	private final String text;
	private final String author;
	private final String date;

	public Comment(JSONObject comment) throws JSONException {
		this.event = getDefaultString(comment, REQUESTED_SERVICE_COMMENT_EVENT, null);
		this.text = getDefaultString(comment, REQUESTED_SERVICE_COMMENT_TEXT, null);
		this.author = getDefaultString(comment, REQUESTED_SERVICE_COMMENT_AUTHOR, REQUESTED_SERVICE_COMMENT_AUTHOR_DEFAULT);
		this.date = formatDate(getDefaultString(comment, REQUESTED_SERVICE_COMMENT_DATE, null));
	}
	
	private String getDefaultString(JSONObject obj, String key, String defaultValue) throws JSONException {
		return obj.isNull(key) ? defaultValue : obj.getString(key);
	}

	private String formatDate(String dateStr) {
		if (dateStr != null) {
			// try to convert to a friendly format
			// vCommander sent us the dates in UTC
			// we do not want to convert to different timezone, just format it to a friendlier format
			// input example:	2017-12-06T18:55:09Z
			// output example: 	2017/12/06 18:55:09 UTC
			try {
				SimpleDateFormat inputFormat = new SimpleDateFormat(UTC_DATE_FORMAT);
				inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date convertedDate = inputFormat.parse(dateStr);

				DateFormat outputFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
				outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				return outputFormat.format(convertedDate);
			} catch (ParseException e) {
				// ignore
				System.err.println(e);
			}
		}
		return dateStr;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(author);
		builder.append(" at ");
		builder.append(date);

		if (StringUtils.isNotBlank(event)) {
			builder.append(" - ");
			builder.append(event);
		}

		if (StringUtils.isNotBlank(text)) {
			builder.append(" - ");
			builder.append(text);
		}

		return builder.toString();
	}
}
