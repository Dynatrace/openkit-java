/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.CascadedActionTestShared;

public class CascadedActionTest extends AbstractLocalAppMonTest {

	@Test
	public void test() {
		CascadedActionTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration.getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1006000&ts=1004000&tx=1015000&et=11&na=StringValue&it=1&pa=3&s0=4&t0=6000&vl=all+cascaded%21&et=19&it=1&pa=0&s0=8&t0=10000&et=1&na=CascadedAction-3&it=1&ca=3&pa=2&s0=3&t0=5000&s1=5&t1=2000&et=1&na=CascadedAction-2&it=1&ca=2&pa=1&s0=2&t0=4000&s1=6&t1=4000&et=1&na=CascadedAction-1&it=1&ca=1&pa=0&s0=1&t0=3000&s1=7&t1=6000";
		validateDefaultRequests(sentRequests, expectedBeacon);
	}

}
