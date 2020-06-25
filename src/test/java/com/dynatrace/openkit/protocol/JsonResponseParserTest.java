/**
 * Copyright 2018-2020 Dynatrace LLC
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

import com.dynatrace.openkit.util.json.parser.ParserException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonResponseParserTest {

    private StringBuilder inputBuilder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        inputBuilder = new StringBuilder();
        inputBuilder.append("{");
    }

    @Test
    public void parsingAnEmptyStringThrowsException() throws ParserException {
        // given
        String input = "";
        expectedException.expect(ParserException.class);

        // when
        JsonResponseParser.parse(input);
    }

    @Test
    public void parsingAnEmptyObjectReturnsInstanceWithDefaultValues() throws ParserException {
        // given
        ResponseAttributesDefaults defaults = ResponseAttributesDefaults.JSON_RESPONSE;
        String input = "{}";

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(input);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(defaults.getMaxBeaconSizeInBytes()));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getMaxSessionDurationInMilliseconds()));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getMaxEventsPerSession()));
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(defaults.getSessionTimeoutInMilliseconds()));
        assertThat(obtained.getSendIntervalInMilliseconds(), is(defaults.getSendIntervalInMilliseconds()));
        assertThat(obtained.getVisitStoreVersion(), is(defaults.getVisitStoreVersion()));
        assertThat(obtained.isCapture(), is(defaults.isCapture()));
        assertThat(obtained.isCaptureCrashes(), is(defaults.isCaptureCrashes()));
        assertThat(obtained.isCaptureErrors(), is(defaults.isCaptureErrors()));
        assertThat(obtained.getMultiplicity(), is(defaults.getMultiplicity()));
        assertThat(obtained.getServerId(), is(defaults.getServerId()));
        assertThat(obtained.getTimestampInMilliseconds(), is(defaults.getTimestampInMilliseconds()));
    }

    @Test
    public void parseExtractsMaxBeaconSize() throws ParserException {
        // given
        int beaconSize = 73;
        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB, beaconSize);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize * 1024));
    }

    @Test
    public void parseExtractsMaxSessionDuration() throws ParserException {
        // given
        int sessionDuration = 73;
        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_MAX_SESSION_DURATION_IN_MIN, sessionDuration);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is((int) TimeUnit.MINUTES.toMillis(sessionDuration)));
    }

    @Test
    public void parseExtractsMaxEventsPerSession() throws ParserException {
        // given
        int eventsPerSession = 73;
        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_MAX_EVENTS_PER_SESSION, eventsPerSession);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void parseExtractsSessionTimeout() throws ParserException {
        // given
        int sessionTimeout = 73;
        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_SESSION_TIMEOUT_IN_SEC, sessionTimeout);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is((int) TimeUnit.SECONDS.toMillis(sessionTimeout)));
    }

    @Test
    public void parseExtractsSendInterval() throws ParserException {
        // given
        int sendInterval = 73;
        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_SEND_INTERVAL_IN_SEC, sendInterval);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is((int) TimeUnit.SECONDS.toMillis(sendInterval)));
    }

    @Test
    public void parseExtractsVisitStoreVersion() throws ParserException {
        // given
        int visitStoreVersion = 73;
        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_VISIT_STORE_VERSION, visitStoreVersion);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void parseExtractsCaptureEnabled() throws ParserException {
        // given
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_CAPTURE, 1);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(true));
    }

    @Test
    public void parseExtractsCaptureDisabled() throws ParserException {
        // given
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_CAPTURE, 0);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(false));
    }

    @Test
    public void parseExtractsReportCrashesEnabled() throws ParserException {
        // given
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_REPORT_CRASHES, 1);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(true));
    }

    @Test
    public void parseExtractsReportCrashesDisabled() throws ParserException {
        // given
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_REPORT_CRASHES, 0);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(false));
    }

    @Test
    public void parseExtractsReportErrorsEnabled() throws ParserException {
        // given
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_REPORT_ERRORS, 1);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(true));
    }

    @Test
    public void parseExtractsReportErrorsDisabled() throws ParserException {
        // given
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_REPORT_ERRORS, 0);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(false));
    }

    @Test
    public void parseExtractsApplicationId() throws ParserException {
        // given
        String applicationId = UUID.randomUUID().toString();
        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_APPLICATION_ID, applicationId);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getApplicationId(), is(equalTo(applicationId)));
    }

    @Test
    public void parseExtractsMultiplicity() throws ParserException {
        // given
        int multiplicity = 73;
        begin(JsonResponseParser.RESPONSE_KEY_DYNAMIC_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_MULTIPLICITY, multiplicity);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void parseExtractsServerId() throws ParserException {
        // given
        int serverId = 73;
        begin(JsonResponseParser.RESPONSE_KEY_DYNAMIC_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_SERVER_ID, serverId);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void parseExtractsStatus() throws ParserException {
        // given
        String status = "foobar";
        begin(JsonResponseParser.RESPONSE_KEY_DYNAMIC_CONFIG);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_STATUS, status);
        close(2);

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getStatus(), is(equalTo(status)));
    }

    @Test
    public void parseExtractsTimestamp() throws ParserException {
        // given
        long timestamp = 73;
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_TIMESTAMP_IN_MILLIS, timestamp);
        close();

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void parseResponseWithAllValuesSet() throws ParserException {
        // given
        int beaconSize = 73;
        int sessionDuration = 74;
        int eventsPerSession = 75;
        int sessionTimeout = 76;
        int sendInterval = 77;
        int visitStoreVersion = 78;
        String applicationId = UUID.randomUUID().toString();
        int multiplicity = 79;
        int serverId = 80;
        String status = "some status";
        long timestamp = 81;

        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendParameter(JsonResponseParser.RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB, beaconSize);
        appendParameter(JsonResponseParser.RESPONSE_KEY_MAX_SESSION_DURATION_IN_MIN, sessionDuration);
        appendParameter(JsonResponseParser.RESPONSE_KEY_MAX_EVENTS_PER_SESSION, eventsPerSession);
        appendParameter(JsonResponseParser.RESPONSE_KEY_SESSION_TIMEOUT_IN_SEC, sessionTimeout);
        appendParameter(JsonResponseParser.RESPONSE_KEY_SEND_INTERVAL_IN_SEC, sendInterval);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_VISIT_STORE_VERSION, visitStoreVersion);
        close();
        inputBuilder.append(",");

        begin(JsonResponseParser.RESPONSE_KEY_APP_CONFIG);
        appendParameter(JsonResponseParser.RESPONSE_KEY_CAPTURE, 0);
        appendParameter(JsonResponseParser.RESPONSE_KEY_REPORT_CRASHES, 1);
        appendParameter(JsonResponseParser.RESPONSE_KEY_REPORT_ERRORS, 0);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_APPLICATION_ID, applicationId);
        close();
        inputBuilder.append(",");

        begin(JsonResponseParser.RESPONSE_KEY_DYNAMIC_CONFIG);
        appendParameter(JsonResponseParser.RESPONSE_KEY_MULTIPLICITY, multiplicity);
        appendParameter(JsonResponseParser.RESPONSE_KEY_SERVER_ID, serverId);
        appendLastParameter(JsonResponseParser.RESPONSE_KEY_STATUS, status);
        close();
        inputBuilder.append(",");

        appendLastParameter(JsonResponseParser.RESPONSE_KEY_TIMESTAMP_IN_MILLIS, timestamp);
        close();

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize * 1024));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is((int) TimeUnit.MINUTES.toMillis(sessionDuration)));
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is((int) TimeUnit.SECONDS.toMillis(sessionTimeout)));
        assertThat(obtained.getSendIntervalInMilliseconds(), is((int) TimeUnit.SECONDS.toMillis(sendInterval)));
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));

        assertThat(obtained.isCapture(), is(false));
        assertThat(obtained.isCaptureCrashes(), is(true));
        assertThat(obtained.isCaptureErrors(), is(false));
        assertThat(obtained.getApplicationId(), is(equalTo(applicationId)));

        assertThat(obtained.getMultiplicity(), is(multiplicity));
        assertThat(obtained.getServerId(), is(serverId));
        assertThat(obtained.getStatus(), is(equalTo(status)));

        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));

        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(obtained.isAttributeSet(attribute), is(true));
        }
    }

    @Test
    public void parseIgnoresUnknownTokens() throws ParserException {
        // given
        begin("unknownObject");
        close();
        inputBuilder.append(",");

        begin(JsonResponseParser.RESPONSE_KEY_AGENT_CONFIG);
        appendParameter(JsonResponseParser.RESPONSE_KEY_MAX_EVENTS_PER_SESSION, 999);
        appendLastParameter("unknownAttribute", 777);
        close();
        inputBuilder.append(",");

        appendLastParameter("anotherUnknownAttribute", 666);
        close();

        // when
        ResponseAttributes obtained = JsonResponseParser.parse(inputBuilder.toString());

        // then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            if (attribute == ResponseAttribute.MAX_EVENTS_PER_SESSION) {
                continue;
            }
            assertThat(obtained.isAttributeSet(attribute), is(false));
        }
    }

    private void begin(String objectName) {
        inputBuilder.append("\"").append(objectName).append("\": {");
    }

    private void appendLastParameter(String key, long value) {
        inputBuilder.append("\"").append(key).append("\":").append(value);
    }

    private void appendLastParameter(String key, String value) {
        inputBuilder.append("\"").append(key).append("\":\"").append(value).append("\"");
    }

    private void appendParameter(String key, long value) {
        appendLastParameter(key, value);
        inputBuilder.append(",");
    }

    private void close() {
        close(1);
    }

    private void close(int numClosingBrackets) {
        for (int i = 0; i < numClosingBrackets; i++) {
            inputBuilder.append("}");
        }
    }
}
