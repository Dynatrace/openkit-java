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

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.*;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


public class BeaconTest {

    private static final String APP_ID = "appID";
    private static final String APP_NAME = "appName";
    private static final int ACTION_ID = 17;
    private static final int SERVER_ID = 123;
    private static final long DEVICE_ID = 456;
    private static final int THREAD_ID = 1234567;

    private Configuration configuration;
    private ThreadIDProvider threadIDProvider;
    private TimingProvider timingProvider;

    private Logger logger;

    @Before
    public void setUp() {
        configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn(APP_ID);
        when(configuration.getApplicationIDPercentEncoded()).thenReturn(APP_ID);
        when(configuration.getApplicationName()).thenReturn(APP_NAME);
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.getDeviceID()).thenReturn(DEVICE_ID);
        when(configuration.isCapture()).thenReturn(true);
        when(configuration.isCaptureErrors()).thenReturn(true);
        when(configuration.isCaptureCrashes()).thenReturn(true);
        when(configuration.getMaxBeaconSize()).thenReturn(30 * 1024); // 30kB
        BeaconConfiguration mockBeaconConfiguration = mock(BeaconConfiguration.class);
        when(mockBeaconConfiguration.getMultiplicity()).thenReturn(1);
        when(mockBeaconConfiguration.getDataCollectionLevel()).thenReturn(DataCollectionLevel.USER_BEHAVIOR);
        when(mockBeaconConfiguration.getCrashReportingLevel()).thenReturn(CrashReportingLevel.OPT_IN_CRASHES);
        when(mockBeaconConfiguration.isCapturingAllowed()).thenReturn(true);
        when(configuration.getBeaconConfiguration()).thenReturn(mockBeaconConfiguration);

        HTTPClientConfiguration mockHTTPClientConfiguration = mock(HTTPClientConfiguration.class);
        when(mockHTTPClientConfiguration.getServerID()).thenReturn(SERVER_ID);
        when(configuration.getHttpClientConfig()).thenReturn(mockHTTPClientConfiguration);

        BeaconConfiguration beaconConfiguration = new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OPT_IN_CRASHES);
        when(configuration.getBeaconConfiguration()).thenReturn(beaconConfiguration);

        threadIDProvider = mock(ThreadIDProvider.class);
        when(threadIDProvider.getThreadID()).thenReturn(THREAD_ID);

        timingProvider = mock(TimingProvider.class);
        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(0L);
        when(timingProvider.isTimeSyncSupported()).thenReturn(true);

        logger = mock(Logger.class);
    }

    @Test
    public void defaultBeaconConfigurationDoesNotDisableCapturing() {

        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // then
        assertThat(target.isCapturingDisabled(), is(false));
    }

    @Test
    public void defaultBeaconConfigurationSetsMultiplicityToOne() {

        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // then
        assertThat(target.getMultiplicity(), is(equalTo(1)));
    }

    @Test
    public void createIDs() {
        // create test environment
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // verify that the created sequence numbers are incremented
        int id1 = beacon.createID();
        assertThat(id1, is(1));

        int id2 = beacon.createID();
        assertThat(id2, is(2));

        int id3 = beacon.createID();
        assertThat(id3, is(3));
    }

    @Test
    public void getCurrentTimestamp() {

        // given
        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(42L);
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // when obtaining the timestamp
        long timestamp = beacon.getCurrentTimestamp();

        // then verify
        assertThat(timestamp, is(equalTo(42L)));

        // verify called twice (once in Beacon's ctor) and once when invoking the call
        verify(timingProvider, times(2)).provideTimestampInMilliseconds();
    }

    @Test
    public void createSequenceNumbers() {
        // create test environment
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // verify that the created sequence numbers are incremented
        int id1 = beacon.createSequenceNumber();
        assertThat(id1, is(1));

        int id2 = beacon.createSequenceNumber();
        assertThat(id2, is(2));

        int id3 = beacon.createSequenceNumber();
        assertThat(id3, is(3));
    }

    @Test
    public void createWebRequestTag() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);

        // when
        int sequenceNo = 42;
        String tag = beacon.createTag(action, sequenceNo);

        // then
        assertThat(tag, is(equalTo("MT_3_" + SERVER_ID + "_" + DEVICE_ID + "_0_" + APP_ID + "_" + ACTION_ID + "_" + THREAD_ID + "_" + sequenceNo)));
    }

    @Test
    public void addValidActionEvent() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        int parentID = 13;
        when(action.getParentID()).thenReturn(parentID);
        String actionName = "MyAction";
        when(action.getName()).thenReturn(actionName);

        // when
        beacon.addAction(action);
        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(equalTo(new String[]{
            "et=1&na=" + actionName + "&it=" + THREAD_ID + "&ca=" + ACTION_ID + "&pa=" + parentID + "&s0=0&t0=0&s1=0&t1=0"
        })));
    }

    @Test
    public void addEndSessionEvent() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        SessionImpl session = mock(SessionImpl.class);

        // when
        beacon.endSession(session);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=19&it=" + THREAD_ID + "&pa=0&s0=1&t0=0"})));
    }

    @Test
    public void reportValidValueInt() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "IntValue";
        int value = 42;

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=12&na=" + valueName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&vl=" + String.valueOf(value)
        })));
    }

    @Test
    public void reportValidValueDouble() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "DoubleValue";
        double value = 3.1415;

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=13&na=" + valueName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&vl=" + String.valueOf(value)
        })));
    }

    @Test
    public void reportValidValueString() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "StringValue";
        String value = "HelloWorld";

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=11&na=" + valueName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&vl=" + value
        })));
    }

    @Test
    public void reportValueStringWithValueNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "StringValue";

        // when
        beacon.reportValue(action, valueName, null);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=11&na=StringValue&it=1234567&pa=17&s0=1&t0=0"})));
    }

    @Test
    public void reportValueStringWithValueNullAndNameNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);

        // when
        beacon.reportValue(action, null, null);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=11&it=1234567&pa=17&s0=1&t0=0"})));
    }

    @Test
    public void reportValidEvent() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String eventName = "SomeEvent";

        // when
        beacon.reportEvent(action, eventName);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=10&na=" + eventName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0"})));
    }

    @Test
    public void reportEventWithNameNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);

        // when
        beacon.reportEvent(action, null);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=10&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0"})));
    }

    @Test
    public void reportError() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String errorName = "SomeEvent";
        int errorCode = -123;
        String reason = "SomeReason";

        // when
        beacon.reportError(action, errorName, errorCode, reason);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=40&na=" + errorName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&ev=" + errorCode + "&rs=" + reason
        })));
    }

    @Test
    public void reportErrorNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        int errorCode = -123;

        // when
        beacon.reportError(action, null, errorCode, null);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=40&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&ev=" + errorCode})));
    }

    @Test
    public void reportValidCrash() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String errorName = "SomeEvent";
        String reason = "SomeReason";
        String stacktrace = "SomeStacktrace";

        // when
        beacon.reportCrash(errorName, reason, stacktrace);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=50&na=" + errorName + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&rs=" + reason + "&st=" + stacktrace
        })));
    }

    @Test
    public void reportCrashWithDetailsNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String errorName = "errorName";

        // when
        beacon.reportCrash(errorName, null, null);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=50&na=" + errorName + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0"})));
    }

    @Test
    public void addWebRequest() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        WebRequestTracerURLConnection webRequestTracer = mock(WebRequestTracerURLConnection.class);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        // when
        beacon.addWebRequest(action, webRequestTracer);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=30&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=0&t0=0&s1=0&t1=0&bs=13&br=14&rc=15"
        })));
    }

    @Test
    public void addUserIdentifyEvent() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String userID = "myTestUser";

        // when
        beacon.identifyUser(userID);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=60&na=" + userID + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0"})));
    }

    @Test
    public void addUserIdentifyWithNullUserIDEvent() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // when
        beacon.identifyUser(null);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=60&it=" + THREAD_ID + "&pa=0&s0=1&t0=0"})));
    }

    @Test
    public void canAddSentBytesToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);
        int bytesSent = 12321;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String
                .valueOf(bytesSent)
        })));
    }

    @Test
    public void canAddSentBytesValueZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);
        int bytesSent = 0;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String
                .valueOf(bytesSent)
        })));
    }

    @Test
    public void cannotAddSentBytesWithInvalidValueSmallerZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);

        // when
        webRequest.start().setBytesSent(-5).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0"})));
    }

    @Test
    public void canAddReceivedBytesToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);
        int bytesReceived = 12321;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0&br=" + String
                .valueOf(bytesReceived)
        })));
    }

    @Test
    public void canAddReceivedBytesValueZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);
        int bytesReceived = 0;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0&br=" + String
                .valueOf(bytesReceived)
        })));
    }

    @Test
    public void cannotAddReceivedBytesWithInvalidValueSmallerZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);

        // when
        webRequest.start().setBytesReceived(-1).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{"et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0"})));
    }

    @Test
    public void canAddBothSentBytesAndReceivedBytesToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        String testURL = "https://localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(logger, beacon, rootAction, testURL);
        int bytesReceived = 12321;
        int bytesSent = 123;

        // when
        webRequest.start()
                  .setBytesSent(bytesSent)
                  .setBytesReceived(bytesReceived)
                  .stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[]{
            "et=30&na=" + URLEncoder.encode(testURL, "UTF-8") + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String
                .valueOf(bytesSent) + "&br=" + String.valueOf(bytesReceived)
        })));
    }

    @Test
    public void canAddRootActionIfCaptureIsOn() {
        // given
        when(configuration.isCapture()).thenReturn(true);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        // when
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        beacon.addAction(rootAction);

        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(equalTo(new String[]{"et=1&na=" + actionName + "&it=" + THREAD_ID + "&ca=0&pa=0&s0=0&t0=0&s1=0&t1=0"})));
    }

    @Test
    public void cannotAddRootActionIfCaptureIsOff() {
        // given
        when(configuration.isCapture()).thenReturn(false);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        // when
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        beacon.addAction(rootAction);

        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(arrayWithSize(0)));
    }

    @Test
    public void canHandleNoDataInBeaconSend() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient mockClient = mock(HTTPClient.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(mockClient);

        // when
        StatusResponse response = beacon.send(httpClientProvider);

        // then (verify, that null is returned as no data was sent)
        assertThat(response, nullValue());
    }

    @Test
    public void sendValidData() {
        // given
        String ipAddress = "127.0.0.1";
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, ipAddress, threadIDProvider, timingProvider);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 200;
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class))).thenReturn(new StatusResponse(logger, "", responseCode, Collections.<String, List<String>>emptyMap()));
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        beacon.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = beacon.send(httpClientProvider);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class));
    }

    @Test
    public void sendDataAndFakeErrorResponse() {
        // given
        String ipAddress = "127.0.0.1";
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, ipAddress, threadIDProvider, timingProvider);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 418;
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class))).thenReturn(new StatusResponse(logger, "", responseCode, Collections.<String, List<String>>emptyMap()));
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        beacon.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = beacon.send(httpClientProvider);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class));
    }

    @Test
    public void clearDataFromBeaconCache() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        // add various data
        ActionImpl action = mock(ActionImpl.class);
        beacon.addAction(action);
        beacon.reportValue(action, "IntValue", 42);
        beacon.reportValue(action, "DoubleValue", 3.1415);
        beacon.reportValue(action, "StringValue", "HelloWorld");
        beacon.reportEvent(action, "SomeEvent");
        beacon.reportError(action, "SomeError", -123, "SomeReason");
        beacon.reportCrash("SomeCrash", "SomeReason", "SomeStacktrace");
        SessionImpl session = mock(SessionImpl.class);
        beacon.endSession(session);

        // when
        beacon.clearData();

        // then (verify, all data is cleared)
        String[] events = beacon.getEvents();
        assertThat(events, emptyArray());
        String[] actions = beacon.getActions();
        assertThat(actions, emptyArray());
        assertThat(beacon.isEmpty(), is(true));
    }

    @Test
    public void noSessionIsAddedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        SessionImpl session = mock(SessionImpl.class);

        // when
        target.endSession(session);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(session);
    }

    @Test
    public void noActionIsAddedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        ActionImpl action = mock(ActionImpl.class);

        // when
        target.addAction(action);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(action);
    }

    @Test
    public void noIntValueIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        int intValue = 42;
        ActionImpl parentAction = mock(ActionImpl.class);

        // when
        target.reportValue(parentAction, "intValue", intValue);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(parentAction);
    }

    @Test
    public void noDoubleValueIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        double doubleValue = Math.E;
        ActionImpl parentAction = mock(ActionImpl.class);

        // when
        target.reportValue(parentAction, "doubleValue", doubleValue);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(parentAction);
    }

    @Test
    public void noStringValueIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        String stringValue = "Write once, debug everywhere";
        ActionImpl parentAction = mock(ActionImpl.class);

        // when
        target.reportValue(parentAction, "doubleValue", stringValue);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(parentAction);
    }

    @Test
    public void noEventIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        ActionImpl parentAction = mock(ActionImpl.class);

        // when
        target.reportEvent(parentAction, "Event name");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(parentAction);
    }

    @Test
    public void noErrorIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        ActionImpl parentAction = mock(ActionImpl.class);

        // when
        target.reportError(parentAction, "Error name", 123, "The reason for this error");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(parentAction);
    }

    @Test
    public void noCrashIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        // when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void noWebRequestIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        ActionImpl parentAction = mock(ActionImpl.class);
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);

        // when
        target.addWebRequest(parentAction, webRequestTracer);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(parentAction, webRequestTracer);
    }

    @Test
    public void noUserIdentificationIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        // when
        target.identifyUser("jane.doe@acme.com");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void noWebRequestIsReportedForDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);
        WebRequestTracerURLConnection mockWebRequestTracer = mock(WebRequestTracerURLConnection.class);
        //when
        target.addWebRequest(mockAction, mockWebRequestTracer);

        //then
        verifyZeroInteractions(mockAction);
        verifyZeroInteractions(mockWebRequestTracer);
        //verify nothing has been serialized
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void webRequestIsReportedForDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);
        WebRequestTracerURLConnection mockWebRequestTracer = mock(WebRequestTracerURLConnection.class);

        //when
        target.addWebRequest(mockAction, mockWebRequestTracer);

        //then
        verify(mockAction, times(1)).getID();
        verify(mockWebRequestTracer, times(1)).getBytesReceived();
        verify(mockWebRequestTracer, times(1)).getBytesSent();
        verify(mockWebRequestTracer, times(1)).getResponseCode();
        //verify nothing has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void webRequestIsReportedForDataCollectionLevel2() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);
        WebRequestTracerURLConnection mockWebRequestTracer = mock(WebRequestTracerURLConnection.class);

        //when
        target.addWebRequest(mockAction, mockWebRequestTracer);

        //then
        verify(mockAction, times(1)).getID();
        verify(mockWebRequestTracer, times(1)).getBytesReceived();
        verify(mockWebRequestTracer, times(1)).getBytesSent();
        verify(mockWebRequestTracer, times(1)).getResponseCode();
        //verify nothing has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void beaconReturnsEmptyTagOnDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        String returnedTag = target.createTag(mockAction, 1);

        //then
        assertThat(returnedTag.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void beaconReturnsValidTagOnDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        String returnedTag = target.createTag(mockAction, 1);

        //then
        assertThat(returnedTag.isEmpty(), is(false));
        verify(mockAction, times(1)).getID();
    }

    @Test
    public void beaconReturnsValidTagOnDataCollectionLevel2() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        String returnedTag = target.createTag(mockAction, 1);

        //then
        assertThat(returnedTag.isEmpty(), is(false));
        verify(mockAction, times(1)).getID();
    }

    @Test
    public void cannotIdentifyUserOnDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        //when
        target.identifyUser("jane@doe.com");

        //then
        //verify nothing has been serialized
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void cannotIdentifyUserOnDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        //when
        target.identifyUser("jane@doe.com");

        //then
        //verify nothing has been serialized
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void canIdentifyUserOnDataCollectionLevel2() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        //when
        target.identifyUser("jane@doe.com");

        //then
        //verify user tag has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void visitorIDIsRandomizedOnDataCollectionLevel0() {
        //given
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockConfiguration.getApplicationVersion()).thenReturn("v1");
        Device testDevice = new Device("OS", "MAN", "MODEL");
        when(mockConfiguration.getDevice()).thenReturn(testDevice);
        when(mockConfiguration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        Random mockRandom = mock(Random.class);
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), mockConfiguration, "127.0.0.1", threadIDProvider, timingProvider, mockRandom);

        //when
        target.getDeviceID();

        // then verify that the device id is not taken from the configuration
        // this means it must have been generated randomly
        verify(mockConfiguration, times(0)).getDeviceID();
        verify(mockRandom, times(1)).nextLong();
    }

    @Test
    public void visitorIDIsRandomizedOnDataCollectionLevel1() {
        //given
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockConfiguration.getApplicationVersion()).thenReturn("v1");
        Device testDevice = new Device("OS", "MAN", "MODEL");
        when(mockConfiguration.getDevice()).thenReturn(testDevice);
        when(mockConfiguration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        Random mockRandom = mock(Random.class);
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), mockConfiguration, "127.0.0.1", threadIDProvider, timingProvider, mockRandom);

        //when
        target.getDeviceID();

        // then verify that the device id is not taken from the configuration
        // this means it must have been generated randomly
        verify(mockConfiguration, times(0)).getDeviceID();
        verify(mockRandom, times(1)).nextLong();
    }

    @Test
    public void givenVisitorIDIsUsedOnDataCollectionLevel2() {
        long TEST_DEVICE_ID = 1338;
        //given
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockConfiguration.getApplicationVersion()).thenReturn("v1");
        Device testDevice = new Device("OS", "MAN", "MODEL");
        when(mockConfiguration.getDevice()).thenReturn(testDevice);
        when(mockConfiguration.getDeviceID()).thenReturn(TEST_DEVICE_ID);
        when(mockConfiguration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        Random mockRandom = mock(Random.class);
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), mockConfiguration, "127.0.0.1", threadIDProvider, timingProvider, mockRandom);

        //when
        long visitorID = target.getDeviceID();

        //then verify that device id is taken from configuration
        verify(mockConfiguration, times(1)).getDeviceID();
        verifyNoMoreInteractions(mockRandom);
        assertThat(visitorID, is(equalTo(TEST_DEVICE_ID)));
    }

    @Test
    public void randomVisitorIDCannotBeNegativeOnDataCollectionLevel0() {
        long TEST_DEVICE_ID = 1338;
        //given
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockConfiguration.getApplicationVersion()).thenReturn("v1");
        Device testDevice = new Device("OS", "MAN", "MODEL");
        when(mockConfiguration.getDevice()).thenReturn(testDevice);
        when(mockConfiguration.getDeviceID()).thenReturn(TEST_DEVICE_ID);
        when(mockConfiguration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextLong()).thenReturn(-123456789L);
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), mockConfiguration, "127.0.0.1", threadIDProvider, timingProvider, mockRandom);

        //when
        long visitorID = target.getDeviceID();

        //then verify that the id is positive regardless of the data collection level
        verify(mockRandom, times(1)).nextLong();
        assertThat(visitorID, is(greaterThanOrEqualTo(0L)));
        assertThat(visitorID, is(equalTo(-123456789L & Long.MAX_VALUE)));
    }

    @Test
    public void randomVisitorIDCannotBeNegativeOnDataCollectionLevel1() {
        long TEST_DEVICE_ID = 1338;
        //given
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockConfiguration.getApplicationVersion()).thenReturn("v1");
        Device testDevice = new Device("OS", "MAN", "MODEL");
        when(mockConfiguration.getDevice()).thenReturn(testDevice);
        when(mockConfiguration.getDeviceID()).thenReturn(TEST_DEVICE_ID);
        when(mockConfiguration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        Random mockRandom = mock(Random.class);
        when(mockRandom.nextLong()).thenReturn(-123456789L);
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), mockConfiguration, "127.0.0.1", threadIDProvider, timingProvider, mockRandom);

        //when
        long visitorID = target.getDeviceID();

        //then verify that the id is positive regardless of the data collection level
        verify(mockRandom, times(1)).nextLong();
        assertThat(visitorID, is(greaterThanOrEqualTo(0L)));
        assertThat(visitorID, is(equalTo(-123456789L & Long.MAX_VALUE)));
    }

    @Test
    public void sessionIDIsAlwaysValue1OnDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        //when
        int sessionNumber = target.getSessionNumber();

        //then
        assertThat(sessionNumber, is(equalTo(1)));
    }

    @Test
    public void sessionIDIsAlwaysValue1OnDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        //when
        int sessionNumber = target.getSessionNumber();

        //then
        assertThat(sessionNumber, is(equalTo(1)));
    }

    @Test
    public void sessionIDIsValueFromSessionIDProviderOnDataCollectionLevel2() {
        // given
        final int SESSION_ID = 1234;
        Configuration mockConfiguration = mock(Configuration.class);
        when(mockConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockConfiguration.getApplicationVersion()).thenReturn("v1");
        Device testDevice = new Device("OS", "MAN", "MODEL");
        when(mockConfiguration.getDevice()).thenReturn(testDevice);
        when(mockConfiguration.createSessionNumber()).thenReturn(SESSION_ID);
        when(mockConfiguration.getBeaconConfiguration()).thenReturn(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), mockConfiguration, "127.0.0.1", threadIDProvider, timingProvider);

        //when
        int sessionNumber = target.getSessionNumber();

        //then
        assertThat(sessionNumber, is(equalTo(SESSION_ID)));
    }

    @Test
    public void reportCrashDoesNotReportOnCrashReportingLevel0() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        //when
        target.reportCrash("OutOfMemory exception", "insufficient memory", "stacktrace:123");

        //then
        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        assertThat(target.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void reportCrashDoesNotReportOnCrashReportingLevel1() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OPT_OUT_CRASHES));

        //when
        target.reportCrash("OutOfMemory exception", "insufficient memory", "stacktrace:123");

        //then
        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        assertThat(target.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void reportCrashDoesReportOnCrashReportingLevel2() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OPT_IN_CRASHES));

        //when
        target.reportCrash("OutOfMemory exception", "insufficient memory", "stacktrace:123");

        //then
        verify(timingProvider, times(2)).provideTimestampInMilliseconds();
        assertThat(target.isEmpty(), is(equalTo(false)));
    }

    @Test
    public void actionNotReportedForDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        target.addAction(mockAction);

        //then
        //verify action has not been serialized
        verify(mockAction, times(0)).getID();
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void actionReportedForDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        target.addAction(mockAction);

        //then
        //verify action has been serialized
        verify(mockAction, times(1)).getID();
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void actionReportedForDataCollectionLevel2() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        target.addAction(mockAction);

        //then
        //verify action has been serialized
        verify(mockAction, times(1)).getID();
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void sessionNotReportedForDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        SessionImpl mockSession = mock(SessionImpl.class);

        //when
        target.endSession(mockSession);

        //then
        //verify session has not been serialized
        verify(mockSession, times(0)).getEndTime();
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void sessionReportedForDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));
        SessionImpl mockSession = mock(SessionImpl.class);

        //when
        target.endSession(mockSession);

        //then
        //verify session has been serialized
        verify(mockSession, times(2)).getEndTime();
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void sessionReportedForDataCollectionLevel2() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));
        SessionImpl mockSession = mock(SessionImpl.class);

        //when
        target.endSession(mockSession);

        //then
        //verify session has been serialized
        verify(mockSession, times(2)).getEndTime();
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void errorNotReportedForDataCollectionLevel0() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        target.reportError(mockAction, "DivByZeroError", 127, "out of math");

        //then
        //verify action has not been serialized
        verify(mockAction, times(0)).getID();
        assertThat(target.isEmpty(), is(true));
    }

    @Test
    public void errorReportedForDataCollectionLevel1() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        target.reportError(mockAction, "DivByZeroError", 127, "out of math");

        //then
        //verify action has been serialized
        verify(mockAction, times(1)).getID();
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void errorReportedForDataCollectionLevel2() {
        //given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));
        ActionImpl mockAction = mock(ActionImpl.class);

        //when
        target.reportError(mockAction, "DivByZeroError", 127, "out of math");

        //then
        //verify action has been serialized
        verify(mockAction, times(1)).getID();
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void IntValueIsNotReportedForDataCollectionLevel0() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", 123);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void IntValueIsNotReportedForDataCollectionLevel1() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", 123);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void IntValueIsReportedForDataCollectionLevel2() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", 123);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(false));
        verify(mockAction, times(1)).getID();
    }

    @Test
    public void DoubleValueIsNotReportedForDataCollectionLevel0() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", 2.71);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void DoubleValueIsNotReportedForDataCollectionLevel1() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", 2.71);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void DoubleValueIsReportedForDataCollectionLevel2() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", 2.71);

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(false));
        verify(mockAction, times(1)).getID();
    }

    @Test
    public void StringValueIsNotReportedForDataCollectionLevel0() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", "test data");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void StringValueIsNotReportedForDataCollectionLevel1() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", "test data");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void StringValueIsReportedForDataCollectionLevel2() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportValue(mockAction, "test value", "test data");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(false));
        verify(mockAction, times(1)).getID();
    }

    @Test
    public void NamedEventIsNotReportedForDataCollectionLevel0() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportEvent(mockAction, "test event");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void NamedEventIsNotReportedForDataCollectionLevel1() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportEvent(mockAction, "test event");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
        verifyZeroInteractions(mockAction);
    }

    @Test
    public void NamedEventIsReportedForDataCollectionLevel2() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF));

        ActionImpl mockAction = mock(ActionImpl.class);

        // when
        target.reportEvent(mockAction, "test event");

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(false));
        verify(mockAction, times(1)).getID();
    }

    @Test
    public void sessionStartIsReported() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);

        // when
        target.startSession();

        // then ensure session start has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void sessionStartIsReportedForDataCollectionLevel0() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OPT_IN_CRASHES));

        // when
        target.startSession();

        // then ensure session start has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void sessionStartIsReportedForDataCollectionLevel1() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OPT_IN_CRASHES));

        // when
        target.startSession();

        // then ensure session start has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void sessionStartIsReportedForDataCollectionLevel2() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OPT_IN_CRASHES));

        // when
        target.startSession();

        // then ensure session start has been serialized
        assertThat(target.isEmpty(), is(false));
    }

    @Test
    public void noSessionStartIsReportedIfBeaconConfigurationDisablesCapturing() {
        // given
        Beacon target = new Beacon(logger, new BeaconCacheImpl(logger), configuration, "127.0.0.1", threadIDProvider, timingProvider);
        target.setBeaconConfiguration(new BeaconConfiguration(0, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OPT_IN_CRASHES));

        // when
        target.startSession();

        // then ensure nothing has been serialized
        assertThat(target.isEmpty(), is(true));
    }
}
