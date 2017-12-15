/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.*;
import com.dynatrace.openkit.test.TestHTTPClient.Request;
import com.dynatrace.openkit.test.providers.TestHTTPClientProvider;
import com.dynatrace.openkit.test.providers.TestTimingProvider;

public class OpenKitTestImpl extends OpenKitImpl {

	TestHTTPClientProvider testHttpClientProvider;

	public OpenKitTestImpl(AbstractConfiguration config, boolean remoteTest) throws InterruptedException {
		this(config, new TestHTTPClientProvider(remoteTest), remoteTest ? new DefaultTimingProvider() : new TestTimingProvider());
	}

	private OpenKitTestImpl(AbstractConfiguration config, TestHTTPClientProvider provider, TimingProvider timingProvider) {
		super(config, provider, timingProvider, createThreadIdProvider());

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
