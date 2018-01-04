/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
