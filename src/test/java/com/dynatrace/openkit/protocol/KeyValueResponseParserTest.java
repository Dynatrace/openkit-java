/**
 * Copyright 2018-2019 Dynatrace LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dynatrace.openkit.protocol;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class KeyValueResponseParserTest {

    private StringBuilder inputBuilder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        inputBuilder = new StringBuilder("type=m");
    }

    @Test
    public void parsingAnEmptyStringReturnsResponseWithDefaultValues() {
        // given
        ResponseDefaults defaults = ResponseDefaults.KEY_VALUE_RESPONSE;
        String input = "";

        // when
        Response obtained = KeyValueResponseParser.parse(input);

        // then
        assertThat(obtained, notNullValue());

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(defaults.getBeaconSizeInBytes()));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getSessionDurationInMilliseconds()));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getEventsPerSession()));
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
    public void parsingKeyWithoutValueDelimeterThrowsAnException() {
        // given
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid response; even number of tokens expected.");

        inputBuilder.append("&param");

        // when
        KeyValueResponseParser.parse(inputBuilder.toString());
    }

    @Test
    public void parseExtractsBeaconSize() {
        // given
        int beaconSize = 37;
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB, beaconSize);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize * 1024));
    }

    @Test
    public void parseExtractsSendInterval() {
        // given
        int sendInterval = 37;
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_SEND_INTERVAL_IN_SEC, sendInterval);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is((int)TimeUnit.SECONDS.toMillis(sendInterval)));
    }

    @Test
    public void parseExtractsCaptureEnabled() {
        // given
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_CAPTURE, 1);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(true));
    }

    @Test
    public void parseExtractsCaptureDisabled() {
        // given
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_CAPTURE, 0);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(false));
    }

    @Test
    public void parseExtractsCaptureCrashesEnabled() {
        // given
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_REPORT_CRASHES, 1);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(true));
    }

    @Test
    public void parseExtractsCaptureCrashesDisabled() {
        // given
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_REPORT_CRASHES, 0);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(false));
    }

    @Test
    public void parseExtractsCaptureErrorsEnabled() {
        // given
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_REPORT_ERRORS, 1);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(true));
    }

    @Test
    public void parseExtractsCaptureErrorsDisabled() {
        // given
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_REPORT_ERRORS, 0);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(false));
    }

    @Test
    public void parseExtractsServerId() {
        // given
        int serverId = 73;
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_SERVER_ID, serverId);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void parseExtractsMultiplicity() {
        // given
        int multiplicity = 73;
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_MULTIPLICITY, multiplicity);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void parseResponseWithAllParametersSet() {
        // given
        ResponseDefaults defaults = ResponseDefaults.KEY_VALUE_RESPONSE;
        int beaconSize = 73;
        int sendInterval = 74;
        int serverId = 75;
        int multiplicity = 76;

        appendParameter(KeyValueResponseParser.RESPONSE_KEY_MAX_BEACON_SIZE_IN_KB, beaconSize);
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_SEND_INTERVAL_IN_SEC, sendInterval);
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_CAPTURE, 0);
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_REPORT_CRASHES, 1);
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_REPORT_ERRORS, 0);
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_SERVER_ID, serverId);
        appendParameter(KeyValueResponseParser.RESPONSE_KEY_MULTIPLICITY, multiplicity);

        // when
        Response obtained = KeyValueResponseParser.parse(inputBuilder.toString());

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize * 1024));
        assertThat(obtained.isAttributeSet(ResponseAttribute.MAX_BEACON_SIZE), is(true));

        assertThat(obtained.getSendIntervalInMilliseconds(), is((int)TimeUnit.SECONDS.toMillis(sendInterval)));
        assertThat(obtained.isAttributeSet(ResponseAttribute.SEND_INTERVAL), is(true));

        assertThat(obtained.isCapture(), is(false));
        assertThat(obtained.isAttributeSet(ResponseAttribute.IS_CAPTURE), is(true));

        assertThat(obtained.isCaptureCrashes(), is(true));
        assertThat(obtained.isAttributeSet(ResponseAttribute.IS_CAPTURE_CRASHES), is(true));

        assertThat(obtained.isCaptureErrors(), is(false));
        assertThat(obtained.isAttributeSet(ResponseAttribute.IS_CAPTURE_ERRORS), is(true));

        assertThat(obtained.getMultiplicity(), is(multiplicity));
        assertThat(obtained.isAttributeSet(ResponseAttribute.MULTIPLICITY), is(true));

        assertThat(obtained.getServerId(), is(serverId));
        assertThat(obtained.isAttributeSet(ResponseAttribute.SERVER_ID), is(true));

        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getSessionDurationInMilliseconds()));
        assertThat(obtained.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION), is(false));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getEventsPerSession()));
        assertThat(obtained.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION), is(false));
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(defaults.getSessionTimeoutInMilliseconds()));
        assertThat(obtained.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT), is(false));
        assertThat(obtained.getVisitStoreVersion(), is(defaults.getVisitStoreVersion()));
        assertThat(obtained.isAttributeSet(ResponseAttribute.VISIT_STORE_VERSION), is(false));
        assertThat(obtained.getTimestampInMilliseconds(), is(defaults.getTimestampInMilliseconds()));
        assertThat(obtained.isAttributeSet(ResponseAttribute.TIMESTAMP), is(false));
    }

    private void appendParameter(String key, int value) {
        appendParameter(key, String.valueOf(value));
    }

    private void appendParameter(String key, String value) {
        inputBuilder.append("&").append(key).append("=").append(value);
    }
}
