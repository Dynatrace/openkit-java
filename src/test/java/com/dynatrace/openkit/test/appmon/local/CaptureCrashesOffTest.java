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

import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.shared.CaptureCrashesOffTestShared;
import org.junit.Test;

import java.util.ArrayList;

public class CaptureCrashesOffTest extends AbstractLocalAppMonTest {

    public void setUp() throws InterruptedException {
        CaptureCrashesOffTestShared.setUp(testConfiguration);
        super.setUp();
    }

    @Test
    public void test() {
        CaptureCrashesOffTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1005000&ts=1004000&tx=1010000&et=40&na=bad+error%21&it=1&pa=1&s0=2&t0=3000&ev=666&rs=this+really+should+never+ever+happen%21&et=19&it=1&pa=0&s0=4&t0=5000&et=1&na=CaptureCrashesOffAction&it=1&ca=1&pa=0&s0=1&t0=2000&s1=3&t1=2000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
