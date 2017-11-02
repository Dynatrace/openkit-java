/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.core.configuration.HttpClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;

/**
 * Implementation of an HTTPClientProvider which creates a HTTP client for executing status check, beacon send and time sync requests.
 */
public class DefaultHTTPClientProvider implements HTTPClientProvider {

	@Override
	public HTTPClient createClient(HttpClientConfiguration configuration) {
		return new HTTPClient(configuration.getBaseUrl(), configuration.getApplicationId(), configuration.getServerId(), configuration.isVerbose());
	}

}
