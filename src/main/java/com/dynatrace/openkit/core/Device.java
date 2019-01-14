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

package com.dynatrace.openkit.core;

/**
 * Class holding device specific information
 */
public class Device {

    // platform information
    private final String operatingSystem;
    private final String manufacturer;
    private final String modelID;

    public Device(String operatingSystem, String manufacturer, String modelID) {

        this.operatingSystem = operatingSystem;
        this.manufacturer = manufacturer;
        this.modelID = modelID;
    }

    // *** getter methods ***

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModelID() {
        return modelID;
    }

}
