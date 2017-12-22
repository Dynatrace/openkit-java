package com.dynatrace.openkit;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;

/**
 * Concrete builder that creates an {@code OpenKit} instance for Dynatrace Saas/Managed
 */
public class DynatraceOpenKitBuilder extends AbstractOpenKitBuilder {

    private final String applicationID;
    private String applicationName;

    /**
     * Creates a new instance of type DynatraceOpenKitBuilder
     *
     * @param endpointURL       endpoint OpenKit connects to
     * @param applicationID     unique application id
     * @param deviceID          unique device id
     */
    public DynatraceOpenKitBuilder(String endpointURL, String applicationID, long deviceID) {
        super(endpointURL, deviceID);
        this.applicationID = applicationID;
    }

    /**
     * Sets the application name
     *
     * @param applicationName name of the application
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    @Override
    Configuration buildConfiguration() {
        Device device = new Device(getOperatingSystem(), getManufacturer(), getModelID());

        return new Configuration(
            OpenKitType.DYNATRACE,
            applicationName,
            applicationID,
            getDeviceID(),
            getEndpointURL(),
            isVerbose(),
            getTrustManager(),
            device,
            getApplicationVersion());
    }
}
