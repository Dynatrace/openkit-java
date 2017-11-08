/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.TestHTTPClientProvider;
import com.dynatrace.openkit.providers.TestThreadIDProvider;
import com.dynatrace.openkit.providers.TestTimeProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimeProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

public class OpenKitTestImpl extends OpenKitImpl {

	TestHTTPClientProvider testHttpClientProvider;

	public OpenKitTestImpl(AbstractConfiguration config, boolean remoteTest) {
		this(config, remoteTest, new TestHTTPClientProvider(remoteTest), createMockTimingProvider());
	}

	private OpenKitTestImpl(AbstractConfiguration config, boolean remoteTest, TestHTTPClientProvider provider, TimingProvider timingProvider) {
		super(config, provider, timingProvider);

		testHttpClientProvider = provider;

		// only generate pseudo-data if it's a local test -> only in this case beacon comparisons make sense
		if (!remoteTest) {
			TimeProvider.setTimeProvider(new TestTimeProvider());
			ThreadIDProvider.setThreadIDProvider(new TestThreadIDProvider());
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

	private static TimingProvider createMockTimingProvider() {

        TimingProvider provider = mock(TimingProvider.class);
        when(provider.provideTimestampInMilliseconds()).thenAnswer(new Answer<Long>() {

            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return TimeProvider.getTimestamp();
            }
        });

        return provider;
    }

}
