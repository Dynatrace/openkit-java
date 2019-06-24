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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class StatusResponseTest {

    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
    }


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void passingNullResponseStringDoesNotThrow() {
        // then
        new StatusResponse(mockLogger, null, 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void defaultCaptureIsOn() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.isCapture(), is(true));
    }

    @Test
    public void defaultSendIntervalIsMinusOne() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getSendInterval(), is(equalTo(-1)));
    }

    @Test
    public void defaultMonitorNameIsNull() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getMonitorName(), is(nullValue()));
    }

    @Test
    public void defaultServerIDIsMinusOne() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getServerID(), is(equalTo(-1)));
    }

    @Test
    public void defaultMaxBeaconSizeIsMinusOne() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getMaxBeaconSize(), is(equalTo(-1)));
    }

    @Test
    public void defaultCaptureCrashesIsOn() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.isCaptureCrashes(), is(true));
    }

    @Test
    public void defaultCaptureErrorsIsOn() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.isCaptureErrors(), is(true));
    }

    @Test
    public void defaultMultiplicityIsOne() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, "", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getMultiplicity(), is(equalTo(1)));
    }

    @Test
    public void oddNumberOfTokensThrowsException() {
        // given
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid response; even number of tokens expected.");

        String responseString = StatusResponse.RESPONSE_KEY_CAPTURE + "=100" + "&" + StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES;

        // when, then
        new StatusResponse(mockLogger, responseString, 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void ampersandIsNotAValidKeyValueSeparator() {
        // given
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid response; even number of tokens expected.");

        String responseString = StatusResponse.RESPONSE_KEY_CAPTURE + "&100";

        // when, then
        new StatusResponse(mockLogger, responseString, 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void captureIsTrueWhenItIsEqualToOne() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=1", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.isCapture(), is(true));
    }

    @Test
    public void captureIsFalseWhenItIsNotEqualToOne() {
        // when it's a positive number greater than 1, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=2", 200, Collections.<String, List<String>>emptyMap()).isCapture(),
            is(false));

        // and when it's zero, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=0", 200, Collections.<String, List<String>>emptyMap()).isCapture(),
            is(false));

        // and when it's a negative number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=-2", 200, Collections.<String, List<String>>emptyMap()).isCapture(),
            is(false));
    }

    @Test
    public void parsingEmptyCaptureValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericCaptureValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigCaptureValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingSendInterval() {
        // when it's a positive number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=1", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(1000)));
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=1200", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(1200000)));

        // and when it's zero, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=0", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(0)));

        // and when it's a negative number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=-1", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(-1000)));
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=-42", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(-42000)));
    }

    @Test
    public void parsingTooBigSendIntervalOverflows() {
        // when the value is positive, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=2147484", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(-2147483296)));

        // when the value is negative, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=-2147485", 200, Collections.<String, List<String>>emptyMap()).getSendInterval(),
            is(equalTo(2147482296)));
    }

    @Test
    public void parsingEmptySendIntervalValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericSendIntervalValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigSendIntervalValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SEND_INTERVAL + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingMonitorNames() {
        // when it's a positive number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MONITOR_NAME + "=", 200, Collections.<String, List<String>>emptyMap()).getMonitorName(),
            isEmptyString());
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MONITOR_NAME + "=foobar", 200, Collections.<String, List<String>>emptyMap()).getMonitorName(),
            is(equalTo("foobar")));
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MONITOR_NAME + "=1234", 200, Collections.<String, List<String>>emptyMap()).getMonitorName(),
            is(equalTo("1234")));
    }

    @Test
    public void serverIDIsParsed() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SERVER_ID + "=1234", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getServerID(), is(equalTo(1234)));
    }

    @Test
    public void parsingEmptyServerIDValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SERVER_ID + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericServerIDValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SERVER_ID + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigServerIDValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_SERVER_ID + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingMaxBeaconSize() {
        // when it's a positive number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=1", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(1024)));
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=1200", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(1200 * 1024)));

        // and when it's zero, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=0", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(0)));

        // and when it's a negative number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=-1", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(-1 * 1024)));
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=-42", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(-42 * 1024)));
    }

    @Test
    public void parsingTooBigMaxBeaconSizeOverflows() {
        // when the value is positive, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=2097152", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(-2147483648)));

        // when the value is negative, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=-2097153", 200, Collections.<String, List<String>>emptyMap()).getMaxBeaconSize(),
            is(equalTo(2147482624)));
    }

    @Test
    public void parsingEmptyMaxBeaconSizeThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericMaxBeaconSizeThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigMaxBeaconSizeThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MAX_BEACON_SIZE + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void captureErrorsIsTrueWhenItIsNotEqualToZero() {
        // when it's a positive number greater than 1, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=2", 200, Collections.<String, List<String>>emptyMap()).isCaptureErrors(),
            is(true));

        // when it's one, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=1", 200, Collections.<String, List<String>>emptyMap()).isCaptureErrors(),
            is(true));

        // and when it's a negative number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=-2", 200, Collections.<String, List<String>>emptyMap()).isCaptureErrors(),
            is(true));
    }

    @Test
    public void captureErrorsIsFalseWhenItIsEqualToZero() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=0", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.isCaptureErrors(), is(false));
    }

    @Test
    public void parsingEmptyCaptureErrorsValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericCaptureErrorsValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigCaptureErrorsValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_ERRORS + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void captureCrashesIsTrueWhenItIsNotEqualToZero() {
        // when it's a positive number greater than 1, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=2", 200, Collections.<String, List<String>>emptyMap()).isCaptureCrashes(),
            is(true));

        // when it's one, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=1", 200, Collections.<String, List<String>>emptyMap()).isCaptureCrashes(),
            is(true));

        // and when it's a negative number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=-2", 200, Collections.<String, List<String>>emptyMap()).isCaptureCrashes(),
            is(true));
    }

    @Test
    public void captureCrashesIsFalseWhenItIsEqualToZero() {
        // given
        StatusResponse target = new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=0", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.isCaptureCrashes(), is(false));
    }

    @Test
    public void parsingEmptyCaptureCrashesValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericCaptureCrashesValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigCaptureCrashesValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_CAPTURE_CRASHES + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingMultiplicity() {
        // when it's a positive number greater than 1, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MULTIPLICITY + "=3", 200, Collections.<String, List<String>>emptyMap()).getMultiplicity(),
            is(equalTo(3)));

        // and when it's zero, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MULTIPLICITY + "=0", 200, Collections.<String, List<String>>emptyMap()).getMultiplicity(),
            is(equalTo(0)));

        // and when it's a negative number, then
        assertThat(new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MULTIPLICITY + "=-5", 200, Collections.<String, List<String>>emptyMap()).getMultiplicity(),
            is(equalTo(-5)));
    }

    @Test
    public void parsingEmptyMultiplicityValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MULTIPLICITY + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericMultiplicityValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MULTIPLICITY + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigMultiplicityValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^31, then
        new StatusResponse(mockLogger, StatusResponse.RESPONSE_KEY_MULTIPLICITY + "=2147483648", 200, Collections.<String, List<String>>emptyMap());
    }
}
