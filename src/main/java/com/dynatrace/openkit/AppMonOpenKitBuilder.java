package com.dynatrace.openkit;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;
import com.dynatrace.openkit.providers.DefaultSessionIDProvider;

/**
 * Concrete builder that creates an {@code OpenKit} instance for AppMon
 */
public class AppMonOpenKitBuilder extends AbstractOpenKitBuilder {

    private final String applicationName;

    /**
     * Creates a new instance of type AppMonOpenKitBuilder
     *
     * @param endpointURL     endpoint OpenKit connects to
     * @param applicationName unique application id
     * @param deviceID        unique device id
     */
    AppMonOpenKitBuilder(String endpointURL, String applicationName, long deviceID) {
        super(endpointURL, deviceID);
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
            new DefaultSessionIDProvider(),
            getTrustManager(),
            device,
            getApplicationVersion());
    }
}
