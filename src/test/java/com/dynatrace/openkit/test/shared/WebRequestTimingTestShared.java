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
import org.junit.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WebRequestTimingTestShared {

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        Action action = session.enterAction("WebRequestTiming");

        URLConnection connection;
        try {
            connection = new URL("http://www.google.com/api/search?q=test").openConnection();
        } catch (MalformedURLException e) {
            Assert.fail(e.toString());
            return;
        } catch (IOException e) {
            Assert.fail(e.toString());
            return;
        }
        WebRequestTracer webRequestTiming = action.traceWebRequest(connection);
        webRequestTiming.start();

        // we could actually execute the request, but as it's not needed for this test, we don't

        webRequestTiming.stop();

        action.leaveAction();
        session.end();

        openKit.shutdown();
    }

}
