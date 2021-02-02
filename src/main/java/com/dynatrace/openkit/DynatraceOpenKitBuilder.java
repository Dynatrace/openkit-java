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

package com.dynatrace.openkit;

/**
 * Concrete builder that creates an {@code OpenKit} instance for Dynatrace Saas/Managed
 */
public class DynatraceOpenKitBuilder extends AbstractOpenKitBuilder {

    /**
     * The default server ID to communicate with.
     */
    public static final int DEFAULT_SERVER_ID = 1;

    /**
     * A string, identifying the type of OpenKit this builder is made for.
     */
    public static final String OPENKIT_TYPE = "DynatraceOpenKit";

    private final String applicationID;
    private String applicationName = null;

    /**
     * Creates a new instance of type DynatraceOpenKitBuilder
     *
     * @param endpointURL   endpoint OpenKit connects to
     * @param applicationID unique application id
     * @param deviceID      unique device id
     */
    public DynatraceOpenKitBuilder(String endpointURL, String applicationID, long deviceID) {
        super(endpointURL, deviceID);
        this.applicationID = applicationID;
    }

    /**
     * Creates a new instance of type DynatraceOpenKitBuilder
     *
     * <p>
     *     If the given {@code deviceID} does not correspond to a numeric value it will be hashed accordingly to a
     *     64 bit number.
     * </p>
     *
     * @param endpointURL   endpoint OpenKit connects to
     * @param applicationID unique application id
     * @param deviceID      unique device id
     *
     * @deprecated  use {@link #DynatraceOpenKitBuilder(String, String, long)} instead
     */
    @Deprecated
    public DynatraceOpenKitBuilder(String endpointURL, String applicationID, String deviceID) {
        super(endpointURL, deviceID);
        this.applicationID = applicationID;
    }

    /**
     * Sets the application name. The value is only set if it is not null.
     *
     * @param applicationName name of the application
     * @return {@code this}
     *
     * @deprecated with version 2.0.0 This value is set in Dynatrace when creating a Custom application.
     */
    @Deprecated
    public AbstractOpenKitBuilder withApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    @Override
    public int getDefaultServerID() {
        return DEFAULT_SERVER_ID;
    }

    @Override
    public String getOpenKitType() {
        return OPENKIT_TYPE;
    }

    @Override
    public String getApplicationID() {
        return applicationID;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }
}
