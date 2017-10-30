/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import java.util.ArrayList;

import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TestHTTPClientProvider;
import com.dynatrace.openkit.providers.TestThreadIDProvider;
import com.dynatrace.openkit.providers.TestTimeProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimeProvider;
import com.dynatrace.openkit.test.TestHTTPClient.Request;

public class OpenKitTestImpl extends OpenKitImpl {

	TestHTTPClientProvider testHttpClientProvider;

	public OpenKitTestImpl(AbstractConfiguration config, boolean remoteTest) {
		super(config);

		// only generate pseudo-data if it's a local test -> only in this case beacon comparisons make sense
		if (!remoteTest) {
			TimeProvider.setTimeProvider(new TestTimeProvider());
			ThreadIDProvider.setThreadIDProvider(new TestThreadIDProvider());
		}

		this.testHttpClientProvider = new TestHTTPClientProvider(remoteTest);
		HTTPClientProvider.setHTTPClientProvider(testHttpClientProvider);
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

}
