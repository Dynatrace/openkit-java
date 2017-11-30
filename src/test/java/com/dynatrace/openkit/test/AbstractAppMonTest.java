/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import java.util.ArrayList;

import org.junit.Assert;

import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

public abstract class AbstractAppMonTest extends AbstractTest {

	public static final String TEST_APPLICATION_NAME = "TestApplicationName";
	public static final String TEST_APPLICATION_ID = "TestApplicationID";
	public static final String TEST_ENDPOINT = "http://localhost/";
	public static final String TEST_IP = "123.45.67.89";

	protected String getDefaultEndpoint() {
		return TEST_ENDPOINT + "dynaTraceMonitor?type=m&srvid=1&app=" + TEST_APPLICATION_NAME + "&va=7.0.0000&pt=1";
	}

	protected String getDefaultTimeSyncEndpoint() {
		return TEST_ENDPOINT + "dynaTraceMonitor?type=mts";
	}

	protected void validateDefaultRequests(ArrayList<Request> sentRequests, String expectedBeacon) {
		Assert.assertEquals(3, sentRequests.size());

		validateRequest(sentRequests.get(0), RequestType.STATUS, "GET", getDefaultEndpoint(), null, "");
		validateRequest(sentRequests.get(1), RequestType.TIMESYNC, "GET", getDefaultTimeSyncEndpoint(), null, "");
		validateRequest(sentRequests.get(2), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon);
	}

}