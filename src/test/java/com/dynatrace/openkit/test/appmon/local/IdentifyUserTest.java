package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.test.TestHTTPClient;
import org.junit.Test;

import java.util.ArrayList;

public class IdentifyUserTest extends AbstractLocalAppMonTest {
    @Test
    public void test() {
        String userTag = "myTestUser";

        Session session = openKit.createSession(TEST_IP);
        session.identifyUser(userTag);
        session.end();

        openKit.shutdown();

        ArrayList<TestHTTPClient.Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&pt=1&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&tv=1005000&ts=1004000&tx=1008000&et=60&na=" + userTag +"&it=1&pa=0&s0=1&t0=2000&et=19&it=1&pa=0&s0=2&t0=3000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }
}
