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
import com.dynatrace.openkit.test.shared.ErrorAndCrashTestShared;
import org.junit.Test;

import java.util.ArrayList;

public class ErrorAndCrashTest extends AbstractLocalAppMonTest {

    @Test
    public void test() {
        ErrorAndCrashTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&tt=okjava&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1005000&ts=1004000&tx=1013000&et=40&na=CouldHaveBeenForeseenException&it=1&pa=1&s0=2&t0=3000&ev=42&rs=You+did+not+need+a+psychic+to+expect+this+exception%2C+right%3F&et=50&na=java.lang.ArithmeticException&it=1&pa=0&s0=5&t0=6000&rs=%2F+by+zero&st=java.lang.ArithmeticException%3A+%2F+by+zero%0A%09at+com.dynatrace.openkit.test.shared.ErrorAndCrashTestShared.test%28ErrorAndCrashTestShared.java%3A27%29%0A%09at+com.dynatrace.openkit.test.appmon.local.ErrorAndCrashTest.test%28ErrorAndCrashTest.java%3A19%29%0A%09...&et=19&it=1&pa=0&s0=7&t0=8000&et=1&na=ErrorAction&it=1&ca=1&pa=0&s0=1&t0=2000&s1=3&t1=2000&et=1&na=CrashAction&it=1&ca=2&pa=0&s0=4&t0=5000&s1=6&t1=2000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
