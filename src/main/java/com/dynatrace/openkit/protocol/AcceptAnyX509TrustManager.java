/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.protocol;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Trivial TrustManager that simply accepts any X509Certificate.
 */
public class AcceptAnyX509TrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// accept anything
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// accept anything
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}
