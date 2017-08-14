/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.StringURLWebRequestTestShared;

public class StringURLWebRequestTest extends AbstractLocalAppMonTest {

	@Test
	public void test() {
		String tag = StringURLWebRequestTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration.getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1002000&ts=1001000&tx=1008000&et=30&na=http%3A%2F%2Fwww.google.com%2Fsearch.html&it=1&pa=1&s0=2&t0=3000&s1=3&t1=1000&et=19&it=1&pa=0&s0=5&t0=6000&et=1&na=StringUrlWebRequestAction&it=1&ca=1&pa=0&s0=1&t0=2000&s1=4&t1=3000";
		validateDefaultRequests(sentRequests, expectedBeacon);

		String expectedTag = "MT_3_1_" + testConfiguration.getVisitorID() + "_1_" + TEST_APPLICATION_ID + "_1_1_2";
		Assert.assertEquals("Tag does not match expectedTag", expectedTag, tag);
	}

}
