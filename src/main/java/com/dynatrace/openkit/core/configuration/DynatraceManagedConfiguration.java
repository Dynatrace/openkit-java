package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.providers.DefaultSessionIDProvider;

/**
 * Configuration implementation for Dynatrace Managed
 */
public class DynatraceManagedConfiguration extends AbstractConfiguration {

	private final String tenantId;

	public DynatraceManagedConfiguration(String tenantId, String applicationName, String applicationID, long deviceID, String endpointURL, boolean verbose, SSLTrustManager sslTrustManager) {
		super(OpenKitType.DYNATRACE, applicationName, applicationID, deviceID, endpointURL, verbose, new DefaultSessionIDProvider());
		this.tenantId = tenantId;

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
		urlBuilder.append("/");

		urlBuilder.append(tenantId);

		return urlBuilder.toString();
	}
}
