/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

public class TestConfiguration {

	private long visitorID = -1;
	private String statusResponse = null;
	private int statusResponseCode = -1;
	private String timeSyncResponse = null;
	private int timeSyncResponseCode = -1;

	public void setVisitorID(long visitorID) {
		this.visitorID = visitorID;
	}

	public void setStatusResponse(String response, int responseCode) {
		statusResponse = response;
		statusResponseCode = responseCode;
	}

	public void setTimeSyncResponse(String response, int responseCode) {
		timeSyncResponse = response;
		timeSyncResponseCode = responseCode;
	}

	public long getVisitorID() {
		return visitorID;
	}

	public String getStatusResponse() {
		return statusResponse;
	}

	public int getStatusResponseCode() {
		return statusResponseCode;
	}

	public String getTimeSyncResponse() {
		return timeSyncResponse;
	}

	public int getTimeSyncResponseCode() {
		return timeSyncResponseCode;
	}

}
