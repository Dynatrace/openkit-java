/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.core.configuration.HttpClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;

/**
 * Abstract class for providing an HTTP client. Mostly needed for testing purposes.
 */
public interface HTTPClientProvider {
	HTTPClient createClient(HttpClientConfiguration configuration);
}
