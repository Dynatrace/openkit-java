/**
 * Copyright 2018-2019 Dynatrace LLC
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


import com.dynatrace.openkit.util.json.JSONParser;
import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONObjectValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import com.dynatrace.openkit.util.json.parser.ParserException;

import java.util.concurrent.TimeUnit;

public class JsonResponseParser {

    static final String RESPONSE_KEY_AGENT_CONFIG = "mobileAgentConfig";
    static final String RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB = "maxBeaconSizeKb";
    static final String RESPONSE_KEY_MAX_SESSION_DURATION_IN_MIN = "maxSessionDurationMins";
    static final String RESPONSE_KEY_MAX_EVENTS_PER_SESSION = "maxEventsPerSession";
    static final String RESPONSE_KEY_SESSION_TIMEOUT_IN_SEC = "sessionTimeoutSec";
    static final String RESPONSE_KEY_SEND_INTERVAL_IN_SEC = "sendIntervalSec";
    static final String RESPONSE_KEY_VISIT_STORE_VERSION = "visitStoreVersion";

    static final String RESPONSE_KEY_APP_CONFIG = "appConfig";
    static final String RESPONSE_KEY_CAPTURE = "capture";
    static final String RESPONSE_KEY_REPORT_CRASHES = "reportCrashes";
    static final String RESPONSE_KEY_REPORT_ERRORS = "reportErrors";
    static final String RESPONSE_KEY_APPLICATION_ID = "applicationId";

    static final String RESPONSE_KEY_DYNAMIC_CONFIG = "dynamicConfig";
    static final String RESPONSE_KEY_MULTIPLICITY = "multiplicity";
    static final String RESPONSE_KEY_SERVER_ID = "serverId";

    static final String RESPONSE_KEY_TIMESTAMP_IN_MILLIS = "timestamp";

    private JsonResponseParser() {
    }

    public static ResponseAttributes parse(String jsonResponse) throws ParserException {
        JSONParser parser = new JSONParser(jsonResponse);

        JSONValue parsedValue = parser.parse();

        JSONObjectValue rootObject = (JSONObjectValue) parsedValue;

        ResponseAttributesImpl.Builder builder = ResponseAttributesImpl.withJsonDefaults();
        applyAgentConfiguration(builder, rootObject);
        applyApplicationConfiguration(builder, rootObject);
        applyDynamicConfiguration(builder, rootObject);
        applyRootAttributes(builder, rootObject);

        return builder.build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Agent configuration
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void applyAgentConfiguration(ResponseAttributesImpl.Builder builder, JSONObjectValue rootObject) {
        JSONValue agentConfigValue = rootObject.get(RESPONSE_KEY_AGENT_CONFIG);
        if (agentConfigValue == null) {
            return;
        }

        JSONObjectValue agentConfigObject = (JSONObjectValue) agentConfigValue;
        applyBeaconSizeInKb(builder, agentConfigObject);
        applyMaxSessionDurationInMin(builder, agentConfigObject);
        applyMaxEventsPerSession(builder, agentConfigObject);
        applySessionTimeoutInSec(builder, agentConfigObject);
        applySendIntervalInSec(builder, agentConfigObject);
        applyVisitStoreVersion(builder, agentConfigObject);
    }

    private static void applyBeaconSizeInKb(ResponseAttributesImpl.Builder builder, JSONObjectValue agentConfigObject) {
        JSONValue value = agentConfigObject.get(RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int beaconSizeInKb = numberValue.getIntValue();
        builder.withMaxBeaconSizeInBytes(beaconSizeInKb * 1024);
    }

    private static void applyMaxSessionDurationInMin(ResponseAttributesImpl.Builder builder, JSONObjectValue agentConfigObject) {
        JSONValue value = agentConfigObject.get(RESPONSE_KEY_MAX_SESSION_DURATION_IN_MIN);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int sessionDurationInMin = numberValue.getIntValue();
        builder.withMaxSessionDurationInMilliseconds((int) TimeUnit.MINUTES.toMillis(sessionDurationInMin));
    }

    private static void applyMaxEventsPerSession(ResponseAttributesImpl.Builder builder, JSONObjectValue agentConfigObject) {
        JSONValue value = agentConfigObject.get(RESPONSE_KEY_MAX_EVENTS_PER_SESSION);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int eventsPerSession = numberValue.getIntValue();
        builder.withMaxEventsPerSession(eventsPerSession);
    }

    private static void applySessionTimeoutInSec(ResponseAttributesImpl.Builder builder, JSONObjectValue agentConfigObject) {
        JSONValue value = agentConfigObject.get(RESPONSE_KEY_SESSION_TIMEOUT_IN_SEC);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int sessionTimeoutInSec = numberValue.getIntValue();
        builder.withSessionTimeoutInMilliseconds((int) TimeUnit.SECONDS.toMillis(sessionTimeoutInSec));
    }

    private static void applySendIntervalInSec(ResponseAttributesImpl.Builder builder, JSONObjectValue agentConfigObject) {
        JSONValue value = agentConfigObject.get(RESPONSE_KEY_SEND_INTERVAL_IN_SEC);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int sendIntervalInSec = numberValue.getIntValue();
        builder.withSendIntervalInMilliseconds((int) TimeUnit.SECONDS.toMillis(sendIntervalInSec));
    }

    private static void applyVisitStoreVersion(ResponseAttributesImpl.Builder builder, JSONObjectValue agentConfigObject) {
        JSONValue value = agentConfigObject.get(RESPONSE_KEY_VISIT_STORE_VERSION);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int visitStoreVersion = numberValue.getIntValue();
        builder.withVisitStoreVersion(visitStoreVersion);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Application configuration
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void applyApplicationConfiguration(ResponseAttributesImpl.Builder builder, JSONObjectValue rootObject) {
        JSONValue appConfigValue = rootObject.get(RESPONSE_KEY_APP_CONFIG);
        if (appConfigValue == null) {
            return;
        }

        JSONObjectValue appConfigObject = (JSONObjectValue) appConfigValue;
        applyCapture(builder, appConfigObject);
        applyReportCrashes(builder, appConfigObject);
        applyReportErrors(builder, appConfigObject);
        applyApplicationId(builder, appConfigObject);
    }

    private static void applyCapture(ResponseAttributesImpl.Builder builder, JSONObjectValue appConfigObject) {
        JSONValue value = appConfigObject.get(RESPONSE_KEY_CAPTURE);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int capture = numberValue.getIntValue();
        builder.withCapture(capture == 1);
    }

    private static void applyReportCrashes(ResponseAttributesImpl.Builder builder, JSONObjectValue appConfigObject) {
        JSONValue value = appConfigObject.get(RESPONSE_KEY_REPORT_CRASHES);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int reportCrashes = numberValue.getIntValue();
        builder.withCaptureCrashes(reportCrashes != 0);
    }

    private static void applyReportErrors(ResponseAttributesImpl.Builder builder, JSONObjectValue appConfigObject) {
        JSONValue value = appConfigObject.get(RESPONSE_KEY_REPORT_ERRORS);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int reportErrors = numberValue.getIntValue();
        builder.withCaptureErrors(reportErrors != 0);
    }

    private static void applyApplicationId(ResponseAttributesImpl.Builder builder, JSONObjectValue appConfigObject) {
        JSONValue value = appConfigObject.get(RESPONSE_KEY_APPLICATION_ID);
        if (value == null) {
            return;
        }

        JSONStringValue stringValue = (JSONStringValue) value;
        builder.withApplicationId(stringValue.getValue());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Dynamic configuration
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void applyDynamicConfiguration(ResponseAttributesImpl.Builder builder, JSONObjectValue rootObject) {
        JSONValue dynConfigValue = rootObject.get(RESPONSE_KEY_DYNAMIC_CONFIG);
        if (dynConfigValue == null) {
            return;
        }

        JSONObjectValue dynConfigObject = (JSONObjectValue) dynConfigValue;
        applyMultiplicity(builder, dynConfigObject);
        applyServerId(builder, dynConfigObject);
    }

    private static void applyMultiplicity(ResponseAttributesImpl.Builder builder, JSONObjectValue dynConfigObject) {
        JSONValue value = dynConfigObject.get(RESPONSE_KEY_MULTIPLICITY);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int multiplicity = numberValue.getIntValue();
        builder.withMultiplicity(multiplicity);
    }

    private static void applyServerId(ResponseAttributesImpl.Builder builder, JSONObjectValue dynConfigObject) {
        JSONValue value = dynConfigObject.get(RESPONSE_KEY_SERVER_ID);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        int serverId = numberValue.getIntValue();
        builder.withServerId(serverId);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Root attributes
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void applyRootAttributes(ResponseAttributesImpl.Builder builder, JSONObjectValue rootObject) {
        JSONValue value = rootObject.get(RESPONSE_KEY_TIMESTAMP_IN_MILLIS);
        if (value == null) {
            return;
        }

        JSONNumberValue numberValue = (JSONNumberValue) value;
        long timestampInMillis = numberValue.getLongValue();
        builder.withTimestampInMilliseconds(timestampInMillis);
    }
}
