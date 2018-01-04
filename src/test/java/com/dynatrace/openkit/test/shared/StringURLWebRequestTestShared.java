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
