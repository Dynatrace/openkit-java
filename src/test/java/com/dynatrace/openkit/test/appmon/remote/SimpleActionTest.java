/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.remote;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.SimpleActionTestShared;

public class SimpleActionTest extends AbstractRemoteAppMonTest {

	@Test
	public void test() {
		SimpleActionTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		validateDefaultRequests(sentRequests, null);
	}

}
