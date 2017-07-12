/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.protocol;

import java.util.StringTokenizer;

/**
 * Implements a time sync response which is sent for time sync requests.
 */
public class TimeSyncResponse extends Response {

	// time sync response constants
	private static final String RESPONSE_KEY_REQUEST_RECEIVE_TIME = "t1";
	private static final String RESPONSE_KEY_RESPONSE_SEND_TIME = "t2";

	// timestamps contained in time sync response
	private long requestReceiveTime = -1;
	private long responseSendTime = -1;

	// *** constructors ***

	public TimeSyncResponse(String response, int responseCode) {
		super(response, responseCode);
		parseResponse(response);
	}

	// *** private methods ***

	// parses time sync response
	private void parseResponse(String response) {
		StringTokenizer tokenizer = new StringTokenizer(response, "&=");
		while(tokenizer.hasMoreTokens()) {
			String key = tokenizer.nextToken();
			String value = tokenizer.nextToken();

			if (RESPONSE_KEY_REQUEST_RECEIVE_TIME.equals(key)) {
				requestReceiveTime = Long.parseLong(value);
			} else if (RESPONSE_KEY_RESPONSE_SEND_TIME.equals(key)) {
				responseSendTime = Long.parseLong(value);
			}
		}
	}

	// *** getter methods ***

	public long getRequestReceiveTime() {
		return requestReceiveTime;
	}

	public long getResponseSendTime() {
		return responseSendTime;
	}

}
