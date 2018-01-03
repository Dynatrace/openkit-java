package com.dynatrace.openkit;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;

/**
 * Abstract base class for concrete builder. Using the builder a OpenKit instance can be created
 */
public abstract class AbstractOpenKitBuilder {

    // immutable fields
    private final String endpointURL;
    private final long deviceID;

    // mutable fields
    private SSLTrustManager trustManager = new SSLStrictTrustManager();
    private boolean verbose;
    private String operatingSystem = OpenKitConstants.DEFAULT_OPERATING_SYSTEM;
    private String manufacturer = OpenKitConstants.DEFAULT_MANUFACTURER;
    private String modelID = OpenKitConstants.DEFAULT_MODEL_ID;
    private String applicationVersion = OpenKitConstants.DEFAULT_APPLICATION_VERSION;

    /**
     * Creates a new instance of type AbstractOpenKitBuilder
     *
     * @param endpointURL   endpoint OpenKit connects to
     * @param deviceID      unique device id
     */
    AbstractOpenKitBuilder(String endpointURL, long deviceID) {
        this.endpointURL = endpointURL;
        this.deviceID = deviceID;
    }

    // ** public methods **

    /**
     * Enables verbose mode.
     *
     * @return {@code this}
     */
    public AbstractOpenKitBuilder enableVerbose() {
        verbose = true;
        return this;
    }

    /**
     * Defines the version of the application. The value is only set if it is not null and or empty.
     *
     * @param applicationVersion
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
     * @param trustManager Trust manager implementation
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withTrustManager(SSLTrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    /**
     * Sets the operating system information. The value is only set if it is not null and or empty.
     *
     * @param operatingSystem the operating system
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withOperatingSystem(String operatingSystem) {
        if(operatingSystem != null && !operatingSystem.isEmpty()) {
            this.operatingSystem = operatingSystem;
        }
        return this;
    }

    /**
     * Sets the manufacturer information. The value is only set if it is not null and or empty.
     *
     * @param manufacturer the manufacturer
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withManufacturer(String manufacturer) {
        if(manufacturer != null && !manufacturer.isEmpty()) {
            this.manufacturer = manufacturer;
        }
        return this;
    }

    /**
     * Sets the model id. The value is only set if it is not null and or empty.
     *
     * @param modelID the model id
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withModelID(String modelID) {
        if(modelID != null && !modelID.isEmpty()) {
            this.modelID = modelID;
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
        Logger logger = new DefaultLogger(verbose);
        OpenKitImpl openKit = new OpenKitImpl(logger, buildConfiguration());
        openKit.initialize();

        return openKit;
    }

    // ** protected getter **

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

    long getDeviceID() {
        return deviceID;
    }

    SSLTrustManager getTrustManager() {
        return trustManager;
    }
}
