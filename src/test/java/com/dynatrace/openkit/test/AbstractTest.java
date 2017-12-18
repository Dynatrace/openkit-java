/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import org.junit.Assert;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

public abstract class AbstractTest {

	protected OpenKit openKit;
	protected OpenKitTestImpl openKitTestImpl;

	protected void validateRequest(Request request, RequestType requestType, String method, String url, String clientIPAddress, String expectedBeacon) {
		Assert.assertEquals(requestType, request.getRequestType());
		Assert.assertEquals(method, request.getMethod());
		Assert.assertEquals(url, request.getURL());
		Assert.assertEquals(clientIPAddress, request.getClientIPAddress());
		if (expectedBeacon != null) {
			Assert.assertEquals(expectedBeacon, request.getDecodedData());
		}
	}

}