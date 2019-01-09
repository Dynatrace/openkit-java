/**
 * Copyright 2018-2019 Dynatrace LLC
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
            String stacktrace = "java.lang.ArithmeticException: / by zero\n\tat com.dynatrace.openkit.test.shared.ErrorAndCrashTestShared.test(ErrorAndCrashTestShared.java:27)\n\tat com.dynatrace.openkit.test.appmon.local.ErrorAndCrashTest.test(ErrorAndCrashTest.java:19)\n\t...";
            session.reportCrash(e.getClass().getName(), e.getMessage(), stacktrace);
        }

        session.end();

        openKit.shutdown();
    }

}
