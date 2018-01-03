/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;

public class CascadedActionTestShared {

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        RootAction action1 = session.enterAction("CascadedAction-1");
        Action action2 = action1.enterAction("CascadedAction-2");

        action2.reportValue("StringValue", "all cascaded!");

        action2.leaveAction();
        action1.leaveAction();
        session.end();

        openKit.shutdown();
    }

}
