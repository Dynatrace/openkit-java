/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class SimpleActionTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);
		Action action = session.enterAction("SimpleAction");
		try {
		    Thread.sleep(2000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		action.leaveAction();
		session.end();

		openKit.shutdown();
	}

}
