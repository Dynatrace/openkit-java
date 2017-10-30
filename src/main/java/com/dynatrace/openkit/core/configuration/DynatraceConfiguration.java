package com.dynatrace.openkit.core.configuration;

public class DynatraceConfiguration extends AbstractConfiguration {

	public DynatraceConfiguration(String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
		super(OpenKitType.DYNATRACE, applicationName, applicationID, visitorID, endpointURL, verbose);
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
