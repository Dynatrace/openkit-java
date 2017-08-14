/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.remote;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ErrorAndCrashTestShared;

public class ErrorAndCrashTest extends AbstractRemoteAppMonTest {

	@Test
	public void test() {
		ErrorAndCrashTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		validateDefaultRequests(sentRequests, null);
	}

}
