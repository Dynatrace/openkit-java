/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.protocol;

/**
 * Abstract base class for a response to one of the 3 request types (status check, beacon send, time sync).
 */
public abstract class Response {

	private final int responseCode;

	// *** constructors ***

	public Response(String response, int responseCode) {
		this.responseCode = responseCode;
	}

	// *** getter methods ***

	public int getResponseCode() {
		return responseCode;
	}

}
