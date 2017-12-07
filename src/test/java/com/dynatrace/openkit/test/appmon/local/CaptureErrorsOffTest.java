/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.CaptureErrorsOffTestShared;
import org.junit.Test;

import java.util.ArrayList;

public class CaptureErrorsOffTest extends AbstractLocalAppMonTest {

    public void setup() throws InterruptedException {
        CaptureErrorsOffTestShared.setup(testConfiguration);
        super.setup();
    }

    @Test
    public void test() {
        CaptureErrorsOffTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&vi=" + testConfiguration
            .getVisitorID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1005000&ts=1004000&tx=1010000&et=50&na=SEGFAULT+crash&it=1&pa=0&s0=3&t0=4000&rs=sometimes+bad+stuff+happens&st=...&et=19&it=1&pa=0&s0=4&t0=5000&et=1&na=CaptureErrorsOffAction&it=1&ca=1&pa=0&s0=1&t0=2000&s1=2&t1=1000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
