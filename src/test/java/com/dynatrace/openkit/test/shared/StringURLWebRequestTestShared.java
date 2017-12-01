/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;

public class StringURLWebRequestTestShared {

	public static String test(OpenKit openKit, String ipAddress) {
		Session session = openKit.createSession(ipAddress);
		Action action = session.enterAction("StringUrlWebRequestAction");

		WebRequestTracer webRequestTiming = action.traceWebRequest("http://www.google.com/search.html?q=test&p=10");
		webRequestTiming.start();

		String tag = webRequestTiming.getTag();
		// at this point the user should use the tag to set on the corresponding HTTP header

		webRequestTiming.stop();

		action.leaveAction();
		session.end();

		openKit.shutdown();

		return tag;
	}

}
