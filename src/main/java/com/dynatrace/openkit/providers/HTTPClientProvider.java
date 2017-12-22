/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;

/**
 * Interface for providing an HTTP client. Mostly needed for testing purposes.
 */
public interface HTTPClientProvider {

	/**
	 * Returns an HTTPClient based on the provided configuration.
	 */
	HTTPClient createClient(Logger logger, HTTPClientConfiguration configuration);
}
