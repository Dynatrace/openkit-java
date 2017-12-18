package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;

/**
 * Configuration implementation for Dynatrace SaaS
 */
public class DynatraceConfiguration extends AbstractConfiguration {

	public DynatraceConfiguration(String applicationName, String applicationID, long deviceID, String endpointURL, boolean verbose, SSLTrustManager sslTrustManager) {
		super(OpenKitType.DYNATRACE, applicationName, applicationID, deviceID, endpointURL, verbose);

		setHttpClientConfiguration(
			new HTTPClientConfiguration(
				createBaseURL(endpointURL, OpenKitType.DYNATRACE.getDefaultMonitorName()),
				OpenKitType.DYNATRACE.getDefaultServerID(),
				applicationID,
				verbose,
				sslTrustManager));
	}

	@Override
	protected String createBaseURL(String endpointURL, String monitorName) {
		StringBuilder urlBuilder = new StringBuilder();

		urlBuilder.append(endpointURL);
		if (!endpointURL.endsWith("/") && !monitorName.startsWith("/")) {
			urlBuilder.append('/');
		}
		urlBuilder.append(monitorName);

		return urlBuilder.toString();
	}
}
