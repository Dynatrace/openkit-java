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

/**
 * Concrete builder that creates an {@code OpenKit} instance for AppMon
 */
public class AppMonOpenKitBuilder extends AbstractOpenKitBuilder {

    /**
     * A string, identifying the type of OpenKit this builder is made for.
     */
    public static final String OPENKIT_TYPE = "AppMonOpenKit";

    private final String applicationName;

    /**
     * Creates a new instance of type AppMonOpenKitBuilder
     *
     * @param endpointURL     endpoint OpenKit connects to
     * @param applicationName unique application id
     * @param deviceID        unique device id
     */
    public AppMonOpenKitBuilder(String endpointURL, String applicationName, long deviceID) {
        this(endpointURL, applicationName, Long.toString(deviceID));
    }

    /**
     * Creates a new instance of type AppMonOpenKitBuilder
     *
     * <p>
     *     If the given {@code deviceID} is longer than 250 characters,
     *     only the first 250 characters are used.
     * </p>
     *
     * @param endpointURL     endpoint OpenKit connects to
     * @param applicationName unique application id
     * @param deviceID        unique device id
     */
    public AppMonOpenKitBuilder(String endpointURL, String applicationName, String deviceID) {
        super(endpointURL, deviceID);
        this.applicationName = applicationName;
    }

    @Override
    public String getOpenKitType() {
        return OPENKIT_TYPE;
    }

    @Override
    public String getApplicationID() {
        // in AppMon application name and ID are the same
        return getApplicationName();
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }
}
