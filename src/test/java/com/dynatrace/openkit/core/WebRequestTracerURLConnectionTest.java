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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link WebRequestTracerBaseImplTest} implementation having knowledge of the code.
 */
public class WebRequestTracerURLConnectionTest {

    private static final String UNKNOWN_URL_STRING = "<unknown>";

    private Logger mockLogger;
    private Beacon mockBeacon;
    private URLConnection mockURLConnection;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        mockBeacon = mock(Beacon.class);
        mockURLConnection = mock(URLConnection.class);
    }

    @Test
    public void passingNullURLConnectionLeavesUnknownURLString() {
        // given
        WebRequestTracerURLConnection target = new WebRequestTracerURLConnection(mockLogger, mockBeacon, 0, null);

        // then
        assertThat(target.getURL(), is(equalTo(UNKNOWN_URL_STRING)));
    }

    @Test
    public void whenURLOfURLConnectionIsNullUnknownURLStringIsLeft() {
        // given
        when(mockURLConnection.getURL()).thenReturn(null);

        WebRequestTracerURLConnection target = new WebRequestTracerURLConnection(mockLogger, mockBeacon, 0, mockURLConnection);

        // then
        assertThat(target.getURL(), is(equalTo(UNKNOWN_URL_STRING)));
    }

    @Test
    public void whenURLOfURLConnectionIsNullTagIsSet() {
        // given
        when(mockURLConnection.getURL()).thenReturn(null);

        WebRequestTracerURLConnection target = new WebRequestTracerURLConnection(mockLogger, mockBeacon, 0, mockURLConnection);

        // then verify
        verify(mockURLConnection, times(1)).getURL();
        verify(mockURLConnection, times(1)).getRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER);
        verify(mockURLConnection, times(1)).setRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER, target.getTag());
        verifyNoMoreInteractions(mockURLConnection);
    }

    @Test
    public void whenTagIsAlreadySetThanItIsNotSetAgain() {
        // given
        when(mockURLConnection.getURL()).thenReturn(null);
        when(mockURLConnection.getRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER)).thenReturn("");

        WebRequestTracerURLConnection target = new WebRequestTracerURLConnection(mockLogger, mockBeacon, 0, mockURLConnection);

        // then verify
        verify(mockURLConnection, times(1)).getURL();
        verify(mockURLConnection, times(1)).getRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER);
        verifyNoMoreInteractions(mockURLConnection);
    }

    @Test
    public void whenURLOfURLConnectionIsNotNullURLStringIsAssignedAccordingly() throws MalformedURLException {
        // given
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com"));

        WebRequestTracerURLConnection target = new WebRequestTracerURLConnection(mockLogger, mockBeacon, 0, mockURLConnection);

        // then
        assertThat(target.getURL(), is(equalTo("https://www.google.com")));
    }

    @Test
    public void urlStoredDoesNotContainRequestParameters() throws MalformedURLException {
        // given
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com/foo/bar?foo=bar&asdf=jklo"));

        WebRequestTracerURLConnection target = new WebRequestTracerURLConnection(mockLogger, mockBeacon, 0, mockURLConnection);

        // then
        assertThat(target.getURL(), is(equalTo("https://www.google.com/foo/bar")));
    }

}
