/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.protocol.HTTPClient;

/**
 * Abstract class for providing an HTTP client. Mostly needed for testing purposes.
 */
public abstract class HTTPClientProvider {

	private static HTTPClientProvider httpClientProvider = new DefaultHTTPClientProvider();

	protected abstract HTTPClient provideHTTPClient(String baseURL, String applicationID, int serverID, boolean verbose);

	public static HTTPClient createHTTPClient(String baseURL, String applicationID, int serverID, boolean verbose) {
		return httpClientProvider.provideHTTPClient(baseURL, applicationID, serverID, verbose);
	}

	// FOR TESTS ONLY: set HTTP client provider
	public static void setHTTPClientProvider(HTTPClientProvider httpClientProvider) {
		HTTPClientProvider.httpClientProvider = httpClientProvider;
	}

}
