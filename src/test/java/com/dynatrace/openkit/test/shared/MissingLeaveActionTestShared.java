/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class MissingLeaveActionTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);
		Action action1 = session.enterAction("MissingLeaveAction-1");
		Action action2 = action1.enterAction("MissingLeaveAction-2");
		/*Action action3 = */action2.enterAction("MissingLeaveAction-3a");
		/*Action action4 = */action2.enterAction("MissingLeaveAction-3b");
		/*Action action5 = */action2.enterAction("MissingLeaveAction-3c");

//		intentionally, those Actions are not left
//		action5.leaveAction();
//		action4.leaveAction();
//		action3.leaveAction();
//		action2.leaveAction();
//		action1.leaveAction();

		session.end();

		openKit.shutdown();
	}

}
