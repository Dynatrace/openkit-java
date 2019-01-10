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
import com.dynatrace.openkit.test.shared.ComplexSessionTestShared;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

@Ignore("Integration tests are ignored")
public class ComplexSessionTest extends AbstractLocalAppMonTest {

    @Test
    public void test() {
        ComplexSessionTestShared.test(openKit, TEST_IP);

        ArrayList<Request> sentRequests = openKitTestImpl.getSentRequests();
        String expectedBeacon = "vv=3&va=7.0.0000&ap=" + TEST_APPLICATION_NAME + "&an=" + TEST_APPLICATION_NAME + "&vn=" + TEST_OPENKIT_DEFAULT_VERSION + "&pt=1&tt=okjava&vi=" + testConfiguration
            .getDeviceID() + "&sn=1&ip=" + TEST_IP + "&os=" + TEST_OS + "&mf=" + TEST_MANUFACTURER + "&md=" + TEST_DEVICE_TYPE + "&tv=1004000&ts=1004000&tx=1018000&et=12&na=IntegerValue&it=1&pa=1&s0=2&t0=2000&vl=45&et=13&na=DoubleValue&it=1&pa=1&s0=3&t0=3000&vl=9.2&et=11&na=String&it=1&pa=1&s0=4&t0=4000&vl=This+is+a+string&et=40&na=errorName&it=1&pa=1&s0=5&t0=5000&ev=22&rs=meaningful+reason&et=40&na=FATAL+ERROR&it=1&pa=1&s0=6&t0=6000&ev=42&rs=valid+reason&et=10&na=EventOne&it=1&pa=2&s0=8&t0=8000&et=30&na=http%3A%2F%2Fmydomain%2Fapp%2Fsearch.php&it=1&pa=1&s0=10&t0=10000&s1=11&t1=1000&et=19&it=1&pa=0&s0=13&t0=13000&et=1&na=ActionTwo&it=1&ca=2&pa=1&s0=7&t0=7000&s1=9&t1=2000&et=1&na=ActionOne&it=1&ca=1&pa=0&s0=1&t0=1000&s1=12&t1=11000";
        validateDefaultRequests(sentRequests, expectedBeacon);
    }

}
