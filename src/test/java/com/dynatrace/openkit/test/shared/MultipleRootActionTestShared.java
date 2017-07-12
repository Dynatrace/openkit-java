/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class MultipleRootActionTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);
		Action action1 = session.enterAction("RootAction-1");
		Action action2 = session.enterAction("RootAction-2");
		Action action3 = session.enterAction("RootAction-3");

		action3.leaveAction();
		action2.leaveAction();
		action1.leaveAction();

		session.end();

		openKit.shutdown();
	}

}
