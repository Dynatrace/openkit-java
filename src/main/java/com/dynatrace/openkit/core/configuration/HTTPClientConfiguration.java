package com.dynatrace.openkit.core.configuration;

/**
 * The HTTPClientConfiguration holds all http client related settings
 */
public class HTTPClientConfiguration {

	// all fields are immutable
	private final String baseUrl;
	private final int serverId;
	private final String applicationID;
	private final boolean verbose;

	public HTTPClientConfiguration(String baseUrl, int serverID, String applicationID, boolean verbose) {
		this.baseUrl = baseUrl;
		this.serverId = serverID;
		this.applicationID = applicationID;
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
	public int getServerID() { return serverId; }

	/**
	 * The application id for the http client
	 *
	 * @return the application id
	 */
	public String getApplicationID() { return applicationID; }

	/**
	 * If {@code true} logging is enabled
	 *
	 * @return If {@code true} logging is enabled otherwise {@code false}
	 */
	public boolean isVerbose() { return verbose; }
}
