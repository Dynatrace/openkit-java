/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.providers;

import java.util.ArrayList;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.test.TestHTTPClient;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

public class TestHTTPClientProvider implements HTTPClientProvider {

	private boolean remoteTest;
	private TestHTTPClient testHTTPClient;

	private String statusResponse = null;
	private int statusResponseCode = -1;
	private String timeSyncResponse = null;
	private int timeSyncResponseCode = -1;

	ArrayList<Request> previouslySentRequests = new ArrayList<Request>();

	public TestHTTPClientProvider(boolean remoteTest) {
		this.remoteTest = remoteTest;
	}

	@Override
	public HTTPClient createClient(Logger logger, HTTPClientConfiguration configuration) {
		if (testHTTPClient != null) {
			previouslySentRequests.addAll(testHTTPClient.getSentRequests());
		}

		testHTTPClient = new TestHTTPClient(configuration.getBaseURL(), configuration.getApplicationID(),
				configuration.getServerID(), remoteTest);
		if (statusResponse != null) {
			testHTTPClient.setStatusResponse(statusResponse, statusResponseCode);
		}
		if (timeSyncResponse != null) {
			testHTTPClient.setTimeSyncResponse(timeSyncResponse, timeSyncResponseCode);
		}
		return testHTTPClient;
	}

	public ArrayList<Request> getSentRequests() {
		ArrayList<Request> sentRequests = new ArrayList<Request>();
		sentRequests.addAll(previouslySentRequests);
		if (testHTTPClient != null) {
			sentRequests.addAll(testHTTPClient.getSentRequests());
		}
		return sentRequests;
	}

	public void setStatusResponse(String response, int responseCode) {
		statusResponse = response;
		statusResponseCode = responseCode;
	}

	public void setTimeSyncResponse(String response, int responseCode) {
		timeSyncResponse = response;
		timeSyncResponseCode = responseCode;
	}

}
