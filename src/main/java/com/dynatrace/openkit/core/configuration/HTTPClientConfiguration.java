package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;

/**
 * The HTTPClientConfiguration holds all http client related settings
 */
public class HTTPClientConfiguration {

	// all fields are immutable
	private final String baseUrl;
	private final int serverId;
	private final String applicationID;
	private final boolean verbose;
	private final SSLTrustManager sslTrustManager;

	public HTTPClientConfiguration(String baseUrl, int serverID, String applicationID, boolean verbose, SSLTrustManager sslTrustManager) {
		this.baseUrl = baseUrl;
		this.serverId = serverID;
		this.applicationID = applicationID;
		this.verbose = verbose;
		this.sslTrustManager = sslTrustManager;
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

	/**
	 * Returns an interface used for X509 certificate authentication and hostname verification.
	 */
	public SSLTrustManager getSslTrustManager() {
		return sslTrustManager;
	}
}
