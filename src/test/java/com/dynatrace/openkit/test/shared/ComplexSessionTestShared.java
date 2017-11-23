/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Paul Johnson
 */
package com.dynatrace.openkit.test.shared;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dynatrace.openkit.api.*;

public class ComplexSessionTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);

		RootAction actionOne = session.enterAction("ActionOne");

		actionOne.reportValue("IntegerValue", 45);
		actionOne.reportValue("DoubleValue", 9.2);
		actionOne.reportValue("String", "This is a string");

		actionOne.reportError("errorName", 22, "meaningful reason");
		actionOne.reportError("FATAL ERROR", 42, "valid reason");

		// create a child Action
		Action actionTwo = actionOne.enterAction("ActionTwo");
		actionTwo.reportEvent("EventOne");
		actionTwo.leaveAction();

		// simulate the tagged web request - we dont actually need to send it
		URL url;
		WebRequestTag timing;
		try {
			url = new URL("http://mydomain/app/search.php");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			timing = actionOne.tagWebRequest(conn);			// tags the request
			timing.startTiming();							// starts the timing
			// no request is performed - but that's OK
			timing.stopTiming();							// stop the timing and generate the beacon signal
		} catch (IOException e) {
			System.out.println(e.toString());
		}

		actionOne.leaveAction();

		session.end();

		openKit.shutdown();
	}

}
