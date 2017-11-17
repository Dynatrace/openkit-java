/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.MissingLeaveActionTestShared;
import org.junit.Test;

import java.util.ArrayList;

public class MissingLeaveActionTest extends AbstractLocalAppMonTest {

    @Test
    public void test() {
        MissingLeaveActionTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration
            .getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1005000&ts=1004000&tx=1017000&et=19&it=1&pa=0&s0=11&t0=12000&et=1&na=MissingLeaveAction-3a&it=1&ca=3&pa=2&s0=3&t0=4000&s1=6&t1=3000&et=1&na=MissingLeaveAction-3b&it=1&ca=4&pa=2&s0=4&t0=5000&s1=7&t1=3000&et=1&na=MissingLeaveAction-3c&it=1&ca=5&pa=2&s0=5&t0=6000&s1=8&t1=3000&et=1&na=MissingLeaveAction-2&it=1&ca=2&pa=1&s0=2&t0=3000&s1=9&t1=7000&et=1&na=MissingLeaveAction-1&it=1&ca=1&pa=0&s0=1&t0=2000&s1=10&t1=9000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
