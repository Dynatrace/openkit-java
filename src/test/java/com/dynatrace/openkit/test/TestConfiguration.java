/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.test;

import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.core.Device;

public class TestConfiguration {

    private long deviceID = -1;
    private String statusResponse = null;
    private int statusResponseCode = -1;
    private String timeSyncResponse = null;
    private int timeSyncResponseCode = -1;

    // set defaults for device
    private Device device = new Device(
        OpenKitConstants.DEFAULT_OPERATING_SYSTEM,
        OpenKitConstants.DEFAULT_MANUFACTURER,
        OpenKitConstants.DEFAULT_MODEL_ID);
    // set default for version
    private String applicationVersion = OpenKitConstants.DEFAULT_APPLICATION_VERSION;

    public void setDeviceID(long deviceID) {
        this.deviceID = deviceID;
    }

    public void setStatusResponse(String response, int responseCode) {
        statusResponse = response;
        statusResponseCode = responseCode;
    }

    public void setTimeSyncResponse(String response, int responseCode) {
        timeSyncResponse = response;
        timeSyncResponseCode = responseCode;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public long getDeviceID() {
        return deviceID;
    }

    public String getStatusResponse() {
        return statusResponse;
    }

    public int getStatusResponseCode() {
        return statusResponseCode;
    }

    public String getTimeSyncResponse() {
        return timeSyncResponse;
    }

    public int getTimeSyncResponseCode() {
        return timeSyncResponseCode;
    }

    public Device getDevice() {
        return device;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }
}
