/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ChangeSendIntervalTestShared;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ChangeSendIntervalTest extends AbstractLocalAppMonTest {

    public void setup() throws InterruptedException {
        ChangeSendIntervalTestShared.setup(testConfiguration);
        super.setup();
    }

    @Test
    public void test() {
        ChangeSendIntervalTestShared.test(openKit, TEST_IP);

        // NOTE: This test failed for me sometimes - due to timing
        // if it fails and the only difference is different tx param, don't worry, that's fine.

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        Assert.assertEquals(4, sentRequests.size());
        validateRequest(sentRequests.get(0), RequestType.STATUS, "GET", getDefaultEndpoint(), null, "");
        validateRequest(sentRequests.get(1), RequestType.TIMESYNC, "GET", getDefaultTimeSyncEndpoint(), null, "");
        String expectedBeacon1 = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration
            .getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1005000&ts=1004000&tx=1009000&et=1&na=Action-1&it=1&ca=1&pa=0&s0=1&t0=2000&s1=2&t1=1000";
        validateRequest(sentRequests.get(2), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon1);
        String expectedBeacon2 = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_ID + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration
            .getVisitorID() + "&sn=1&ip=" + TEST_IP + "&tv=1005000&ts=1004000&tx=1014000&et=19&it=1&pa=0&s0=5&t0=9000&et=1&na=Action-2&it=1&ca=2&pa=0&s0=3&t0=7000&s1=4&t1=1000";
        validateRequest(sentRequests.get(3), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon2);
    }
}
