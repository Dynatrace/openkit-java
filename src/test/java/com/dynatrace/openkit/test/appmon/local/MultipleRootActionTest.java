/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.MultipleRootActionTestShared;
import org.junit.Test;

import java.util.ArrayList;

public class MultipleRootActionTest extends AbstractLocalAppMonTest {

    @Test
    public void test() {
        MultipleRootActionTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration
            .getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1005000&ts=1004000&tx=1013000&et=19&it=1&pa=0&s0=7&t0=8000&et=1&na=RootAction-3&it=1&ca=3&pa=0&s0=3&t0=4000&s1=4&t1=1000&et=1&na=RootAction-2&it=1&ca=2&pa=0&s0=2&t0=3000&s1=5&t1=3000&et=1&na=RootAction-1&it=1&ca=1&pa=0&s0=1&t0=2000&s1=6&t1=5000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
