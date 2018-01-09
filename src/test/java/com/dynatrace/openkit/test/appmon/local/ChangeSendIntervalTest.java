/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.ChangeSendIntervalTestShared;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ChangeSendIntervalTest extends AbstractLocalAppMonTest {

    public void setUp() throws InterruptedException {
        ChangeSendIntervalTestShared.setUp(testConfiguration);
        super.setUp();
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
        String expectedBeacon1 = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&tt=okjava&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1004000&ts=1004000&tx=1008000&et=1&na=Action-1&it=1&ca=1&pa=0&s0=1&t0=1000&s1=2&t1=1000";
        validateRequest(sentRequests.get(2), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon1);
        String expectedBeacon2 = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&tt=okjava&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1004000&ts=1004000&tx=1013000&et=19&it=1&pa=0&s0=5&t0=8000&et=1&na=Action-2&it=1&ca=2&pa=0&s0=3&t0=6000&s1=4&t1=1000";
        validateRequest(sentRequests.get(3), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon2);
    }
}
