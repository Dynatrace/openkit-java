/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.protocol.HTTPClient;

/**
 * Implementation of an HTTPClientProvider which creates a HTTP client for executing status check, beacon send and time sync requests.
 */
public class DefaultHTTPClientProvider extends HTTPClientProvider {

	@Override
	public HTTPClient provideHTTPClient(String baseURL, String applicationID, int serverID, boolean verbose) {
		return new HTTPClient(baseURL, applicationID, serverID, verbose);
	}

}
