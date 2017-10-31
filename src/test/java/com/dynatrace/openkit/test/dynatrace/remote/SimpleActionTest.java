/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.dynatrace.remote;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.SimpleActionTestShared;

@Ignore("Integration tests are ignored")
public class SimpleActionTest extends AbstractRemoteDynatraceTest {

	@Test
	public void test() {
		SimpleActionTestShared.test(openKit, TEST_IP);

		ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
		validateDefaultRequests(sentRequests, null);
	}

}
