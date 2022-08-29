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

import com.dynatrace.openkit.DynatraceOpenKitBuilder;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.api.http.HttpRequestInterceptor;
import com.dynatrace.openkit.api.http.HttpResponseInterceptor;
import com.dynatrace.openkit.core.util.PercentEncoder;

/**
 * Configuration class storing all configuration parameters that have been configured via
 * {@link com.dynatrace.openkit.DynatraceOpenKitBuilder}.
 */
public class OpenKitConfiguration {

    /** Character set used to encode application & device ID */
    private static final String ENCODING_CHARSET = "UTF-8";
    /** Underscore is a reserved character in the server, therefore it also needs to be encoded */
    private static final char[] RESERVED_CHARACTERS = {'_'};

    /** The endpoint URL to send data to. */
    private final String endpointURL;
    /** Unique device/installation identifier */
    private final long deviceID;
    /** Unique device/installation identifier in an not-hashed representation as it as passed to the OpenKit builder */
    private final String origDeviceID;
    /**
     * OpenKit's type string
     *
     * <p>
     *     This is a string that can be used for logging.
     * </p>
     */
    private final String openKitType;
    /** Application identifier for which to report data */
    private final String applicationID;
    /** Percent encoded {@link #applicationID} */
    private final String percentEncodedApplicationID;
    /** Application's name */
    private final String applicationName;
    /** Application's version */
    private final String applicationVersion;
    /** Operating system */
    private final String operatingSystem;
    /** Device's manufacturer */
    private final String manufacturer;
    /** Model identifier */
    private final String modelID;
    /** Default server id to communicate with */
    private final int defaultServerID;
    /** SSL trust manager configured in OpenKit builder */
    private final SSLTrustManager sslTrustManager;
    /** HTTP request interceptor configured in OpenKit builder */
    private final HttpRequestInterceptor httpRequestInterceptor;
    /** HTTP response interceptor configured in OpenKit builder */
    private final HttpResponseInterceptor httpResponseInterceptor;

    /**
     * Initialize this configuration.
     *
     * @param builder The OpenKit builder storing all configuration relevant data.
     */
    private OpenKitConfiguration(DynatraceOpenKitBuilder builder) {
        endpointURL = builder.getEndpointURL();
        deviceID = builder.getDeviceID();
        origDeviceID = builder.getOrigDeviceID();
        openKitType = builder.getOpenKitType();
        applicationID = builder.getApplicationID();
        percentEncodedApplicationID = PercentEncoder.encode(applicationID, ENCODING_CHARSET, RESERVED_CHARACTERS);
        applicationName = builder.getApplicationName();
        applicationVersion = builder.getApplicationVersion();
        operatingSystem = builder.getOperatingSystem();
        manufacturer = builder.getManufacturer();
        modelID = builder.getModelID();
        defaultServerID = builder.getDefaultServerID();
        sslTrustManager = builder.getTrustManager();
        httpRequestInterceptor = builder.getHttpRequestInterceptor();
        httpResponseInterceptor = builder.getHttpResponseInterceptor();
    }

    /**
     * Create a {@link OpenKitConfiguration} from given {@link DynatraceOpenKitBuilder}.
     *
     * @param builder The OpenKit builder for which to create a {@link PrivacyConfiguration}.
     * @return Newly created {@link PrivacyConfiguration} or {@code null} if given argument is {@code null}
     */
    public static OpenKitConfiguration from(DynatraceOpenKitBuilder builder) {
        if (builder == null) {
            return null;
        }
        return new OpenKitConfiguration(builder);
    }

    /**
     * Get the Beacon endpoint URL to communicate with.
     *
     * @return Beacon endpoint URL
     */
    public String getEndpointURL() {
        return endpointURL;
    }

    /**
     * Get the unique device identifier.
     *
     * @return Unique device identifier.
     */
    public long getDeviceID() {
        return deviceID;
    }

    /**
     * Returns the {@link #getDeviceID() device ID} in a not-hashed representation.
     *
     * @return the device identifier as it was originally passed to OpenKit.
     */
    public String getOrigDeviceID() {
        return origDeviceID;
    }

    /**
     * Get the OpenKit type.
     *
     * @return OpenKit type.
     */
    public String getOpenKitType() {
        return openKitType;
    }

    /**
     * Get application identifier.
     *
     * @return Custom application identifier.
     */
    public String getApplicationID() {
        return applicationID;
    }

    /**
     * Get percent encoded application identifier.
     *
     * @return Custom application identifier, percent encoded.
     */
    public String getPercentEncodedApplicationID() {
        return percentEncodedApplicationID;
    }

    /**
     * Get application name.
     *
     * @return Custom application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get application version.
     *
     * @return Application version.
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * Get device operating system.
     *
     * @return Device's operating system.
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Get device manufacturer.
     *
     * @return Device's manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Get device model identifier.
     *
     * @return Device's model identifier.
     */
    public String getModelID() {
        return modelID;
    }

    /**
     * Get default Dynatrace server id to communicate with.
     *
     * @return Default Dynatrace server id.
     */
    public int getDefaultServerID() {
        return defaultServerID;
    }

    /**
     * Get {@link SSLTrustManager}.
     *
     * @return {@link SSLTrustManager}.
     */
    public SSLTrustManager getSSLTrustManager() {
        return sslTrustManager;
    }

    /**
     * Get {@link HttpRequestInterceptor} configured in builder.
     *
     * @return {@link HttpRequestInterceptor}.
     */
    public HttpRequestInterceptor getHttpRequestInterceptor() {
        return httpRequestInterceptor;
    }

    /**
     * Get {@link HttpResponseInterceptor} configured in builder.
     *
     * @return {@link HttpResponseInterceptor}.
     */
    public HttpResponseInterceptor getHttpResponseInterceptor() {
        return httpResponseInterceptor;
    }
}
