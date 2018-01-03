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

public class CaptureOffTestShared {

    public static void setUp(TestConfiguration testConfiguration) {
        testConfiguration.setStatusResponse("type=m&cp=0", 200);
    }

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        Action action = session.enterAction("CaptureOffAction");

        action.leaveAction();
        session.end();

        openKit.shutdown();
    }

}
