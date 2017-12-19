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
	public static final String TEST_ENDPOINT = "http://localhost/dynatraceMonitor";
	public static final String TEST_IP = "123.45.67.89";
	public static final String TEST_OPENKIT_DEFAULT_VERSION = "0.3";
	public static final String TEST_DEVICE_TYPE = "OpenKitDevice";
	public static final String TEST_MANUFACTURER = "Dynatrace";
	public static final String TEST_OS = "OpenKit+0.3";

	protected String getDefaultEndpoint() {
		return TEST_ENDPOINT + "?type=m&srvid=1&app=" + TEST_APPLICATION_NAME + "&va=7.0.0000&pt=1";
	}

	protected String getDefaultTimeSyncEndpoint() {
		return TEST_ENDPOINT + "?type=mts";
	}

	protected void validateDefaultRequests(ArrayList<Request> sentRequests, String expectedBeacon) {
		Assert.assertEquals(3, sentRequests.size());

		validateRequest(sentRequests.get(0), RequestType.STATUS, "GET", getDefaultEndpoint(), null, "");
		validateRequest(sentRequests.get(1), RequestType.TIMESYNC, "GET", getDefaultTimeSyncEndpoint(), null, "");
		validateRequest(sentRequests.get(2), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon);
	}

}