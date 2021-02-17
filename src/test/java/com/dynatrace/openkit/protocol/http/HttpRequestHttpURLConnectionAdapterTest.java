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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class HttpRequestHttpURLConnectionAdapterTest {

    private HttpURLConnection mockUrlConnection;

    @Before
    public void setUp() {
        mockUrlConnection = mock(HttpURLConnection.class);
    }

    @Test
    public void getUrlDelegatesToUnderlyingHttpURLConnection() throws MalformedURLException {
        // given
        URL url = new URL("https://foo.bar.com/foobar");
        when(mockUrlConnection.getURL()).thenReturn(url);

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when
        URL obtained = target.getUrl();

        // then
        assertThat(obtained, is(equalTo(url)));
        verify(mockUrlConnection, times(1)).getURL();
    }

    @Test
    public void getMethodDelegatesToUnderlyingHttpURLConnection() {
        // given
        String requestMethod = "GET";
        when(mockUrlConnection.getRequestMethod()).thenReturn(requestMethod);

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when
        String obtained = target.getMethod();

        // then
        assertThat(obtained, is(equalTo(requestMethod)));
        verify(mockUrlConnection, times(1)).getRequestMethod();
    }

    @Test
    public void getHeadersDelegatesToUnderlyingHttpURLConnection() {
        // given
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        when(mockUrlConnection.getRequestProperties()).thenReturn(requestHeaders);

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when
        Map<String, List<String>> obtained = target.getHeaders();

        // then
        assertThat(obtained, is(equalTo(requestHeaders)));
        verify(mockUrlConnection, times(1)).getRequestProperties();
    }

    @Test
    public void getHeaderDelegatesToUnderlyingHttpURLConnection() {
        // given
        String requestHeaderName = "X-Foo";
        String requestHeaderValue = "bar";
        when(mockUrlConnection.getRequestProperty(requestHeaderName)).thenReturn(requestHeaderValue);

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when
        String obtained = target.getHeader(requestHeaderName);

        // then
        assertThat(obtained, is(equalTo(requestHeaderValue)));
        verify(mockUrlConnection, times(1)).getRequestProperty(requestHeaderName);
    }

    @Test
    public void setHeaderDelegatesToUnderlyingHttpURLConnection() {
        // given
        String requestHeaderName = "User-Agent";
        String requestHeaderValue = "curl/7.64.1";

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when
        target.setHeader(requestHeaderName, requestHeaderValue);

        // then
        verify(mockUrlConnection, times(1)).setRequestProperty(requestHeaderName, requestHeaderValue);
    }

    @Test
    public void setHeaderDoesNotForwardCallIfNameIsNull() {
        // given
        String requestHeaderValue = "curl/7.64.1";

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when
        target.setHeader(null, requestHeaderValue);

        // then
        verifyZeroInteractions(mockUrlConnection);
    }

    @Test
    public void setHeaderDoesNotForwardCallIfHeaderNameIsRestricted() {
        // given
        String[] restrictedHeaders = new String[] {
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Connection",
            "Content-Length",
            "Content-Transfer-Encoding",
            "Host",
            "Keep-Alive",
            "Origin",
            "Trailer",
            "Transfer-Encoding",
            "Upgrade",
            "Via"
        };

        HttpRequestHttpURLConnectionAdapter target = new HttpRequestHttpURLConnectionAdapter(mockUrlConnection);

        // when using mixed case
        for (String restrictedHeaderName : restrictedHeaders) {
            target.setHeader(restrictedHeaderName, "foobar");
        }

        // then
        verifyZeroInteractions(mockUrlConnection);

        // when using all lower case
        for (String restrictedHeaderName : restrictedHeaders) {
            target.setHeader(restrictedHeaderName.toLowerCase(), "foobar");
        }

        // then
        verifyZeroInteractions(mockUrlConnection);

        // when using all upper case
        for (String restrictedHeaderName : restrictedHeaders) {
            target.setHeader(restrictedHeaderName.toUpperCase(), "foobar");
        }

        // then
        verifyZeroInteractions(mockUrlConnection);
    }
}
