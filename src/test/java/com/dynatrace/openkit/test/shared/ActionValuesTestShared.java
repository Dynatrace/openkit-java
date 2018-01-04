/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class ActionValuesTestShared {

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        Action action = session.enterAction("ActionValues");

        action.reportValue("DoubleValue", 3.141592654);
        action.reportValue("IntValue", 42);
        action.reportValue("StringValue", "nice value!");

        action.leaveAction();
        session.end();

        openKit.shutdown();
    }

}
