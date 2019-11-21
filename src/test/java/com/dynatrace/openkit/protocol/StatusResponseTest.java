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

import com.dynatrace.openkit.api.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class StatusResponseTest {

    private Logger mockLogger;
    private ResponseAttributes attributes;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        attributes = ResponseAttributesImpl.withJsonDefaults().build();
    }


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void isErroneousResponseGivesTrueForErrorCodeEqualTo400() {
        // when, then
        assertThat(StatusResponse.createErrorResponse(mockLogger, 400).isErroneousResponse(),
                is(true));
    }

    @Test
    public void isErroneousResponseGivesTrueForErrorCodeGreaterThan400() {
        // when, then
        assertThat(StatusResponse.createErrorResponse(mockLogger, 401).isErroneousResponse(),
                is(true));
    }

    @Test
    public void isErroneousResponseGivesFalseForErrorCodeLessThan400() {
        // when, then
        assertThat(StatusResponse.createSuccessResponse(mockLogger, attributes, 399, Collections.<String, List<String>>emptyMap()).isErroneousResponse(),
                is(false));
    }

    @Test
    public void responseCodeIsSet() {
        // given
        assertThat(StatusResponse.createErrorResponse(mockLogger, 418).getResponseCode(), is(equalTo(418)));
    }

    @Test
    public void headersAreSet() {
        // given
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("X-Foo", Collections.singletonList("X-BAR"));
        headers.put("X-YZ", Collections.<String>emptyList());

        // then
        assertThat(StatusResponse.createSuccessResponse(mockLogger, attributes, 418, headers).getHeaders(),
                is(sameInstance(headers)));
    }

    @Test
    public void getRetryAfterReturnsDefaultValueIfResponseKeyDoesNotExist() {

        // given
        StatusResponse target = StatusResponse.createSuccessResponse(
                mockLogger,
                attributes,
                429,
                Collections.<String, List<String>>emptyMap()
        );

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(StatusResponse.DEFAULT_RETRY_AFTER_IN_MILLISECONDS)));
    }

    @Test
    public void getRetryAfterReturnsDefaultValueIfMultipleValuesWereRetrieved() {

        // given
        Map<String, List<String>> responseHeaders = Collections.singletonMap(StatusResponse.RESPONSE_KEY_RETRY_AFTER, Arrays.asList("100", "200"));
        StatusResponse target = StatusResponse.createSuccessResponse(mockLogger, attributes, 429, responseHeaders);

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(StatusResponse.DEFAULT_RETRY_AFTER_IN_MILLISECONDS)));
    }

    @Test
    public void getRetryAfterReturnsDefaultValueIfValueIsNotParsableAsInteger() {

        // given
        Map<String, List<String>> responseHeaders = Collections.singletonMap(StatusResponse.RESPONSE_KEY_RETRY_AFTER, Collections.singletonList("a"));
        StatusResponse target = StatusResponse.createSuccessResponse(mockLogger, attributes, 429, responseHeaders);

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(StatusResponse.DEFAULT_RETRY_AFTER_IN_MILLISECONDS)));
    }

    @Test
    public void getRetryAfterReturnsParsedValue() {

        // given
        Map<String, List<String>> responseHeaders = Collections.singletonMap(StatusResponse.RESPONSE_KEY_RETRY_AFTER, Collections.singletonList("1234"));
        StatusResponse target = StatusResponse.createSuccessResponse(mockLogger, attributes, 429, responseHeaders);

        // when
        long obtained = target.getRetryAfterInMilliseconds();

        // then
        assertThat(obtained, is(equalTo(1234L * 1000L)));
    }

    @Test
    public void errorResponseDefaultCaptureIsOn() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().isCapture(), is(true));
    }

    @Test
    public void errorResponseDefaultSendIntervalIsMinusOne() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().getSendIntervalInMilliseconds(), is(equalTo(-1)));
    }

    @Test
    public void errorResponseDefaultServerIdIsMinusOne() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().getServerId(), is(equalTo(-1)));
    }

    @Test
    public void errorResponseDefaultMaxBeaconSizeIsMinusOne() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().getMaxBeaconSizeInBytes(), is(equalTo(-1)));
    }

    @Test
    public void errorResponseDefaultCaptureCrashesIsOn() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().isCaptureCrashes(), is(true));
    }

    @Test
    public void errorResponseDefaultCaptureErrorsIsOn() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().isCaptureErrors(), is(true));
    }

    @Test
    public void errorResponseDefaultMultiplicityIsOne() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 200);

        // then
        assertThat(target.getResponseAttributes().getMultiplicity(), is(equalTo(1)));
    }

    @Test
    public void successResponseGetResponseAttributesReturnsAttributesPassedInConstructor() {
        // given
        StatusResponse target = StatusResponse.createSuccessResponse(
                mockLogger,
                attributes,
                200,
                Collections.<String, List<String>>emptyMap()
        );

        // then
        assertThat(target.getResponseAttributes(), is(attributes));
    }

    @Test
    public void mergeWithNullReturnsNull() {
        // given
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 999);

        // when
        StatusResponse obtained = target.merge(null);

        // then
        assertThat(obtained, nullValue());
    }

    @Test
    public void mergeTakesResponseCodeFromPassedStatusResponse() {
        // given
        StatusResponse toMerge = StatusResponse.createErrorResponse(mockLogger, 401);
        StatusResponse target = StatusResponse.createErrorResponse(mockLogger, 999);

        // when
        StatusResponse obtained = target.merge(toMerge);

        // then
        assertThat(obtained.getResponseCode(), is(401));
    }

    @Test
    public void mergeTakesHeadersFromPassedStatusResponse() {
        // given
        Map<String, List<String>> mergeHeaders = new HashMap<String, List<String>>();
        mergeHeaders.put("toMerge", Collections.singletonList("toMergeValue"));

        Map<String, List<String>> targetHeaders = new HashMap<String, List<String>>();
        targetHeaders.put("target", Collections.singletonList("targetValue"));

        StatusResponse toMerge = StatusResponse.createSuccessResponse(mockLogger, attributes, 200, mergeHeaders);
        StatusResponse target = StatusResponse.createSuccessResponse(mockLogger, attributes, 200, targetHeaders);

        // when
        StatusResponse obtained = target.merge(toMerge);

        // then
        assertThat(obtained.getHeaders().size(), is(1));
        assertThat(obtained.getHeaders().get("toMerge"), is(Collections.singletonList("toMergeValue")));
    }

    @Test
    public void mergeMergesAttributes() {
        // given
        Map<String, List<String>> emptyMap = Collections.emptyMap();
        ResponseAttributes mergeAttributes = mock(ResponseAttributes.class);
        ResponseAttributes targetAttributes = mock(ResponseAttributes.class);

        StatusResponse toMerge = StatusResponse.createSuccessResponse(mockLogger, mergeAttributes, 200, emptyMap);
        StatusResponse target = StatusResponse.createSuccessResponse(mockLogger, targetAttributes, 200, emptyMap);

        // when
        target.merge(toMerge);

        // then
        verify(targetAttributes, times(1)).merge(mergeAttributes);
        verifyZeroInteractions(mergeAttributes);
    }
}
