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

import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import org.junit.Assert;

import java.util.ArrayList;

public abstract class AbstractAppMonTest extends AbstractTest {

    public static final String TEST_APPLICATION_NAME = "TestApplicationName";
    public static final String TEST_APPLICATION_ID = "TestApplicationID";
    public static final String TEST_ENDPOINT = "http://localhost/dynatraceMonitor";
    public static final String TEST_IP = "123.45.67.89";
    public static final String TEST_OPENKIT_DEFAULT_VERSION = OpenKitConstants.DEFAULT_APPLICATION_VERSION;
    public static final String TEST_DEVICE_TYPE = "OpenKitDevice";
    public static final String TEST_MANUFACTURER = "Dynatrace";
    public static final String TEST_OS = "OpenKit+" + OpenKitConstants.DEFAULT_APPLICATION_VERSION;

    protected String getDefaultEndpoint() {
        return TEST_ENDPOINT + "?type=m&srvid=1&app=" + TEST_APPLICATION_NAME + "&va=7.0.0000&pt=1&tt=okjava";
    }

    protected String getDefaultTimeSyncEndpoint() {
        return TEST_ENDPOINT + "?type=mts";
    }

    protected void validateDefaultRequests(ArrayList<Request> sentRequests, String expectedBeacon) {
        Assert.assertEquals(3, sentRequests.size());

        validateRequest(sentRequests.get(0), RequestType.STATUS, "GET", getDefaultEndpoint(), null, "");
        validateRequest(sentRequests.get(1), RequestType.TIMESYNC, "GET", getDefaultTimeSyncEndpoint(), null, "");
        validateRequest(sentRequests.get(2), RequestType.BEACON, "POST", getDefaultEndpoint(), TEST_IP, expectedBeacon);
    }

}