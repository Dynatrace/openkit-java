/**
 * Copyright 2018-2019 Dynatrace LLC
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
import com.dynatrace.openkit.test.shared.StringURLWebRequestTestShared;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

@Ignore("Integration tests are ignored")
public class StringURLWebRequestTest extends AbstractLocalAppMonTest {

    @Test
    public void test() {
        String tag = StringURLWebRequestTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&tt=okjava&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1004000&ts=1004000&tx=1010000&et=30&na=http%3A%2F%2Fwww.google.com%2Fsearch.html&it=1&pa=1&s0=2&t0=2000&s1=3&t1=1000&et=19&it=1&pa=0&s0=5&t0=5000&et=1&na=StringUrlWebRequestAction&it=1&ca=1&pa=0&s0=1&t0=1000&s1=4&t1=3000";
        validateDefaultRequests(sentRequests, expectedBeacon);

        String expectedTag = "MT_3_1_" + testConfiguration.getDeviceID() + "_1_" + TEST_APPLICATION_NAME + "_1_1_2";
        Assert.assertEquals("Tag does not match expectedTag", expectedTag, tag);
    }

}
