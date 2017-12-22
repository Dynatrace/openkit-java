package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.SSLTrustManager;

/**
 * The HTTPClientConfiguration holds all http client related settings
 */
public class HTTPClientConfiguration {

	// all fields are immutable
	private final String baseURL;
	private final int serverId;
	private final String applicationID;
	private final SSLTrustManager sslTrustManager;

	public HTTPClientConfiguration(String baseURL, int serverID, String applicationID, SSLTrustManager sslTrustManager) {
		this.baseURL = baseURL;
		this.serverId = serverID;
		this.applicationID = applicationID;
		this.sslTrustManager = sslTrustManager;
	}

	/**
	 * Returns the base url for the http client
	 *
	 * @return the base url
	 */
	public String getBaseURL() { return baseURL; }

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
	 * Returns an interface used for X509 certificate authentication and hostname verification.
	 */
	public SSLTrustManager getSSLTrustManager() {
		return sslTrustManager;
	}
}
