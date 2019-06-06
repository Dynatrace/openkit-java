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

import com.dynatrace.openkit.AbstractOpenKitBuilder;

/**
 * Configuration class storing all configuration parameters that have been configured via
 * {@link com.dynatrace.openkit.DynatraceOpenKitBuilder} or {@link com.dynatrace.openkit.AppMonOpenKitBuilder}.
 */
public class OpenKitConfiguration {

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

    private OpenKitConfiguration(AbstractOpenKitBuilder builder) {
        openKitType = builder.getOpenKitType();
        applicationID = builder.getApplicationID();
        applicationName = builder.getApplicationName();
        applicationVersion = builder.getApplicationVersion();
        operatingSystem = builder.getOperatingSystem();
        manufacturer = builder.getManufacturer();
        modelID = builder.getModelID();
        defaultServerID = builder.getDefaultServerID();
    }

    public static OpenKitConfiguration from(AbstractOpenKitBuilder builder) {
        if (builder == null) {
            return null;
        }
        return new OpenKitConfiguration(builder);
    }

    public String getOpenKitType() {
        return openKitType;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelID() {
        return modelID;
    }

    public int getDefaultServerID() {
        return defaultServerID;
    }
}
