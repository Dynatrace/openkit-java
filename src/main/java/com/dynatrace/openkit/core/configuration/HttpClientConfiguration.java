package com.dynatrace.openkit.core.configuration;

/**
 * The HttpClientConfiguration holds all http client related settings
 */
public class HttpClientConfiguration {

	// all fields are immutable
	private final String baseUrl;
	private final int serverId;
	private final String applicationId;
	private final boolean verbose;

	public HttpClientConfiguration(String baseUrl, int serverId, String applicationId, boolean verbose) {
		this.baseUrl = baseUrl;
		this.serverId = serverId;
		this.applicationId = applicationId;
		this.verbose = verbose;
	}

	/**
	 * Returns the base url for the http client
	 *
	 * @return the base url
	 */
	public String getBaseUrl() { return baseUrl; }

	/**
	 * Returns the server id to be used for the http client
	 *
	 * @return the server id
	 */
	public int getServerId() { return serverId; }

	/**
	 * The application id for the http client
	 *
	 * @return the application id
	 */
	public String getApplicationID() { return applicationId; }

	/**
	 * If {@code true} logging is enabled
	 *
	 * @return If {@code true} logging is enabled otherwise {@code false}
	 */
	public boolean isVerbose() { return verbose; }
}
