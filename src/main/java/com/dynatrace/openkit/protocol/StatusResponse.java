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

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;

import java.util.List;
import java.util.Map;

/**
 * Implements a status response which is sent for the request types status check and beacon send.
 */
public class StatusResponse extends Response {

    // status response constants
    static final String RESPONSE_KEY_CAPTURE = "cp";
    static final String RESPONSE_KEY_SEND_INTERVAL = "si";
    static final String RESPONSE_KEY_MONITOR_NAME = "bn";
    static final String RESPONSE_KEY_SERVER_ID = "id";
    static final String RESPONSE_KEY_MAX_BEACON_SIZE = "bl";
    static final String RESPONSE_KEY_CAPTURE_ERRORS = "er";
    static final String RESPONSE_KEY_CAPTURE_CRASHES = "cr";
    static final String RESPONSE_KEY_MULTIPLICITY = "mp";

    // settings contained in status response
    private boolean capture = true;
    private int sendInterval = -1;
    private String monitorName = null;
    private int serverID = -1;
    private int maxBeaconSize = -1;
    private boolean captureErrors = true;
    private boolean captureCrashes = true;
    private int multiplicity = 1;

    // *** constructors ***

    public StatusResponse(Logger logger, String response, int responseCode, Map<String, List<String>> headers) {
        super(logger, responseCode, headers);
        parseResponse(response);
    }

    // *** private methods ***

    // parses status check response
    private void parseResponse(String response) {

        if (response == null || response.isEmpty()) {
            return;
        }

        List<KeyValuePair> parsedResponse = Response.parseResponseKeyValuePair(response);
        for (KeyValuePair kv : parsedResponse) {

            if (RESPONSE_KEY_CAPTURE.equals(kv.key)) {
                capture = (Integer.parseInt(kv.value) == 1);
            } else if (RESPONSE_KEY_SEND_INTERVAL.equals(kv.key)) {
                sendInterval = Integer.parseInt(kv.value) * 1000;
            } else if (RESPONSE_KEY_MONITOR_NAME.equals(kv.key)) {
                monitorName = kv.value;
            } else if (RESPONSE_KEY_SERVER_ID.equals(kv.key)) {
                serverID = Integer.parseInt(kv.value);
            } else if (RESPONSE_KEY_MAX_BEACON_SIZE.equals(kv.key)) {
                maxBeaconSize = Integer.parseInt(kv.value) * 1024;
            } else if (RESPONSE_KEY_CAPTURE_ERRORS.equals(kv.key)) {
                captureErrors = (Integer.parseInt(kv.value) != 0);                    // 1 (always on) and 2 (only on WiFi) are treated the same
            } else if (RESPONSE_KEY_CAPTURE_CRASHES.equals(kv.key)) {
                captureCrashes = (Integer.parseInt(kv.value) != 0);                // 1 (always on) and 2 (only on WiFi) are treated the same
            } else if (RESPONSE_KEY_MULTIPLICITY.equals(kv.key)) {
                multiplicity = Integer.parseInt(kv.value);
            }
        }
    }

    // *** getter methods ***

    public boolean isCapture() {
        return capture;
    }

    public int getSendInterval() {
        return sendInterval;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public int getServerID() {
        return serverID;
    }

    public int getMaxBeaconSize() {
        return maxBeaconSize;
    }

    public boolean isCaptureErrors() {
        return captureErrors;
    }

    public boolean isCaptureCrashes() {
        return captureCrashes;
    }

    public int getMultiplicity() {
        return multiplicity;
    }
}
