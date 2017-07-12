/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class CascadedActionTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);
		Action action1 = session.enterAction("CascadedAction-1");
		Action action2 = action1.enterAction("CascadedAction-2");
		Action action3 = action2.enterAction("CascadedAction-3");

		action3.reportValue("StringValue", "all cascaded!");

		action3.leaveAction();
		action2.leaveAction();
		action1.leaveAction();
		session.end();

		openKit.shutdown();
	}

}
