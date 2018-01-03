/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.test.TestConfiguration;

public class ChangeSendIntervalTestShared {

    public static void setUp(TestConfiguration testConfiguration) {
        testConfiguration.setStatusResponse("type=m&si=1&bn=dynaTraceMonitor&id=1", 200);
    }

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        Action action1 = session.enterAction("Action-1");
        action1.leaveAction();

        try {
            Thread.sleep(2700);
        } catch (InterruptedException e) {
            // ignored
        }

        Action action2 = session.enterAction("Action-2");
        action2.leaveAction();
        session.end();

        openKit.shutdown();
    }

}
