package com.dynatrace.openkit.core.configuration;

public class DynatraceManagedConfiguration extends Configuration {

	private final String tenantId;

	public DynatraceManagedConfiguration(String tenantId, String applicationName, String applicationID, long visitorID,
			String endpointURL, boolean verbose) {
		super(OpenKitType.DYNATRACE, applicationName, applicationID, visitorID, endpointURL, verbose);

		this.tenantId = tenantId;
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
