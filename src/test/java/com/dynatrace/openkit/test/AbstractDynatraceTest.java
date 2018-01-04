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

package com.dynatrace.openkit.test;

import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import org.junit.Assert;

import java.util.ArrayList;

public abstract class AbstractDynatraceTest extends AbstractTest {

    public static final String TEST_APPLICATION_NAME = "TestApp";
    public static final String TEST_APPLICATION_ID = "<provide application ID here>";
    public static final String TEST_ENDPOINT = "<provide endpoint here>/";
    public static final String TEST_IP = "123.45.67.89";

    protected String getDefaultEndpoint() {
        return TEST_ENDPOINT + "mbeacon?type=m&srvid=-1&app=" + TEST_APPLICATION_ID + "&va=7.0.0000";
    }

    protected String getDefaultEndpointStart() {
        return TEST_ENDPOINT + "mbeacon?type=m&srvid=";
    }

    protected String getDefaultEndpointEnd() {
        return "&app=" + TEST_APPLICATION_ID + "&va=7.0.0000";
    }

    protected String getDefaultTimeSyncEndpoint() {
        return TEST_ENDPOINT + "mbeacon?type=mts";
    }

    protected void validateDefaultRequests(ArrayList<Request> sentRequests, String expectedBeacon) {
        Assert.assertEquals(7, sentRequests.size());

        Assert.assertEquals(RequestType.STATUS, sentRequests.get(0).getRequestType());
        Assert.assertEquals("GET", sentRequests.get(0).getMethod());
        Assert.assertEquals(getDefaultEndpoint(), sentRequests.get(0).getURL());
        Assert.assertEquals(null, sentRequests.get(0).getClientIPAddress());
        Assert.assertEquals("", sentRequests.get(0).getDecodedData());

        for (int i = 1; i < 6; i++) {
            Assert.assertEquals(RequestType.TIMESYNC, sentRequests.get(i).getRequestType());
            Assert.assertEquals("GET", sentRequests.get(i).getMethod());
            Assert.assertEquals(getDefaultTimeSyncEndpoint(), sentRequests.get(i).getURL());
        }

        Assert.assertEquals(RequestType.BEACON, sentRequests.get(6).getRequestType());
        Assert.assertEquals("POST", sentRequests.get(6).getMethod());
        Assert.assertTrue(sentRequests.get(6).getURL().startsWith(getDefaultEndpointStart()));
        Assert.assertTrue(sentRequests.get(6).getURL().endsWith(getDefaultEndpointEnd()));
        Assert.assertEquals(TEST_IP, sentRequests.get(6).getClientIPAddress());
        if (expectedBeacon != null) {
            Assert.assertEquals(expectedBeacon, sentRequests.get(6).getDecodedData());
        }
    }

}