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

public class ParallelActionTestShared {

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        RootAction rootAction = session.enterAction("RootAction");

        Action parallelAction1 = rootAction.enterAction("ParallelAction-1");
        Action parallelAction2 = rootAction.enterAction("ParallelAction-2");
        Action parallelAction3 = rootAction.enterAction("ParallelAction-3");

        parallelAction1.leaveAction();
        parallelAction2.leaveAction();
        parallelAction3.leaveAction();

        rootAction.leaveAction();

        session.end();

        openKit.shutdown();
    }

}
