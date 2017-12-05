/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import com.dynatrace.openkit.providers.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

public class OpenKitTestImpl extends OpenKitImpl {

	TestHTTPClientProvider testHttpClientProvider;

	public OpenKitTestImpl(AbstractConfiguration config, boolean remoteTest) throws InterruptedException {
		this(config, remoteTest, new TestHTTPClientProvider(remoteTest), createMockTimingProvider());
	}

	private OpenKitTestImpl(AbstractConfiguration config, boolean remoteTest, TestHTTPClientProvider provider, TimingProvider timingProvider) {
		super(config, provider, timingProvider, createThreadIdProvider());

		testHttpClientProvider = provider;

		// only generate pseudo-data if it's a local test -> only in this case beacon comparisons make sense
		if (!remoteTest) {
			TimeProvider.setTimeProvider(new TestTimeProvider());
		}
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

	private static TimingProvider createMockTimingProvider() throws InterruptedException {

        TimingProvider provider = mock(TimingProvider.class);
        when(provider.provideTimestampInMilliseconds()).thenAnswer(new Answer<Long>() {

            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return TimeProvider.getTimestamp();
            }
        });
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(invocation.getArgumentAt(0, Long.class)); // fake times - make sleep longer
                return null;
            }
        }).when(provider).sleep(anyLong());

        return provider;
    }

    private static ThreadIDProvider createThreadIdProvider() {
		ThreadIDProvider provider = mock(ThreadIDProvider.class);
		when(provider.getThreadID()).thenReturn(1L);
		return provider;
	}

}
