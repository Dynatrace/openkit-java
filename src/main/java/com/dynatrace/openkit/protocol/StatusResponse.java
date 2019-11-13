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

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Implements a status response which is sent for the request types status check and beacon send.
 */
public class StatusResponse  {

    /**
     * Response code sent by HTTP server to indicate success.
     */
    public static final int HTTP_OK = 200;

    /**
     * Bad request is error code 400 in the HTTP protocol.
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * Too many requests sent by client (rate limiting) error code.
     */
    public static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * Key in the HTTP response headers for Retry-After
     */
    static final String RESPONSE_KEY_RETRY_AFTER = "retry-after";

    // status response constants
    static final String RESPONSE_KEY_CAPTURE = "cp";
    static final String RESPONSE_KEY_SEND_INTERVAL = "si";
    static final String RESPONSE_KEY_MONITOR_NAME = "bn";
    static final String RESPONSE_KEY_SERVER_ID = "id";
    static final String RESPONSE_KEY_MAX_BEACON_SIZE = "bl";
    static final String RESPONSE_KEY_CAPTURE_ERRORS = "er";
    static final String RESPONSE_KEY_CAPTURE_CRASHES = "cr";
    static final String RESPONSE_KEY_MULTIPLICITY = "mp";

    /**
     * Default "Retry-After" is 10 minutes.
     */
    static final long DEFAULT_RETRY_AFTER_IN_MILLISECONDS = 10L * 60L * 1000L;

    private final Logger logger;

    private final int responseCode;
    private final Map<String, List<String>> headers;

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
        this.logger = logger;
        this.responseCode = responseCode;
        this.headers = headers;
        parseResponse(response);
    }

    public boolean isErroneousResponse() {
        return getResponseCode() >= HTTP_BAD_REQUEST;
    }

    public int getResponseCode() {
        return responseCode;
    }

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

    public long getRetryAfterInMilliseconds() {

        List<String> values = getHeaders().get(RESPONSE_KEY_RETRY_AFTER);
        if (values == null) {
            // the Retry-After response header is missing
            logger.warning(RESPONSE_KEY_RETRY_AFTER + " is not available - using default value " + DEFAULT_RETRY_AFTER_IN_MILLISECONDS);
            return DEFAULT_RETRY_AFTER_IN_MILLISECONDS;
        }

        if (values.size() != 1) {
            // the Retry-After response header has multiple values, but only one is expected
            logger.warning(RESPONSE_KEY_RETRY_AFTER + " has unexpected number of values - using default value " + DEFAULT_RETRY_AFTER_IN_MILLISECONDS);
            return DEFAULT_RETRY_AFTER_IN_MILLISECONDS;
        }

        // according to RFC 7231 Section 7.1.3 (https://tools.ietf.org/html/rfc7231#section-7.1.3)
        // Retry-After value can either be a delay seconds value, which is a non-negative decimal integer
        // or it is an HTTP date.
        // Our implementation assumes only delay seconds value here
        int delaySeconds;
        try {
            delaySeconds = Integer.parseInt(values.get(0));
        } catch (NumberFormatException e) {
            logger.error("Failed to parse " + RESPONSE_KEY_RETRY_AFTER + " value \"" + values.get(0)
                    + "\" - using default value " + DEFAULT_RETRY_AFTER_IN_MILLISECONDS);
            return DEFAULT_RETRY_AFTER_IN_MILLISECONDS;
        }

        // convert delay seconds to milliseconds
        return delaySeconds * 1000L;
    }

    Map<String, List<String>> getHeaders() {
        return headers;
    }

    // parses status check response
    private void parseResponse(String response) {

        if (response == null || response.isEmpty()) {
            return;
        }

        List<KeyValuePair> parsedResponse = parseResponseKeyValuePair(response);
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

     static List<KeyValuePair> parseResponseKeyValuePair(String response) {
        List<KeyValuePair> result = new ArrayList<KeyValuePair>();
        StringTokenizer tokenizer = new StringTokenizer(response, "&");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int keyValueSeparatorIndex = token.indexOf('=');
            if (keyValueSeparatorIndex == -1) {
                throw new IllegalArgumentException("Invalid response; even number of tokens expected.");
            }
            String key = token.substring(0, keyValueSeparatorIndex);
            String value = token.substring(keyValueSeparatorIndex + 1);

            result.add(new KeyValuePair(key, value));
        }

        return result;
    }

    static class KeyValuePair {
        final String key;
        final String value;

        KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
