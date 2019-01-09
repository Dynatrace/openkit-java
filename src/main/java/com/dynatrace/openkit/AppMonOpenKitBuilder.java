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

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
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
    public AppMonOpenKitBuilder(String endpointURL, String applicationName, long deviceID) {
        super(endpointURL, deviceID);
        this.applicationName = applicationName;
    }

    @Override
    Configuration buildConfiguration() {
        Device device = new Device(getOperatingSystem(), getManufacturer(), getModelID());

        BeaconCacheConfiguration beaconCacheConfiguration = new BeaconCacheConfiguration(getBeaconCacheMaxRecordAge(),
            getBeaconCacheLowerMemoryBoundary(),
            getBeaconCacheUpperMemoryBoundary());
        BeaconConfiguration beaconConfiguration = new BeaconConfiguration(BeaconConfiguration.DEFAULT_MULITPLICITY,
            getDataCollectionLevel(),
            getCrashReportLevel());
        return new Configuration(
            OpenKitType.APPMON,
            applicationName,
            applicationName,
            getDeviceID(),
            getEndpointURL(),
            new DefaultSessionIDProvider(),
            getTrustManager(),
            device,
            getApplicationVersion(),
            beaconCacheConfiguration,
            beaconConfiguration);
    }
}
