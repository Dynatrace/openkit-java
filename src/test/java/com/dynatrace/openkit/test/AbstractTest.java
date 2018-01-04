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

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.protocol.HTTPClient.RequestType;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import org.junit.Assert;

public abstract class AbstractTest {

    protected OpenKit openKit;
    protected OpenKitTestImpl openKitTestImpl;

    protected void validateRequest(Request request, RequestType requestType, String method, String url, String clientIPAddress, String expectedBeacon) {
        Assert.assertEquals(requestType, request.getRequestType());
        Assert.assertEquals(method, request.getMethod());
        Assert.assertEquals(url, request.getURL());
        Assert.assertEquals(clientIPAddress, request.getClientIPAddress());
        if (expectedBeacon != null) {
            Assert.assertEquals(expectedBeacon, request.getDecodedData());
        }
    }

}