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
import com.dynatrace.openkit.core.*;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.providers.DefaultTimingProvider;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class BeaconTest {

    private static final String APP_ID = "appID";
    private static final String APP_NAME = "appName";
    private static final int ACTION_ID = 17;
    private static final int SERVER_ID = 123;
    private static final long DEVICE_ID = 456;
    private static final long THREAD_ID = 111222333L;

    private Configuration configuration;
    private ThreadIDProvider threadIDProvider;

    private Logger logger;

    @Before
    public void setUp() {
        configuration = mock(Configuration.class);
        when(configuration.getApplicationID()).thenReturn(APP_ID);
        when(configuration.getApplicationName()).thenReturn(APP_NAME);
        when(configuration.getDevice()).thenReturn(new Device("", "", ""));
        when(configuration.getDeviceID()).thenReturn(DEVICE_ID);
        when(configuration.isCapture()).thenReturn(true);
        when(configuration.isCaptureErrors()).thenReturn(true);
        when(configuration.isCaptureCrashes()).thenReturn(true);
        when(configuration.getMaxBeaconSize()).thenReturn(30 * 1024); // 30kB

        HTTPClientConfiguration mockHTTPClientConfiguration = mock(HTTPClientConfiguration.class);
        when(mockHTTPClientConfiguration.getServerID()).thenReturn(SERVER_ID);
        when(configuration.getHttpClientConfig()).thenReturn(mockHTTPClientConfiguration);

        threadIDProvider = mock(ThreadIDProvider.class);
        when(threadIDProvider.getThreadID()).thenReturn(THREAD_ID);

        logger = mock(Logger.class);
    }

    @Test
    public void createIDs() {
        // create test environment
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());

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
        // create test environment
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new DefaultTimingProvider());

        long timestamp = beacon.getCurrentTimestamp();

        // verify that the two timestamps are closely enough
        assertThat(Math.abs(System.currentTimeMillis() - timestamp), is(lessThan(10L)));
    }

    @Test
    public void createSequenceNumbers() {
        // create test environment
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());

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
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);

        // when
        int sequenceNo = 42;
        String tag = beacon.createTag(action, sequenceNo);

        // then
        assertThat(tag, is(equalTo("MT_3_" + SERVER_ID + "_" + DEVICE_ID + "_0_" + APP_ID + "_" + ACTION_ID + "_"
                + THREAD_ID + "_" + sequenceNo)));
    }

    @Test
    public void addValidActionEvent() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
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
        assertThat(actions, is(equalTo(new String[] { "et=1&na=" + actionName + "&it=" + THREAD_ID + "&ca=" + ACTION_ID
                + "&pa=" + parentID + "&s0=0&t0=0&s1=0&t1=0" })));
    }

    @Test
    public void addEndSessionEvent() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        SessionImpl session = mock(SessionImpl.class);

        // when
        beacon.endSession(session);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=19&it=" + THREAD_ID + "&pa=0&s0=1&t0=0" })));
    }

    @Test
    public void reportValidValueInt() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "IntValue";
        int value = 42;

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=12&na=" + valueName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID
                + "&s0=1&t0=0&vl=" + String.valueOf(value) })));
    }

    @Test
    public void reportValidValueDouble() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "DoubleValue";
        double value = 3.1415;

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=13&na=" + valueName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID
                + "&s0=1&t0=0&vl=" + String.valueOf(value) })));
    }

    @Test
    public void reportValidValueString() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "StringValue";
        String value = "HelloWorld";

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] {
                "et=11&na=" + valueName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&vl=" + value })));
    }

    @Ignore
    @Test
    public void reportValueStringWithValueNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = "StringValue";
        String value = null;

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then (verify, that calling reportValue with a null value doesn't throw an exception but instead only triggers a warning)
        assertThat(events, emptyArray());
        verify(logger, times(1)).warning(anyString());
    }

    @Ignore
    @Test
    public void reportValueStringWithValueNullAndNameNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String valueName = null;
        String value = null;

        // when
        beacon.reportValue(action, valueName, value);
        String[] events = beacon.getEvents();

        // then (verify, that calling reportValue with a null name and null value does throw an exception)
        assertThat(events, emptyArray());
        verify(logger, times(1)).warning(anyString());
    }

    @Test
    public void reportValidEvent() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String eventName = "SomeEvent";

        // when
        beacon.reportEvent(action, eventName);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(
                new String[] { "et=10&na=" + eventName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0" })));
    }

    @Test
    public void reportEventWithNameNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String eventName = null;

        // when
        beacon.reportEvent(action, eventName);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=10&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0" })));
    }

    @Test
    public void reportError() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String errorName = "SomeEvent";
        int errorCode = -123;
        String reason = "SomeReason";

        // when
        beacon.reportError(action, errorName, errorCode, reason);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=40&na=" + errorName + "&it=" + THREAD_ID + "&pa=" + ACTION_ID
                + "&s0=1&t0=0&ev=" + errorCode + "&rs=" + reason })));
    }

    @Ignore
    @Test
    public void reportErrorNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        ActionImpl action = mock(ActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        String errorName = null;
        int errorCode = -123;
        String reason = null;

        // when
        beacon.reportError(action, errorName, errorCode, reason);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(
                new String[] { "et=40&&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=1&t0=0&ev=" + errorCode })));
    }

    @Test
    public void reportValidCrash() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String errorName = "SomeEvent";
        String reason = "SomeReason";
        String stacktrace = "SomeStacktrace";

        // when
        beacon.reportCrash(errorName, reason, stacktrace);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=50&na=" + errorName + "&it=" + THREAD_ID
                + "&pa=0&s0=1&t0=0&rs=" + reason + "&st=" + stacktrace })));
    }

    @Ignore
    @Test
    public void reportCrashWithDetailsNull() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String errorName = null;
        String reason = null;
        String stacktrace = null;

        // when
        beacon.reportCrash(errorName, reason, stacktrace);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=50&it=" + THREAD_ID + "&pa=0&s0=1&t0=0" })));
    }

    @Test
    public void addWebRequest() {
        // given
        final Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
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
        assertThat(events, is(equalTo(new String[] {
                "et=30&it=" + THREAD_ID + "&pa=" + ACTION_ID + "&s0=0&t0=0&s1=0&t1=0&bs=13&br=14&rc=15" })));
    }

    @Test
    public void addUserIdentifyEvent() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String userID = "myTestUser";

        // when
        beacon.identifyUser(userID);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=60&na=" + userID + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0" })));
    }

    @Test
    public void addUserIdentifyWithNullUserIDEvent() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String userID = null;

        // when
        beacon.identifyUser(userID);
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=60&it=" + THREAD_ID + "&pa=0&s0=1&t0=0" })));
    }

    @Test
    public void canAddSentBytesToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesSent = 12321;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID
                + "&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String.valueOf(bytesSent) })));
    }

    @Test
    public void canAddSentBytesValueZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesSent = 0;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID
                + "&pa=0&s0=1&t0=0&s1=2&t1=0&bs=" + String.valueOf(bytesSent) })));
    }

    @Test
    public void cannotAddSentBytesWithInvalidValueSmallerZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);

        // when
        webRequest.start().setBytesSent(-5).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events,
                is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0" })));
    }

    @Test
    public void canAddReceivedBytesToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesReceived = 12321;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID
                + "&pa=0&s0=1&t0=0&s1=2&t1=0&br=" + String.valueOf(bytesReceived) })));
    }

    @Test
    public void canAddReceivedBytesValueZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesReceived = 0;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events, is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID
                + "&pa=0&s0=1&t0=0&s1=2&t1=0&br=" + String.valueOf(bytesReceived) })));
    }

    @Test
    public void cannotAddReceivedBytesWithInvalidValueSmallerZeroToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);

        // when
        webRequest.start().setBytesReceived(-1).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events,
                is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0" })));
    }

    @Test
    public void canAddBothSentBytesAndReceivedBytesToWebRequestTracer() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        String testURL = "localhost";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(beacon, rootAction, testURL);
        int bytesReceived = 12321;
        int bytesSent = 123;

        // when
        webRequest.start().setBytesSent(bytesSent).setBytesReceived(bytesReceived).stop(); // stop will add the web request to the beacon
        String[] events = beacon.getEvents();

        // then
        assertThat(events,
                is(equalTo(new String[] { "et=30&na=" + testURL + "&it=" + THREAD_ID + "&pa=0&s0=1&t0=0&s1=2&t1=0&bs="
                        + String.valueOf(bytesSent) + "&br=" + String.valueOf(bytesReceived) })));
    }

    @Test
    public void canAddRootActionIfCaptureIsOn() {
        // given
        when(configuration.isCapture()).thenReturn(true);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        // when
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        beacon.addAction(rootAction);

        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(equalTo(
                new String[] { "et=1&na=" + actionName + "&it=" + THREAD_ID + "&ca=0&pa=0&s0=0&t0=0&s1=0&t1=0" })));
    }

    @Test
    public void cannotAddRootActionIfCaptureIsOff() {
        // given
        when(configuration.isCapture()).thenReturn(false);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        // when
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
        beacon.addAction(rootAction);

        String[] actions = beacon.getActions();

        // then
        assertThat(actions, is(arrayWithSize(0)));
    }

    @Test
    public void canHandleNoDataInBeaconSend() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
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
        String ipAddr = "127.0.0.1";
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, ipAddr, threadIDProvider,
                new NullTimeProvider());
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 200;
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class)))
                .thenReturn(new StatusResponse("", responseCode));
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        beacon.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = beacon.send(httpClientProvider);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddr), any(byte[].class));
    }

    @Test
    public void sendDataAndFakeErrorResponse() {
        // given
        String ipAddr = "127.0.0.1";
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, ipAddr, threadIDProvider,
                new NullTimeProvider());
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 418;
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class)))
                .thenReturn(new StatusResponse("", responseCode));
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        beacon.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = beacon.send(httpClientProvider);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddr), any(byte[].class));
    }

    @Test
    public void clearDataFromBeaconCache() {
        // given
        Beacon beacon = new Beacon(logger, new BeaconCacheImpl(), configuration, "127.0.0.1", threadIDProvider,
                new NullTimeProvider());
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

    private class NullTimeProvider extends DefaultTimingProvider {

        @Override
        public long provideTimestampInMilliseconds() {
            return 0;
        }
    }
}
