package com.dynatrace.openkit;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;

/**
 * Concrete builder that creates an {@code OpenKit} instance for AppMon
 */
public class AppMonOpenKitBuilder extends AbstractOpenKitBuilder {

    private final String applicationName;

    /**
     * Creates a new instance of type AppMonOpenKitBuilder
     *
     * @param endpointUrl       endpoint OpenKit connects to
     * @param applicationName   unique application id
     * @param deviceID          unique device id
     */
    AppMonOpenKitBuilder(String endpointUrl, String applicationName, long deviceID) {
        super(endpointUrl, deviceID);
        this.applicationName = applicationName;
    }

    @Override
    Configuration buildConfiguration() {
        Device device = new Device(getOperatingSystem(), getManufacturer(), getModelID());

        return new Configuration(
            OpenKitType.APPMON,
            applicationName,
            applicationName,
            getDeviceID(),
            getEndpointURL(),
            isVerbose(),
            getTrustManager(),
            device,
            getApplicationVersion());
    }
}
