/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.remote;

import java.util.ArrayList;

import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ParallelActionTestShared;

public class ParallelActionTest extends AbstractRemoteAppMonTest {

	@Test
	public void test() {
		ParallelActionTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		validateDefaultRequests(sentRequests, null);
	}

}
