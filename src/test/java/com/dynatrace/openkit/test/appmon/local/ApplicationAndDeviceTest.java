/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ApplicationAndDeviceTestShared;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ApplicationAndDeviceTest extends AbstractLocalAppMonTest {

    @Before
    public void setUp() throws InterruptedException {
        // set values
        testConfiguration.setApplicationVersion("2017.42.3141");
        testConfiguration.setDevice(new Device("Windows 10", "Dynatrace", "OpenKitTester"));
        super.setUp();
    }

    @Test
    public void test() throws InterruptedException {
        ApplicationAndDeviceTestShared.test(openKitTestImpl, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=2017.42.3141&pt=1&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=Windows+10&mf=Dynatrace&md=OpenKitTester&tv=1005000&ts=1004000&tx=1009000&et=19&it=1&pa=0&s0=3&t0=4000&et=1&na=ApplicationAndDeviceTestAction&it=1&ca=1&pa=0&s0=1&t0=2000&s1=2&t1=1000";

        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
