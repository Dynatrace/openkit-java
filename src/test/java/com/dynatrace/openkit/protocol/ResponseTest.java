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
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ResponseTest {

    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
    }

    @Test
    public void isErroneousResponseGivesTrueForErrorCodeEqualTo400() {
        // when parsing 2^31, then
        assertThat(new TestResponse(mockLogger, 400, Collections.<String, List<String>>emptyMap()).isErroneousResponse(),
            is(true));
    }

    @Test
    public void isErroneousResponseGivesTrueForErrorCodeGreaterThan400() {
        // when parsing 2^31, then
        assertThat(new TestResponse(mockLogger, 401, Collections.<String, List<String>>emptyMap()).isErroneousResponse(),
            is(true));
    }

    @Test
    public void isErroneousResponseGivesFalseForErrorCodeLessThan400() {
        // when parsing 2^31, then
        assertThat(new TestResponse(mockLogger, 399, Collections.<String, List<String>>emptyMap()).isErroneousResponse(),
            is(false));
    }

    @Test
    public void responseCodeIsSet() {
        // given
        assertThat(new TestResponse(mockLogger, 418, Collections.<String, List<String>>emptyMap()).getResponseCode(), is(equalTo(418)));
    }

    @Test
    public void headersAreSet() {
        // given
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("X-Foo", Collections.singletonList("X-BAR"));
        headers.put("X-YZ", Collections.<String>emptyList());

        // then
        assertThat(new TestResponse(mockLogger, 418, headers).getHeaders(),
            is(sameInstance(headers)));
    }

    @Test
    public void getRetryAfterReturnsDefaultValueIfResponseKeyDoesNotExist() {

        // given
        Response target = new TestResponse(mockLogger, 429, Collections.<String, List<String>>emptyMap());

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(Response.DEFAULT_RETRY_AFTER_IN_MILLISECONDS)));
    }

    @Test
    public void getRetryAfterReturnsDefaultValueIfMultipleValuesWereRetrieved() {

        // given
        Map<String, List<String>> responseHeaders = Collections.singletonMap(Response.RESPONSE_KEY_RETRY_AFTER, Arrays.asList("100", "200"));
        Response target = new TestResponse(mockLogger, 429, responseHeaders);

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(Response.DEFAULT_RETRY_AFTER_IN_MILLISECONDS)));
    }

    @Test
    public void getRetryAfterReturnsDefaultValueIfValueIsNotParsableAsInteger() {

        // given
        Map<String, List<String>> responseHeaders = Collections.singletonMap(Response.RESPONSE_KEY_RETRY_AFTER, Collections.singletonList("a"));
        Response target = new TestResponse(mockLogger, 429, responseHeaders);

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(Response.DEFAULT_RETRY_AFTER_IN_MILLISECONDS)));
    }

    @Test
    public void getRetryAfterReturnsParsedValue() {

        // given
        Map<String, List<String>> responseHeaders = Collections.singletonMap(Response.RESPONSE_KEY_RETRY_AFTER, Collections.singletonList("1234"));
        Response target = new TestResponse(mockLogger, 429, responseHeaders);

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(1234L * 1000L)));
    }

    @Test
    public void parseResponseKeyValuePairReturnsEmptyListIfInputIsEmptyString() {

        // when
        List<Response.KeyValuePair> obtained = Response.parseResponseKeyValuePair("");

        // then
        assertThat(obtained, is(empty()));
    }

    @Test
    public void parseResponseKeyValuePairForOneKeyValuePair() {

        // when
        List<Response.KeyValuePair> obtained = Response.parseResponseKeyValuePair("key=value");

        // then
        assertThat(obtained, is(not(empty())));
        assertThat(obtained.size(), is(equalTo(1)));
        assertThat(obtained.get(0).key, is(equalTo("key")));
        assertThat(obtained.get(0).value, is(equalTo("value")));
    }

    @Test
    public void parseResponseKeyValuePairForMultipleKeyValuePair() {

        // when
        List<Response.KeyValuePair> obtained = Response.parseResponseKeyValuePair("key=value");

        // then
        assertThat(obtained, is(not(empty())));
        assertThat(obtained.size(), is(equalTo(1)));
        assertThat(obtained.get(0).key, is(equalTo("key")));
        assertThat(obtained.get(0).value, is(equalTo("value")));
    }

    /**
     * Test response class extending the abstract base class.
     */
    private static final class TestResponse extends Response {

        TestResponse(Logger logger, int responseCode, Map<String, List<String>> headers) {
            super(logger, responseCode, headers);
        }
    }
}
