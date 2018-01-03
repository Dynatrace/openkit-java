package com.dynatrace.openkit.api;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;

/**
 * Interface to provide a user-defined trust manager to the configuration.
 * <p>
 * <p>
 * When OpenKit connects to a server with self-signed SSL/TLS certificates (e.g. AppMon) then
 * an implementation of this interface is required to verify the certificate.
 * </p>
 */
public interface SSLTrustManager {

    /**
     * Get the X509TrustManager for SSL/TLS certificate authentication.
     */
    X509TrustManager getX509TrustManager();

    /**
     * Get the HostnameVerifier which checks if a hostname is allowed.
     */
    HostnameVerifier getHostnameVerifier();
}
