/**
 * Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link WebRequestTracerBaseImplTest} implementation having knowledge of the code.
 */
public class WebRequestTracerURLConnectionTest {

    private static final String UNKNOWN_URL_STRING = "<unknown>";

    private Logger mockLogger;
    private Beacon mockBeacon;
    private URLConnection mockURLConnection;
    private OpenKitComposite  parentOpenKitObject;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        mockBeacon = mock(Beacon.class);
        mockURLConnection = mock(URLConnection.class);
        parentOpenKitObject = mock(OpenKitComposite.class);
    }

    @Test
    public void passingNullURLConnectionLeavesUnknownURLString() {
        // given
        WebRequestTracerURLConnection target = createConnectionUrl();

        // then
        assertThat(target.getURL(), is(equalTo(UNKNOWN_URL_STRING)));
    }

    @Test
    public void whenURLOfURLConnectionIsNullUnknownURLStringIsLeft() {
        // given
        when(mockURLConnection.getURL()).thenReturn(null);

        WebRequestTracerURLConnection target = createConnectionUrl();

        // then
        assertThat(target.getURL(), is(equalTo(UNKNOWN_URL_STRING)));
    }

    @Test
    public void whenURLOfURLConnectionIsNullTagIsSet() {
        // given
        when(mockURLConnection.getURL()).thenReturn(null);

        WebRequestTracerURLConnection target = createConnectionUrl();

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

        createConnectionUrl();

        // then verify
        verify(mockURLConnection, times(1)).getURL();
        verify(mockURLConnection, times(1)).getRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER);
        verifyNoMoreInteractions(mockURLConnection);
    }

    @Test
    public void whenURLOfURLConnectionIsNotNullURLStringIsAssignedAccordingly() throws MalformedURLException {
        // given
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com"));

        WebRequestTracerURLConnection target = createConnectionUrl();

        // then
        assertThat(target.getURL(), is(equalTo("https://www.google.com")));
    }

    @Test
    public void urlStoredDoesNotContainRequestParameters() throws MalformedURLException {
        // given
        when(mockURLConnection.getURL()).thenReturn(new URL("https://www.google.com/foo/bar?foo=bar&asdf=jklo"));

        WebRequestTracerURLConnection target = createConnectionUrl();

        // then
        assertThat(target.getURL(), is(equalTo("https://www.google.com/foo/bar")));
    }

    @Test
    public void aNewlyCreatedWebRequestTracerDoesNotAttachToTheParent() {
        // given
        WebRequestTracerURLConnection target = createConnectionUrl();

        // then parent is stored, but no interaction with parent happened
        assertThat(target.getParent(), is(sameInstance(parentOpenKitObject)));
        verify(parentOpenKitObject, times(1)).getActionID();
        verifyNoMoreInteractions(parentOpenKitObject);
    }

    @Test
    public void connectionTagIsIgnoredIfSettingRequestPropertyFails() throws Exception {
        // given
        doThrow(new RuntimeException()).when(mockURLConnection).setRequestProperty(anyString(), anyString());

        // when
        createConnectionUrl();

        // then
        verify(mockURLConnection, times(1)).setRequestProperty(anyString(), ArgumentMatchers.<String>any());
    }

    @Test
    public void createInstanceWithNullConnectionUrl() {

    }

    private WebRequestTracerURLConnection createConnectionUrl() {
        return new WebRequestTracerURLConnection(
                mockLogger,
                parentOpenKitObject,
                mockBeacon,
                mockURLConnection
        );
    }
}
