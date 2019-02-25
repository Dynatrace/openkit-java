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

package com.dynatrace.openkit;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.LogLevel;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;

/**
 * Abstract base class for concrete builder. Using the builder a OpenKit instance can be created
 */
public abstract class AbstractOpenKitBuilder {

    // immutable fields
    private final String endpointURL;
    private final String deviceID;

    // mutable fields
    private Logger logger;
    private SSLTrustManager trustManager = new SSLStrictTrustManager();
    private LogLevel logLevel = LogLevel.WARN;
    private String operatingSystem = OpenKitConstants.DEFAULT_OPERATING_SYSTEM;
    private String manufacturer = OpenKitConstants.DEFAULT_MANUFACTURER;
    private String modelID = OpenKitConstants.DEFAULT_MODEL_ID;
    private String applicationVersion = OpenKitConstants.DEFAULT_APPLICATION_VERSION;
    private long beaconCacheMaxRecordAge = BeaconCacheConfiguration.DEFAULT_MAX_RECORD_AGE_IN_MILLIS;
    private long beaconCacheLowerMemoryBoundary = BeaconCacheConfiguration.DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES;
    private long beaconCacheUpperMemoryBoundary = BeaconCacheConfiguration.DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES;
    private DataCollectionLevel dataCollectionLevel = BeaconConfiguration.DEFAULT_DATA_COLLECTION_LEVEL;
    private CrashReportingLevel crashReportLevel = BeaconConfiguration.DEFAULT_CRASH_REPORTING_LEVEL;

    /**
     * Creates a new instance of type AbstractOpenKitBuilder
     *
     * @param endpointURL endpoint OpenKit connects to
     * @param deviceID    unique device id
     */
    AbstractOpenKitBuilder(String endpointURL, long deviceID) {
        this(endpointURL, Long.toString(deviceID));
    }

    /**
     * Creates a new instance of type AbstractOpenKitBuilder
     *
     * @param endpointURL endpoint OpenKit connects to
     * @param deviceID    unique device id
     */
    AbstractOpenKitBuilder(String endpointURL, String deviceID) {
        this.endpointURL = endpointURL;
        this.deviceID = deviceID;
    }

    // ** public methods **

    /**
     * Enables verbose mode. Verbose mode is only enabled if the the default logger is used.
     * If a custom logger is provided by calling  {@code withLogger} debug and info log output
     * depends on the values returned by {@code isDebugEnabled} and {@code isInfoEnabled}.
     *
     * @deprecated {@link #withLogLevel(LogLevel)}
     * @return {@code this}
     */
    @Deprecated
    public AbstractOpenKitBuilder enableVerbose() {
        return withLogLevel(LogLevel.DEBUG);
    }

    /**
     * Sets the default log level if the default logger is used.
     * If a custom logger is provided by calling {@link #withLogger(Logger)}, debug and info log output
     * depends on the values returned by {@link Logger#isDebugEnabled()} and {@link Logger#isInfoEnabled()}.
     *
     * @param level The logLevel for the custom logger
     * @return {@link AbstractOpenKitBuilder}
     */
    public AbstractOpenKitBuilder withLogLevel(LogLevel level) {
        logLevel = level;
        return this;
    }

    /**
     * Sets the logger. If no logger is set the default console logger is used. For the default
     * logger verbose mode is enabled by calling {@code enableVerbose}.
     *
     * @param logger the logger
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Defines the version of the application. The value is only set if it is neither null nor empty.
     *
     * @param applicationVersion the application version
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withApplicationVersion(String applicationVersion) {
        if (applicationVersion != null && !applicationVersion.isEmpty()) {
            this.applicationVersion = applicationVersion;
        }
        return this;
    }

    /**
     * Sets the trust manager. Overrides the default trust manager which is {@code SSLStrictTrustmanager} by default-
     *
     * @param trustManager trust manager implementation
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withTrustManager(SSLTrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    /**
     * Sets the operating system information. The value is only set if it is neither null nor empty.
     *
     * @param operatingSystem the operating system
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withOperatingSystem(String operatingSystem) {
        if (operatingSystem != null && !operatingSystem.isEmpty()) {
            this.operatingSystem = operatingSystem;
        }
        return this;
    }

    /**
     * Sets the manufacturer information. The value is only set if it is neither null nor empty.
     *
     * @param manufacturer the manufacturer
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withManufacturer(String manufacturer) {
        if (manufacturer != null && !manufacturer.isEmpty()) {
            this.manufacturer = manufacturer;
        }
        return this;
    }

    /**
     * Sets the model id. The value is only set if it is neither null nor empty.
     *
     * @param modelID the model id
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withModelID(String modelID) {
        if (modelID != null && !modelID.isEmpty()) {
            this.modelID = modelID;
        }
        return this;
    }

    /**
     * Sets the maximum beacon record age of beacon data in cache.
     *
     * @param maxRecordAgeInMilliseconds The maximum beacon record age in milliseconds, or unbounded if negative.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withBeaconCacheMaxRecordAge(long maxRecordAgeInMilliseconds) {
        this.beaconCacheMaxRecordAge = maxRecordAgeInMilliseconds;
        return this;
    }

    /**
     * Sets the lower memory boundary of the beacon cache.
     *
     * <p>
     * When this is set to a positive value the memory based eviction strategy clears the collected data,
     * until the data size in the cache falls below the configured limit.
     * </p>
     *
     * @param lowerMemoryBoundaryInBytes The lower boundary of the beacon cache or negative if unlimited.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withBeaconCacheLowerMemoryBoundary(long lowerMemoryBoundaryInBytes) {
        this.beaconCacheLowerMemoryBoundary = lowerMemoryBoundaryInBytes;
        return this;
    }

    /**
     * Sets the upper memory boundary of the beacon cache.
     *
     * <p>
     * When this is set to a positive value the memory based eviction strategy starts to clear
     * data from the beacon cache when the cache size exceeds this setting.
     * </p>
     *
     * @param upperMemoryBoundaryInBytes The lower boundary of the beacon cache or negative if unlimited.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withBeaconCacheUpperMemoryBoundary(long upperMemoryBoundaryInBytes) {
        this.beaconCacheUpperMemoryBoundary = upperMemoryBoundaryInBytes;
        return this;
    }

    /**
     * Sets the data collection level.
     *
     * Depending on the chosen level the amount and granularity of data sent is controlled.<br>
     * {@code Off (0)} - no data collected<br>
     * {@code PERFORMANCE (1)} - only performance related data is collected<br>
     * {@code USER_BEHAVIOR (2)} - all available RUM data including performance related data is collected<br>
     *
     * Default value: {@code USER_BEHAVIOR}
     *
     * @param dataCollectionLevel Data collection level to apply.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withDataCollectionLevel(DataCollectionLevel dataCollectionLevel) {
        if(dataCollectionLevel != null) {
            this.dataCollectionLevel = dataCollectionLevel;
        }
        return this;
    }

    /**
     * Sets the flag if crash reporting is enabled
     *
     * {@code OFF (0)} - Crashes are not send to the server<br>
     * {@code OPT_OUT_CRASHES (1)} - Crashes are not send to the server<br>
     * {@code OPT_IN_CRASHES (2)} - Crashes are send to the server<br>
     *
     * Default value: {@code OPT_IN_CRASHES}
     *
     * @param crashReportLevel Flag if crash reporting is enabled
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withCrashReportingLevel(CrashReportingLevel crashReportLevel) {
        if(crashReportLevel != null) {
            this.crashReportLevel = crashReportLevel;
        }
        return this;
    }

    /**
     * Builds the configuration for the OpenKit instance
     *
     * @return
     */
    abstract Configuration buildConfiguration();

    /**
     * Builds a new {@code OpenKit} instance
     *
     * @return retursn an {@code OpenKit} instance
     */
    public OpenKit build() {
        // create and initialize OpenKit instance
        OpenKitImpl openKit = new OpenKitImpl(getLogger(), buildConfiguration());
        openKit.initialize();

        return openKit;
    }

    // ** internal getter **

    String getApplicationVersion() {
        return applicationVersion;
    }

    String getOperatingSystem() {
        return operatingSystem;
    }

    String getManufacturer() {
        return manufacturer;
    }

    String getModelID() {
        return modelID;
    }

    String getEndpointURL() {
        return endpointURL;
    }

    String getDeviceID() {
        return deviceID;
    }

    SSLTrustManager getTrustManager() {
        return trustManager;
    }

    long getBeaconCacheMaxRecordAge() {
        return beaconCacheMaxRecordAge;
    }

    long getBeaconCacheLowerMemoryBoundary() {
        return beaconCacheLowerMemoryBoundary;
    }

    long getBeaconCacheUpperMemoryBoundary() {
        return beaconCacheUpperMemoryBoundary;
    }

    DataCollectionLevel getDataCollectionLevel() {
        return dataCollectionLevel;
    }

    CrashReportingLevel getCrashReportLevel() {
        return crashReportLevel;
    }

    Logger getLogger() {
        if (logger != null) {
            return logger;
        }

        return new DefaultLogger(logLevel);
    }
}
