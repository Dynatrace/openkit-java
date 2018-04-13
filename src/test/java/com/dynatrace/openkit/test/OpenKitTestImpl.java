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

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.providers.DefaultTimingProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.providers.TestHTTPClientProvider;
import com.dynatrace.openkit.test.providers.TestTimingProvider;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenKitTestImpl extends OpenKitImpl {

    TestHTTPClientProvider testHttpClientProvider;

    public OpenKitTestImpl(Logger logger, Configuration config, boolean remoteTest) {
        this(logger, config, new TestHTTPClientProvider(remoteTest), remoteTest
            ? new DefaultTimingProvider()
            : new TestTimingProvider());
    }

    private OpenKitTestImpl(Logger logger, Configuration config, TestHTTPClientProvider provider, TimingProvider timingProvider) {
        super(logger, config, provider, timingProvider, createThreadIdProvider());
        testHttpClientProvider = provider;
    }

    public ArrayList<Request> getSentRequests() {
        return testHttpClientProvider.getSentRequests();
    }

    public void setStatusResponse(String response, int responseCode) {
        testHttpClientProvider.setStatusResponse(response, responseCode);
    }

    public void setTimeSyncResponse(String response, int responseCode) {
        testHttpClientProvider.setTimeSyncResponse(response, responseCode);
    }

    private static ThreadIDProvider createThreadIdProvider() {
        ThreadIDProvider provider = mock(ThreadIDProvider.class);
        when(provider.getThreadID()).thenReturn(1234567);
        return provider;
    }

}
