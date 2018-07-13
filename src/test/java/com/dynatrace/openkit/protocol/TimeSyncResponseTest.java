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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class TimeSyncResponseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void passingNullResponseStringDoesNotThrow() {
        // then
        new TimeSyncResponse(null, 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void theDefaultRequestReceiveTimeIsMinusOne() {
        // given
        TimeSyncResponse target = new TimeSyncResponse("", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getRequestReceiveTime(), is(equalTo(-1L)));
    }

    @Test
    public void theDefaultResponseSendTimeIsMinusOne() {
        // given
        TimeSyncResponse target = new TimeSyncResponse("", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getResponseSendTime(), is(equalTo(-1L)));
    }

    @Test
    public void oddNumberOfTokensThrowsException() {
        // given
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid response; even number of tokens expected.");

        String responseString = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=100" + "&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME;

        // when, then
        new StatusResponse(responseString, 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void ampersandIsNotAValidKeyValueSeparator() {
        // given
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid response; even number of tokens expected.");

        String responseString = TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "&100";

        // when, then
        new StatusResponse(responseString, 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void responseSendTimeIsParsed() {
        // given
        TimeSyncResponse target = new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=100", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getResponseSendTime(), is(equalTo(100L)));
    }

    @Test
    public void parsingEmptyResponseSendTimeValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericResponseSendTimeValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigResponseSendTimeValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^63, then
        new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=9223372036854775808", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void requestReceiveTimeIsParsed() {
        // given
        TimeSyncResponse target = new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=100", 200, Collections.<String, List<String>>emptyMap());

        // then
        assertThat(target.getRequestReceiveTime(), is(equalTo(100L)));
    }

    @Test
    public void parsingEmptyRequestReceiveTimeValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingNonNumericRequestReceiveTimeValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when, then
        new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=a", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void parsingTooBigRequestReceiveTimeValueThrowsException() {
        // given
        expectedException.expect(NumberFormatException.class);

        // when parsing 2^63, then
        new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=9223372036854775808", 200, Collections.<String, List<String>>emptyMap());
    }

    @Test
    public void responseCodeIsSet() {
        // given
        assertThat(new StatusResponse("key=value", 418, Collections.<String, List<String>>emptyMap()).getResponseCode(), is(equalTo(418)));
    }

    @Test
    public void headersAreSet() {
        // given
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("X-Foo", Collections.singletonList("X-BAR"));
        headers.put("X-YZ", Collections.<String>emptyList());

        // then
        assertThat(new StatusResponse("", 418, headers).getHeaders(),
            is(sameInstance(headers)));
    }
}
