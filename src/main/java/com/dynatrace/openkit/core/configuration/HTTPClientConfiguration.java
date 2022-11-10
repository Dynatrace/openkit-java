/**
 * Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.api.http.HttpRequestInterceptor;
import com.dynatrace.openkit.api.http.HttpResponseInterceptor;

/**
 * The HTTPClientConfiguration holds all http client related settings
 */
public class HTTPClientConfiguration {

    private final String baseURL;
    private final int serverID;
    private final String applicationID;
    private final SSLTrustManager sslTrustManager;
    private final HttpRequestInterceptor httpRequestInterceptor;
    private final HttpResponseInterceptor httpResponseInterceptor;
    private final long deviceID;

    private HTTPClientConfiguration(Builder builder) {
        this.baseURL = builder.baseURL;
        this.serverID = builder.serverID;
        this.applicationID = builder.applicationID;
        this.sslTrustManager = builder.sslTrustManager;
        this.httpRequestInterceptor = builder.httpRequestInterceptor;
        this.httpResponseInterceptor = builder.httpResponseInterceptor;
        this.deviceID = builder.deviceID;
    }

    /**
     * Creates a new {@link HTTPClientConfiguration} instance and initializes it from the given
     * {@link OpenKitConfiguration}.
     *
     * @param openKitConfig the openKit configuration from which the instance will be initialized.
     * @return a new {@link HTTPClientConfiguration} instance initialized from the given configuration.
     */
    public static HTTPClientConfiguration from(OpenKitConfiguration openKitConfig) {
        return modifyWith(openKitConfig).build();
    }

    /**
     * Creates a new builder instance and initializes it from the given {@link OpenKitConfiguration}
     *
     * @param openKitConfig the {@link OpenKitConfiguration} from which the builder will be initialized.
     * @return a pre initialized builder instance for creating a new {@link HTTPClientConfiguration}
     */
    public static Builder modifyWith(OpenKitConfiguration openKitConfig) {
        return new Builder()
                .withBaseURL(openKitConfig.getEndpointURL())
                .withApplicationID(openKitConfig.getApplicationID())
                .withSSLTrustManager(openKitConfig.getSSLTrustManager())
                .withServerID(openKitConfig.getDefaultServerID())
                .withHttpRequestInterceptor(openKitConfig.getHttpRequestInterceptor())
                .withHttpResponseInterceptor(openKitConfig.getHttpResponseInterceptor())
                .withDeviceID(openKitConfig.getDeviceID());
    }

    /**
     * Creates a new builder instance and initializes it from the given {@link HTTPClientConfiguration}
     *
     * @param httpClientConfig the {@link HTTPClientConfiguration} from which the builder will be initialized.
     * @return a pre initialized builder instance for creating a new {@link HTTPClientConfiguration}
     */
    public static Builder modifyWith(HTTPClientConfiguration httpClientConfig) {
        return new Builder()
                .withBaseURL(httpClientConfig.getBaseURL())
                .withApplicationID(httpClientConfig.getApplicationID())
                .withSSLTrustManager(httpClientConfig.getSSLTrustManager())
                .withServerID(httpClientConfig.getServerID())
                .withHttpRequestInterceptor(httpClientConfig.getHttpRequestInterceptor())
                .withHttpResponseInterceptor(httpClientConfig.getHttpResponseInterceptor())
                .withDeviceID(httpClientConfig.getDeviceID());
    }

    /**
     * Returns the base url for the http client
     *
     * @return the base url
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Returns the server id to be used for the http client
     *
     * @return the server id
     */
    public int getServerID() {
        return serverID;
    }

    /**
     * The application id for the http client
     *
     * @return the application id
     */
    public String getApplicationID() {
        return applicationID;
    }

    /**
     * Returns an interface used for X509 certificate authentication and hostname verification.
     */
    public SSLTrustManager getSSLTrustManager() {
        return sslTrustManager;
    }

    /**
     * Returns an interface used to intercept HTTP requests, before they are sent to the Dynatrace backend.
     */
    public HttpRequestInterceptor getHttpRequestInterceptor() {
        return httpRequestInterceptor;
    }

    /**
     * Returns an interface used to intercept HTTP responses received from Dynatrace backend.
     */
    public HttpResponseInterceptor getHttpResponseInterceptor() {
        return httpResponseInterceptor;
    }

    /**
     * Returns the unique device identifier
     */
    public long getDeviceID() { return deviceID; }

    /**
     * Builder class for building {@link HTTPClientConfiguration}.
     */
    public static final class Builder {

        private String baseURL = null;
        private int serverID = -1;
        private String applicationID = null;
        private SSLTrustManager sslTrustManager = null;
        private HttpRequestInterceptor httpRequestInterceptor = null;
        private HttpResponseInterceptor httpResponseInterceptor = null;
        private long deviceID;


        public Builder withBaseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder withServerID(int serverID) {
            this.serverID = serverID;
            return this;
        }

        public Builder withApplicationID(String applicationID) {
            this.applicationID = applicationID;
            return this;
        }

        public Builder withSSLTrustManager(SSLTrustManager sslTrustManager) {
            this.sslTrustManager = sslTrustManager;
            return this;
        }

        public Builder withHttpRequestInterceptor(HttpRequestInterceptor httpRequestInterceptor) {
            this.httpRequestInterceptor = httpRequestInterceptor;
            return this;
        }

        public Builder withHttpResponseInterceptor(HttpResponseInterceptor httpResponseInterceptor) {
            this.httpResponseInterceptor = httpResponseInterceptor;
            return this;
        }

        public Builder withDeviceID(long deviceID) {
            this.deviceID = deviceID;
            return this;
        }

        public HTTPClientConfiguration build() {
            return new HTTPClientConfiguration(this);
        }
    }
}
