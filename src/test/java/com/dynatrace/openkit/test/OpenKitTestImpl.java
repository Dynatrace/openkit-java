/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
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
        when(provider.getThreadID()).thenReturn(1L);
        return provider;
    }

}
