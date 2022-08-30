/**
 * Copyright 2018-2021 Dynatrace LLC
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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class KeyValueResponseParser {

    // status response constants
    static final String RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB = "bl";
    static final String RESPONSE_KEY_SEND_INTERVAL_IN_SEC = "si";

    static final String RESPONSE_KEY_CAPTURE = "cp";
    static final String RESPONSE_KEY_REPORT_CRASHES = "cr";
    static final String RESPONSE_KEY_REPORT_ERRORS = "er";
    static final String RESPONSE_KEY_TRAFFIC_CONTROL_PERCENTAGE = "tc";

    static final String RESPONSE_KEY_SERVER_ID = "id";
    static final String RESPONSE_KEY_MULTIPLICITY = "mp";

    private KeyValueResponseParser() {
    }

    public static ResponseAttributes parse(String keyValuePairResponse) {
        Map<String, String> keyValuePairs = parseKeyValuePairs(keyValuePairResponse);

        ResponseAttributesImpl.Builder builder = ResponseAttributesImpl.withKeyValueDefaults();

        applyBeaconSizeInKb(builder, keyValuePairs);
        applySendIntervalInSec(builder, keyValuePairs);
        applyCapture(builder, keyValuePairs);
        applyReportCrashes(builder, keyValuePairs);
        applyReportErrors(builder, keyValuePairs);
        applyTrafficControlPercentage(builder, keyValuePairs);
        applyServerId(builder, keyValuePairs);
        applyMultiplicity(builder, keyValuePairs);

        return builder.build();
    }

    private static Map<String, String> parseKeyValuePairs(String response) {
        Map<String, String> resultMap = new HashMap<>();

        StringTokenizer tokenizer = new StringTokenizer(response, "&");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int keyValueSeparatorIndex = token.indexOf('=');
            if (keyValueSeparatorIndex == -1) {
                throw new IllegalArgumentException("Invalid response; even number of tokens expected.");
            }
            String key = token.substring(0, keyValueSeparatorIndex);
            String value = token.substring(keyValueSeparatorIndex + 1);

            resultMap.put(key, value);
        }

        return resultMap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// extract attributes
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void applyBeaconSizeInKb(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB);
        if (value == null) {
            return;
        }

        int beaconSizeInKb = Integer.parseInt(value);
        builder.withMaxBeaconSizeInBytes(beaconSizeInKb * 1024);
    }

    private static void applySendIntervalInSec(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_SEND_INTERVAL_IN_SEC);
        if (value == null) {
            return;
        }

        int sendIntervalInSec = Integer.parseInt(value);
        builder.withSendIntervalInMilliseconds((int) TimeUnit.SECONDS.toMillis(sendIntervalInSec));
    }

    private static void applyCapture(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_CAPTURE);
        if (value == null) {
            return;
        }

        int capture = Integer.parseInt(value);
        builder.withCapture(capture == 1);
    }

    private static void applyReportCrashes(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_REPORT_CRASHES);
        if (value == null) {
            return;
        }

        int reportCrashes = Integer.parseInt(value);
        builder.withCaptureCrashes(reportCrashes != 0);
    }

    private static void applyReportErrors(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_REPORT_ERRORS);
        if (value == null) {
            return;
        }

        int reportErrors = Integer.parseInt(value);
        builder.withCaptureErrors(reportErrors != 0);
    }

    private static void applyTrafficControlPercentage(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_TRAFFIC_CONTROL_PERCENTAGE);
        if (value == null) {
            return;
        }

        int trafficControlPercentage = Integer.parseInt(value);
        builder.withTrafficControlPercentage(trafficControlPercentage);
    }

    private static void applyServerId(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_SERVER_ID);
        if (value == null) {
            return;
        }

        int serverId = Integer.parseInt(value);
        builder.withServerId(serverId);
    }

    private static void applyMultiplicity(ResponseAttributesImpl.Builder builder, Map<String, String> keyValuePairs) {
        String value = keyValuePairs.get(RESPONSE_KEY_MULTIPLICITY);
        if (value == null) {
            return;
        }

        int multiplicity = Integer.parseInt(value);
        builder.withMultiplicity(multiplicity);
    }
}
