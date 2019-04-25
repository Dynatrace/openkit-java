/**
 * Copyright 2018-2019 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.protocol.ssl;

import com.dynatrace.openkit.api.SSLTrustManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of {@link SSLTrustManager} trusting only valid certificates.
 * <p>
 * <p>
 * This is the default strategy in SSL certificate validation and should NOT be changed.
 * </p>
 */
public class SSLStrictTrustManager implements SSLTrustManager {

    private X509TrustManager cachedTrustManager = null;
    private String defaultAlgorithm = null;

    @Override
    public X509TrustManager getX509TrustManager() {

        String tmfDefaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        if (defaultAlgorithm == null || !defaultAlgorithm.equals(tmfDefaultAlgorithm)) {
            // not initialized yet or default algorithm was changed
            // the default algorithm may be changed during runtime by calling
            // Security.setProperty with property name "ssl.TrustManagerFactory.algorithm".
            cachedTrustManager = getX509TrustManager(tmfDefaultAlgorithm);
            defaultAlgorithm = tmfDefaultAlgorithm;
        }

        return cachedTrustManager;
    }

    private static X509TrustManager getX509TrustManager(String algorithm) {

        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
            factory.init((KeyStore) null); // default keystore

            // get the first X509TrustManager instance
            for (TrustManager trustManager : factory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    return (X509TrustManager) trustManager;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            // intentionally left empty
        } catch (KeyStoreException e) {
            // intentionally left empty
        }

        return null;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        // get the default hostname verifier
        // note this might be altered using HttpsURLConnection.setDefaultHostnameVerifier
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }
}
