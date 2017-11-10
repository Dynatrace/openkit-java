/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.CaptureCrashesOffTestShared;

public class CaptureCrashesOffTest extends AbstractLocalAppMonTest {

	public void setup() throws InterruptedException {
		CaptureCrashesOffTestShared.setup(testConfiguration);
		super.setup();
	}

	@Test
	public void test() {
		CaptureCrashesOffTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration.getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1006000&ts=1004000&tx=1011000&et=40&na=bad+error%21&it=1&pa=1&s0=2&t0=4000&ev=666&rs=this+really+should+never+ever+happen%21&et=19&it=1&pa=0&s0=4&t0=6000&et=1&na=CaptureCrashesOffAction&it=1&ca=1&pa=0&s0=1&t0=3000&s1=3&t1=2000";
		validateDefaultRequests(sentRequests, expectedBeacon);
	}

}
