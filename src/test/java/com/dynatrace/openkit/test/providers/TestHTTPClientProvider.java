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

package com.dynatrace.openkit.test.providers;

import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.test.TestHTTPClient;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

import java.util.ArrayList;

public class TestHTTPClientProvider implements HTTPClientProvider {

    private boolean remoteTest;
    private TestHTTPClient testHTTPClient;

    private String statusResponse = null;
    private int statusResponseCode = -1;
    private String timeSyncResponse = null;
    private int timeSyncResponseCode = -1;

    ArrayList<Request> previouslySentRequests = new ArrayList<Request>();

    public TestHTTPClientProvider(boolean remoteTest) {
        this.remoteTest = remoteTest;
    }

    @Override
    public HTTPClient createClient(HTTPClientConfiguration configuration) {
        if (testHTTPClient != null) {
            previouslySentRequests.addAll(testHTTPClient.getSentRequests());
        }

        testHTTPClient = new TestHTTPClient(configuration.getBaseURL(), configuration.getApplicationID(),
            configuration.getServerID(), remoteTest);
        if (statusResponse != null) {
            testHTTPClient.setStatusResponse(statusResponse, statusResponseCode);
        }
        if (timeSyncResponse != null) {
            testHTTPClient.setTimeSyncResponse(timeSyncResponse, timeSyncResponseCode);
        }
        return testHTTPClient;
    }

    public ArrayList<Request> getSentRequests() {
        ArrayList<Request> sentRequests = new ArrayList<Request>();
        sentRequests.addAll(previouslySentRequests);
        if (testHTTPClient != null) {
            sentRequests.addAll(testHTTPClient.getSentRequests());
        }
        return sentRequests;
    }

    public void setStatusResponse(String response, int responseCode) {
        statusResponse = response;
        statusResponseCode = responseCode;
    }

    public void setTimeSyncResponse(String response, int responseCode) {
        timeSyncResponse = response;
        timeSyncResponseCode = responseCode;
    }

}
