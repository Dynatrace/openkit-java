/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class ErrorAndCrashTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);

		Action errorAction = session.enterAction("ErrorAction");
		errorAction.reportError("CouldHaveBeenForeseenException", 42, "You did not need a psychic to expect this exception, right?");
		errorAction.leaveAction();

		try {
			Action crashException = session.enterAction("CrashAction");

			int notEvenChuckNorrisCanDoThis = 42 / 0;
			System.out.println(notEvenChuckNorrisCanDoThis);

			crashException.leaveAction();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			session.reportCrash(e.getClass().getName(), e.getMessage(), sw.toString());
		}

		session.end();

		openKit.shutdown();
	}

}
