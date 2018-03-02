/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.protocol;

import java.util.StringTokenizer;

/**
 * Implements a status response which is sent for the request types status check and beacon send.
 */
public class StatusResponse extends Response {

    // status response constants
    public static final String RESPONSE_KEY_CAPTURE = "cp";
    public static final String RESPONSE_KEY_SEND_INTERVAL = "si";
    public static final String RESPONSE_KEY_MONITOR_NAME = "bn";
    public static final String RESPONSE_KEY_SERVER_ID = "id";
    public static final String RESPONSE_KEY_MAX_BEACON_SIZE = "bl";
    public static final String RESPONSE_KEY_CAPTURE_ERRORS = "er";
    public static final String RESPONSE_KEY_CAPTURE_CRASHES = "cr";

    // settings contained in status response
    private boolean capture = true;
    private int sendInterval = -1;
    private String monitorName = null;
    private int serverID = -1;
    private int maxBeaconSize = -1;
    private boolean captureErrors = true;
    private boolean captureCrashes = true;

    // *** constructors ***

    public StatusResponse(String response, int responseCode) {
        super(responseCode);
        parseResponse(response);
    }

    // *** private methods ***

    // parses status check response
    private void parseResponse(String response) {

        if (response == null || response.isEmpty()) {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(response, "&");
        while (tokenizer.hasMoreTokens()) {

            String token = tokenizer.nextToken();
            int keyValueSeparatorIndex = token.indexOf('=');
            if (keyValueSeparatorIndex == -1) {
                throw new IllegalArgumentException("Invalid response; even number of tokens expected.");
            }
            String key = token.substring(0, keyValueSeparatorIndex);
            String value = token.substring(keyValueSeparatorIndex + 1);

            if (RESPONSE_KEY_CAPTURE.equals(key)) {
                capture = (Integer.parseInt(value) == 1);
            } else if (RESPONSE_KEY_SEND_INTERVAL.equals(key)) {
                sendInterval = Integer.parseInt(value) * 1000;
            } else if (RESPONSE_KEY_MONITOR_NAME.equals(key)) {
                monitorName = value;
            } else if (RESPONSE_KEY_SERVER_ID.equals(key)) {
                serverID = Integer.parseInt(value);
            } else if (RESPONSE_KEY_MAX_BEACON_SIZE.equals(key)) {
                maxBeaconSize = Integer.parseInt(value) * 1024;
            } else if (RESPONSE_KEY_CAPTURE_ERRORS.equals(key)) {
                captureErrors = (Integer.parseInt(value) != 0);                    // 1 (always on) and 2 (only on WiFi) are treated the same
            } else if (RESPONSE_KEY_CAPTURE_CRASHES.equals(key)) {
                captureCrashes = (Integer.parseInt(value) != 0);                // 1 (always on) and 2 (only on WiFi) are treated the same
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
}
