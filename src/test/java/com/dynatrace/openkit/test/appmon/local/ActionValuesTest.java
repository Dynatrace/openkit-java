/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ActionValuesTestShared;

public class ActionValuesTest extends AbstractLocalAppMonTest {

	@Test
	public void test() {
		ActionValuesTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration.getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1002000&ts=1001000&tx=1009000&et=13&na=DoubleValue&it=1&pa=1&s0=2&t0=3000&vl=3.141592654&et=12&na=IntValue&it=1&pa=1&s0=3&t0=4000&vl=42&et=11&na=StringValue&it=1&pa=1&s0=4&t0=5000&vl=nice+value%21&et=19&it=1&pa=0&s0=6&t0=7000&et=1&na=ActionValues&it=1&ca=1&pa=0&s0=1&t0=2000&s1=5&t1=4000";
		validateDefaultRequests(sentRequests, expectedBeacon);
	}

}
