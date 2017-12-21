package com.dynatrace.openkit.protocol.ssl;

import com.dynatrace.openkit.api.SSLTrustManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Implementation of {@link SSLTrustManager} blindly trusting every certificate and every host.
 *
 * <p>
 *     This class is intended to be used only during development phase. Since local
 *     development environments use self-signed certificates only.
 *     This implementation disables any X509 certificate validation and hostname validation.
 * </p>
 *
 * <p>
 *     NOTE: DO NOT USE THIS IN PRODUCTION!!
 * </p>
 */
public class SSLBlindTrustManager implements SSLTrustManager {

    private final X509TrustManager blindX509TrustManager = new BlindX509TrustManager();
    private final HostnameVerifier blindHostnameVerifier = new BlindHostnameVerifier();

    public SSLBlindTrustManager() {
        System.err.println("###########################################################");
        System.err.println("# WARNING: YOU ARE BYPASSING SSL CERTIFICATE VALIDATION!! #");
        System.err.println("#                 USE AT YOUR OWN RISK!!                  #");
        System.err.println("###########################################################");
    }

    @Override
    public X509TrustManager getX509TrustManager() {
        return blindX509TrustManager;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return blindHostnameVerifier;
    }

    /**
     * Implementation of {@link X509TrustManager} which is blindly trusting all certificates.
     */
    private static final class BlindX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // intentionally left empty to trust everything
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // intentionally left empty to trust everything
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    /**
     * Implementation of {@link HostnameVerifier} allowing all host names.
     */
    private static final class BlindHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
