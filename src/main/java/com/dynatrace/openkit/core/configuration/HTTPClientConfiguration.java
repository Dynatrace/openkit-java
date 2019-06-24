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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;

/**
 * The HTTPClientConfiguration holds all http client related settings
 */
public class HTTPClientConfiguration {

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
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Returns the server id to be used for the http client
     *
     * @return the server id
     */
    public int getServerID() {
        return serverId;
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
}
