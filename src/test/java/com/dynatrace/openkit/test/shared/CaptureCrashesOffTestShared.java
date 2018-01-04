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
import com.dynatrace.openkit.test.TestConfiguration;

public class CaptureCrashesOffTestShared {

    public static void setUp(TestConfiguration testConfiguration) {
        testConfiguration.setStatusResponse("type=m&cr=0", 200);
    }

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);

        Action action = session.enterAction("CaptureCrashesOffAction");
        action.reportError("bad error!", 666, "this really should never ever happen!");
        action.leaveAction();

        session.reportCrash("SEGFAULT crash", "sometimes bad stuff happens", "...");

        session.end();

        openKit.shutdown();
    }

}
