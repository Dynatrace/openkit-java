/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.CascadedActionTestShared;
import org.junit.Test;

import java.util.ArrayList;

public class CascadedActionTest extends AbstractLocalAppMonTest {

    @Test
    public void test() {
        CascadedActionTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION  + "&pt=1&vi=" + testConfiguration
            .getVisitorID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1005000&ts=1004000&tx=1012000&et=11&na=StringValue&it=1&pa=2&s0=3&t0=4000&vl=all+cascaded%21&et=19&it=1&pa=0&s0=6&t0=7000&et=1&na=CascadedAction-2&it=1&ca=2&pa=1&s0=2&t0=3000&s1=4&t1=2000&et=1&na=CascadedAction-1&it=1&ca=1&pa=0&s0=1&t0=2000&s1=5&t1=4000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
