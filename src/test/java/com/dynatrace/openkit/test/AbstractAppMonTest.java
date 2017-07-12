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
		return TEST_ENDPOINT + "dynaTraceMonitor?type=m&srvid=1&app=" + TEST_APPLICATION_ID + "&va=7.0.0000";
	}

	protected void validateDefaultRequests(ArrayList<Request> sentRequests, String expectedBeacon) {
		Assert.assertEquals(2, sentRequests.size());

		Assert.assertEquals(RequestType.STATUS, sentRequests.get(0).getRequestType());
		Assert.assertEquals("GET", sentRequests.get(0).getMethod());
		Assert.assertEquals(getDefaultEndpoint(), sentRequests.get(0).getUrl());
		Assert.assertEquals(null, sentRequests.get(0).getClientIPAddress());
		Assert.assertEquals("", sentRequests.get(0).getDecodedData());

		Assert.assertEquals(RequestType.BEACON, sentRequests.get(1).getRequestType());
		Assert.assertEquals("POST", sentRequests.get(1).getMethod());
		Assert.assertEquals(getDefaultEndpoint(), sentRequests.get(1).getUrl());
		Assert.assertEquals(TEST_IP, sentRequests.get(1).getClientIPAddress());
		if (expectedBeacon != null) {
			Assert.assertEquals(expectedBeacon, sentRequests.get(1).getDecodedData());
		}
	}

}