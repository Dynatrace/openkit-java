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

package com.dynatrace.openkit.protocol.http;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpResponseHttpURLConnectionAdapterTest {

    private HttpURLConnection mockUrlConnection;

    @Before
    public void setUp() {
        mockUrlConnection = mock(HttpURLConnection.class);
    }

    @Test
    public void getRequestUrlDelegatesToUnderlyingHttpURLConnection() throws MalformedURLException {
        // given
        URL url = new URL("https://foo.bar.com/foobar");
        when(mockUrlConnection.getURL()).thenReturn(url);

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        URL obtained = target.getRequestUrl();

        // then
        assertThat(obtained, is(equalTo(url)));
        verify(mockUrlConnection, times(1)).getURL();
    }

    @Test
    public void getRequestMethodDelegatesToUnderlyingHttpURLConnection() {
        // given
        String requestMethod = "GET";
        when(mockUrlConnection.getRequestMethod()).thenReturn(requestMethod);

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        String obtained = target.getRequestMethod();

        // then
        assertThat(obtained, is(equalTo(requestMethod)));
        verify(mockUrlConnection, times(1)).getRequestMethod();
    }

    @Test
    public void getResponseCodeDelegatesToUnderlyingHttpURLConnection() throws IOException {
        // given
        int responseCode = 200;
        when(mockUrlConnection.getResponseCode()).thenReturn(responseCode);

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        int obtained = target.getResponseCode();

        // then
        assertThat(obtained, is(equalTo(responseCode)));
        verify(mockUrlConnection, times(1)).getResponseCode();
    }

    @Test
    public void getResponseCodeReturnsMinIntegerValueIfUnderlyingHttpURLConnectionThrowsIOException() throws IOException {
        // given
        when(mockUrlConnection.getResponseCode()).thenThrow(new IOException());

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        int obtained = target.getResponseCode();

        // then
        assertThat(obtained, is(equalTo(Integer.MIN_VALUE)));
        verify(mockUrlConnection, times(1)).getResponseCode();
    }

    @Test
    public void getResponseMessageDelegatesToUnderlyingHttpURLConnection() throws IOException {
        // given
        String responseMessage = "OK";
        when(mockUrlConnection.getResponseMessage()).thenReturn(responseMessage);

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        String obtained = target.getResponseMessage();

        // then
        assertThat(obtained, is(equalTo(responseMessage)));
        verify(mockUrlConnection, times(1)).getResponseMessage();
    }

    @Test
    public void getResponseMessageReturnsNullIFUnderlyingHttpURLConnectionThrowsIOException() throws IOException {
        // given
        when(mockUrlConnection.getResponseCode()).thenThrow(new IOException());

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        String obtained = target.getResponseMessage();

        // then
        assertThat(obtained, is(nullValue()));
        verify(mockUrlConnection, times(1)).getResponseMessage();
    }

    @Test
    public void getHeadersDelegatesToUnderlyingHttpURLConnection() {
        // given
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        when(mockUrlConnection.getHeaderFields()).thenReturn(requestHeaders);

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        Map<String, List<String>> obtained = target.getHeaders();

        // then
        assertThat(obtained, is(equalTo(requestHeaders)));
        verify(mockUrlConnection, times(1)).getHeaderFields();
    }

    @Test
    public void getHeaderDelegatesToUnderlyingHttpURLConnection() {
        // given
        String requestHeaderName = "X-Foo";
        String requestHeaderValue = "bar";
        when(mockUrlConnection.getHeaderField(requestHeaderName)).thenReturn(requestHeaderValue);

        HttpResponseHttpURLConnectionAdapter target = new HttpResponseHttpURLConnectionAdapter(mockUrlConnection);

        // when
        String obtained = target.getHeader(requestHeaderName);

        // then
        assertThat(obtained, is(equalTo(requestHeaderValue)));
        verify(mockUrlConnection, times(1)).getHeaderField(requestHeaderName);
    }
}
