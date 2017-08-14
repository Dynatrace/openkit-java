/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.WebRequestTimingTestShared;

public class WebRequestTimingTest extends AbstractLocalAppMonTest {

	@Test
	public void test() {
		WebRequestTimingTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration.getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1002000&ts=1001000&tx=1008000&et=30&na=http%3A%2F%2Fwww.google.com%2Fapi%2Fsearch&it=1&pa=1&s0=2&t0=3000&s1=3&t1=1000&et=19&it=1&pa=0&s0=5&t0=6000&et=1&na=WebRequestTiming&it=1&ca=1&pa=0&s0=1&t0=2000&s1=4&t1=3000";
		validateDefaultRequests(sentRequests, expectedBeacon);
	}

}
