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

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfigurationUpdateCallback;
import com.dynatrace.openkit.core.objects.BaseActionImpl;
import com.dynatrace.openkit.core.objects.OpenKitComposite;
import com.dynatrace.openkit.core.objects.RootActionImpl;
import com.dynatrace.openkit.core.objects.WebRequestTracerBaseImpl;
import com.dynatrace.openkit.core.objects.WebRequestTracerStringURL;
import com.dynatrace.openkit.core.objects.WebRequestTracerURLConnection;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.RandomNumberGenerator;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class BeaconTest {

    private static final String APP_ID = "appID";
    private static final String APP_NAME = "appName";
    private static final int ACTION_ID = 17;
    private static final int SERVER_ID = 123;
    private static final long DEVICE_ID = 456;
    private static final int THREAD_ID = 1234567;
    private static final int SESSION_ID = 73;

    private BeaconConfiguration mockBeaconConfiguration;
    private OpenKitConfiguration mockOpenKitConfiguration;
    private PrivacyConfiguration mockPrivacyConfiguration;
    private ServerConfiguration mockServerConfiguration;
    private HTTPClientConfiguration mockHttpClientConfiguration;
    private AdditionalQueryParameters mockAdditionalParameters;

    private SessionIDProvider mockSessionIdProvider;
    private ThreadIDProvider mockThreadIDProvider;
    private TimingProvider mockTimingProvider;
    private RandomNumberGenerator mockRandom;
    private OpenKitComposite parentOpenKitObject;

    private Logger mockLogger;
    private BeaconCache mockBeaconCache;

    @Before
    public void setUp() {
        mockOpenKitConfiguration = mock(OpenKitConfiguration.class);
        when(mockOpenKitConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockOpenKitConfiguration.getPercentEncodedApplicationID()).thenReturn(APP_ID);
        when(mockOpenKitConfiguration.getApplicationName()).thenReturn(APP_NAME);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("");
        when(mockOpenKitConfiguration.getDeviceID()).thenReturn(DEVICE_ID);

        mockPrivacyConfiguration = mock(PrivacyConfiguration.class);
        when(mockPrivacyConfiguration.isDeviceIDSendingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isSessionReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isActionReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isEventReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isErrorReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isCrashReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isUserIdentificationAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.getDataCollectionLevel()).thenReturn(DataCollectionLevel.USER_BEHAVIOR);
        when(mockPrivacyConfiguration.getCrashReportingLevel()).thenReturn(CrashReportingLevel.OPT_IN_CRASHES);

        mockServerConfiguration = mock(ServerConfiguration.class);
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(true);
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(true);
        when(mockServerConfiguration.getServerID()).thenReturn(SERVER_ID);
        when(mockServerConfiguration.getBeaconSizeInBytes()).thenReturn(30 * 1024); // 30kB

        mockHttpClientConfiguration = mock(HTTPClientConfiguration.class);
        when(mockHttpClientConfiguration.getServerID()).thenReturn(SERVER_ID);

        mockBeaconConfiguration = mock(BeaconConfiguration.class);
        when(mockBeaconConfiguration.getOpenKitConfiguration()).thenReturn(mockOpenKitConfiguration);
        when(mockBeaconConfiguration.getPrivacyConfiguration()).thenReturn(mockPrivacyConfiguration);
        when(mockBeaconConfiguration.getServerConfiguration()).thenReturn(mockServerConfiguration);
        when(mockBeaconConfiguration.getHTTPClientConfiguration()).thenReturn(mockHttpClientConfiguration);

        mockAdditionalParameters = mock(AdditionalQueryParameters.class);

        mockSessionIdProvider = mock(SessionIDProvider.class);
        when(mockSessionIdProvider.getNextSessionID()).thenReturn(SESSION_ID);

        mockThreadIDProvider = mock(ThreadIDProvider.class);
        when(mockThreadIDProvider.getThreadID()).thenReturn(THREAD_ID);

        mockTimingProvider = mock(TimingProvider.class);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(0L);

        mockLogger = mock(Logger.class);
        mockBeaconCache = mock(BeaconCache.class);

        parentOpenKitObject = mock(OpenKitComposite.class);
        when(parentOpenKitObject.getActionID()).thenReturn(0);

        mockRandom = mock(RandomNumberGenerator.class);
    }

    @Test
    public void defaultBeaconConfigurationDoesNotDisableCapturing() {
        // given
        Beacon target = createBeacon().build();

        // then
        assertThat(target.isCaptureEnabled(), is(true));
    }

    @Test
    public void testCreateInstanceWithInvalidIpAddress() {
        // given, when
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        HTTPClient httpClient = mock(HTTPClient.class);

        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        String ipAddress = "invalid";
        Beacon target = createBeacon()
                .withIpAddress(ipAddress)
                .build();

        // then
        verify(mockLogger, times(1)).warning("Beacon: Client IP address validation failed: " + ipAddress);

        // and when
        when(mockBeaconCache.getNextBeaconChunk(anyInt(), anyString(), anyInt(), anyChar())).thenReturn("dummy");

        target.send(httpClientProvider, mockAdditionalParameters);

        // then
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient, times(1))
                .sendBeaconRequest(ipCaptor.capture(), any(byte[].class), eq(mockAdditionalParameters));

        String capturedIp = ipCaptor.getValue();
        assertThat(capturedIp, is(""));
    }

    @Test
    public void testCreateInstanceWithNullIpAddress() {
        // given, when
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        HTTPClient httpClient = mock(HTTPClient.class);

        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        Beacon target = createBeacon()
                .withIpAddress(null)
                .build();

        // then
        verify(mockLogger, times(0)).warning(any(String.class));

        // and when
        when(mockBeaconCache.getNextBeaconChunk(anyInt(), anyString(), anyInt(), anyChar())).thenReturn("dummy");

        target.send(httpClientProvider, mockAdditionalParameters);

        // then
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient, times(1))
                .sendBeaconRequest(ipCaptor.capture(), any(byte[].class), eq(mockAdditionalParameters));

        String capturedIp = ipCaptor.getValue();
        assertThat(capturedIp, is(""));
    }

    @Test
    public void createIDs() {
        // create test environment
        final Beacon beacon = createBeacon().build();

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
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(42L);
        Beacon beacon = createBeacon().build();

        // when obtaining the timestamp
        long timestamp = beacon.getCurrentTimestamp();

        // then verify
        assertThat(timestamp, is(42L));

        // verify called twice (once in Beacon's ctor) and once when invoking the call
        verify(mockTimingProvider, times(2)).provideTimestampInMilliseconds();
    }

    @Test
    public void createSequenceNumbers() {
        // create test environment
        final Beacon beacon = createBeacon().build();

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
        final Beacon beacon = createBeacon().build();

        // when
        int sequenceNo = 42;
        String tag = beacon.createTag(ACTION_ID, sequenceNo);

        // then
        assertThat(tag, is(
                "MT" +                      // tag prefix
                        "_" + ProtocolConstants.PROTOCOL_VERSION + // protocol version
                        "_" + SERVER_ID +           // server ID
                        "_" + DEVICE_ID +           // device ID
                        "_" + SESSION_ID +          // session number
                        "_" + APP_ID +              // application ID
                        "_" + ACTION_ID +           // parent action ID
                        "_" + THREAD_ID +           // thread ID
                        "_" + sequenceNo            // sequence number
        ));
    }

    @Test
    public void createWebRequestTagEncodesDeviceIDProperly() {
        // given
        long deviceId = -42;
        when(mockOpenKitConfiguration.getDeviceID()).thenReturn(deviceId);
        final Beacon beacon = createBeacon().build();

        // when
        int sequenceNo = 42;
        String tag = beacon.createTag(ACTION_ID, sequenceNo);

        // then
        assertThat(tag, is(
                "MT" +                      // tag prefix
                        "_" + ProtocolConstants.PROTOCOL_VERSION + // protocol version
                        "_" + SERVER_ID +           // server ID
                        "_" + deviceId +            // device ID percent encoded
                        "_" + SESSION_ID +          // session number
                        "_" + APP_ID +              // application ID
                        "_" + ACTION_ID +           // parent action ID
                        "_" + THREAD_ID +           // thread ID
                        "_" + sequenceNo            // sequence number
        ));

        // also ensure that the application ID is the encoded one
        verify(mockOpenKitConfiguration, times(1)).getPercentEncodedApplicationID();
    }

    @Test
    public void createTagDoesNotAppendSessionSequenceNumberForVisitStoreVersionsLowerTwo() {
        int tracerSeqNo = 42;
        int sessionSeqNo = 73;
        for (int version = 1; version > -2; version--) {
            // given
            when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(version);
            Beacon target = createBeacon().withSessionSequenceNumber(sessionSeqNo).build();

            // when
            String obtained = target.createTag(ACTION_ID, tracerSeqNo);

            // then
            assertThat(obtained, is(
                    "MT" +                      // tag prefix
                            "_" + ProtocolConstants.PROTOCOL_VERSION + // protocol version
                            "_" + SERVER_ID +           // server ID
                            "_" + DEVICE_ID +           // device ID percent encoded
                            "_" + SESSION_ID +          // session number
                            "_" + APP_ID +              // application ID
                            "_" + ACTION_ID +           // parent action ID
                            "_" + THREAD_ID +           // thread ID
                            "_" + tracerSeqNo           // sequence number
            ));
        }
    }

    @Test
    public void createTagAddsSessionSequenceNumberForVisitStoreVersionHigherOne() {
        int tracerSeqNo = 42;
        int sessionSeqNo = 73;
        for (int version = 2; version < 5; version++) {
            // given
            when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(version);
            Beacon target = createBeacon().withSessionSequenceNumber(sessionSeqNo).build();

            // when
            String obtained = target.createTag(ACTION_ID, tracerSeqNo);

            // then
            assertThat(obtained, is(
                    "MT" +                      // tag prefix
                            "_" + ProtocolConstants.PROTOCOL_VERSION + // protocol version
                            "_" + SERVER_ID +           // server ID
                            "_" + DEVICE_ID +           // device ID percent encoded
                            "_" + SESSION_ID +          // session number
                            "-" + sessionSeqNo +        // session sequence number
                            "_" + APP_ID +              // application ID
                            "_" + ACTION_ID +           // parent action ID
                            "_" + THREAD_ID +           // thread ID
                            "_" + tracerSeqNo           // sequence number
            ));
        }
    }

    @Test
    public void addValidActionEvent() {
        // given
        final Beacon beacon = createBeacon().build();
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        int parentID = 13;
        when(action.getParentID()).thenReturn(parentID);
        String actionName = "MyAction";
        when(action.getName()).thenReturn(actionName);

        // when
        beacon.addAction(action);

        // then
        verify(mockBeaconCache, times(1)).addActionData(
                SESSION_ID,                     // session number
                0,                              // action start time
                "et=1&" +                       // event type
                        "na=" + actionName + "&" +      // action name
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "ca=" + ACTION_ID + "&" +       // action ID
                        "pa=" + parentID + "&" +        // parent action ID
                        "s0=0&" +                       // action start sequence number
                        "t0=0&" +                       // action start time (relative to session start)
                        "s1=0&" +                       // action end sequence number
                        "t1=0"                          // action duration (time from action start to end)
        );
    }

    @Test
    public void addEndSessionEvent() {
        // given
        final Beacon beacon = createBeacon().build();

        // when
        beacon.endSession();

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // session end time
                "et=19&" +                      // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=0&" +                       // parent action
                        "s0=1&" +                       // end session sequence number
                        "t0=0"                          // session end time
        );
    }

    @Test
    public void reportValidValueInt() {
        // given
        final Beacon beacon = createBeacon().build();
        String valueName = "IntValue";
        int value = 42;

        // when
        beacon.reportValue(ACTION_ID, valueName, value);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event time
                "et=12&" +                      // event type
                        "na=" + valueName + "&" +       // name of reported value
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported value event
                        "t0=0&" +                       // event time since session start
                        "vl=" + value                   // reported value
        );
    }

    @Test
    public void reportValidValueDouble() {
        // given
        final Beacon beacon = createBeacon().build();
        String valueName = "DoubleValue";
        double value = 3.1415;

        // when
        beacon.reportValue(ACTION_ID, valueName, value);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event timestamp
                "et=13&" +                      // event type
                        "na=" + valueName + "&" +       // name of reported value
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported value event
                        "t0=0&" +                       // event time since session start
                        "vl=" + value                   // reported value
        );
    }

    @Test
    public void reportValidValueString() {
        // given
        final Beacon beacon = createBeacon().build();
        String valueName = "StringValue";
        String value = "HelloWorld";

        // when
        beacon.reportValue(ACTION_ID, valueName, value);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event timestamp
                "et=11&" +                      // event type
                        "na=" + valueName + "&" +       // name of reported value
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported value
                        "t0=0&" +                       // event time since session start
                        "vl=" + value                   // reported value
        );
    }

    @Test
    public void reportValueStringWithValueNull() {
        // given
        final Beacon beacon = createBeacon().build();
        String valueName = "StringValue";

        // when
        beacon.reportValue(ACTION_ID, valueName, null);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event timestamp
                "et=11&" +                      // event type
                        "na=" + valueName + "&" +       // name of reported value
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported value
                        "t0=0"                          // event time since session start
        );
    }

    @Test
    public void reportValueStringWithValueNullAndNameNull() {
        // given
        final Beacon beacon = createBeacon().build();

        // when
        beacon.reportValue(ACTION_ID, null, null);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event timestamp
                "et=11&" +                      // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported value
                        "t0=0"                          // event time since session start
        );
    }

    @Test
    public void reportValidEvent() {
        // given
        final Beacon beacon = createBeacon().build();
        String eventName = "SomeEvent";

        // when
        beacon.reportEvent(ACTION_ID, eventName);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event timestamp
                "et=10&" +                      // event type
                        "na=" + eventName + "&" +       // name of event
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported event
                        "t0=0"                          // event time since session start
        );
    }

    @Test
    public void reportEventWithNameNull() {
        // given
        final Beacon beacon = createBeacon().build();

        // when
        beacon.reportEvent(ACTION_ID, null);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                              // event timestamp
                "et=10&" +                      // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported event
                        "t0=0"                          // event time since session start
        );
    }

    @Test
    public void reportError() {
        // given
        final Beacon beacon = createBeacon().build();
        String errorName = "SomeEvent";
        int errorCode = -123;
        String reason = "SomeReason";

        // when
        beacon.reportError(ACTION_ID, errorName, errorCode, reason);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                    // error event timestamp
                "et=40&" +                // event type
                        "na=" + errorName + "&" +       // name of error event
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of error event
                        "t0=0&" +                       // timestamp of error event since session start
                        "ev=" + errorCode + "&" +       // reported error value
                        "rs=" + reason + "&" +          // reported reason
                        "tt=c"                          // error technology type
        );
    }

    @Test
    public void reportErrorWithoutName() {
        // given
        final Beacon beacon = createBeacon().build();
        int errorCode = -123;

        // when
        beacon.reportError(ACTION_ID, null, errorCode, null);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                     // session number
                0,                    // error event timestamp
                "et=40&" +                // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of reported event
                        "t0=0&" +                       // timestamp of error event since session start
                        "ev=" + errorCode + "&" +       // reported error value
                        "tt=c"                          // error technology type
        );
    }

    @Test
    public void reportValidCrash() {
        // given
        final Beacon beacon = createBeacon().build();
        String errorName = "SomeEvent";
        String reason = "SomeReason";
        String stacktrace = "SomeStacktrace";

        // when
        beacon.reportCrash(errorName, reason, stacktrace);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                // crash event timestamp
                "et=50&" +            // event type
                        "na=" + errorName + "&" +   // reported crash name
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // sequence number of reported crash
                        "t0=0&" +                   // timestamp of crash since session start
                        "rs=" + reason + "&" +      // reported reason
                        "st=" + stacktrace + "&" +  // reported stacktrace
                        "tt=c"                      // crash technology type
        );
    }

    @Test
    public void reportCrashWithDetailsNull() {
        // given
        final Beacon beacon = createBeacon().build();
        String errorName = "errorName";

        // when
        beacon.reportCrash(errorName, null, null);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                // crash event timestamp
                "et=50&" +            // event type
                        "na=" + errorName + "&" +   // reported crash name
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // sequence number of reported crash
                        "t0=0&" +                   // timestamp of crash since session start
                        "tt=c"                      // crash technology type
        );
    }

    @Test
    public void addWebRequest() {
        // given
        final Beacon beacon = createBeacon().build();
        WebRequestTracerURLConnection webRequestTracer = mock(WebRequestTracerURLConnection.class);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        // when
        beacon.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=" + ACTION_ID + "&" +   // parent action ID
                        "s0=0&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start time (since session start)
                        "s1=0&" +                   // web request end sequence number
                        "t1=0&" +                   // web request end time (relative to start time)
                        "bs=13&" +                  // number of bytes sent
                        "br=14&" +                  // number of bytes received
                        "rc=15"                     // response code
        );
    }

    @Test
    public void addUserIdentifyEvent() {
        // given
        Beacon beacon = createBeacon().build();
        String userID = "myTestUser";

        // when
        beacon.identifyUser(userID);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // identify user event timestamp
                "et=60&" +                  // event type
                        "na=" + userID + "&" +      // reported user ID
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // identify user sequence number
                        "t0=0"                      // event timestamp since session start
        );
    }

    @Test
    public void addUserIdentifyWithNullUserIDEvent() {
        // given
        Beacon beacon = createBeacon().build();

        // when
        beacon.identifyUser(null);

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // identify user event timestamp
                "et=60&" +                  // event type
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // identify user sequence number
                        "t0=0"                      // event timestamp since session start
        );
    }

    @Test
    public void canAddSentBytesToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = createBeacon().build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);
        int bytesSent = 12321;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(-1); // stop will add the web request to the beacon

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0&" +                   // web request end timestamp (relative to start time)
                        "bs=" + bytesSent           // number bytes sent
        );
    }

    @Test
    public void canAddSentBytesValueZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = createBeacon().build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);
        int bytesSent = 0;

        // when
        webRequest.start().setBytesSent(bytesSent).stop(-1); // stop will add the web request to the beacon

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0&" +                   // web request end timestamp (relative to start time)
                        "bs=" + bytesSent           // number bytes sent
        );
    }

    @Test
    public void cannotAddSentBytesWithInvalidValueSmallerZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        BeaconCache beaconCache = mock(BeaconCache.class);
        Beacon beacon = createBeacon().with(beaconCache).build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);

        // when
        webRequest.start().setBytesSent(-5).stop(-1); // stop will add the web request to the beacon

        // then
        verify(beaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0"                      // web request end timestamp (relative to start time)
        );
    }

    @Test
    public void canAddReceivedBytesToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = createBeacon().build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);
        int bytesReceived = 12321;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(-1); // stop will add the web request to the beacon

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0&" +                   // web request end timestamp (relative to start time)
                        "br=" + bytesReceived       // number of received bytes
        );
    }

    @Test
    public void canAddReceivedBytesValueZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = createBeacon().build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);
        int bytesReceived = 0;

        // when
        webRequest.start().setBytesReceived(bytesReceived).stop(-1); // stop will add the web request to the beacon

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0&" +                   // web request end timestamp (relative to start time)
                        "br=" + bytesReceived       // number of received bytes
        );
    }

    @Test
    public void cannotAddReceivedBytesWithInvalidValueSmallerZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        BeaconCache beaconCache = mock(BeaconCache.class);
        Beacon beacon = createBeacon().with(beaconCache).build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);

        // when
        webRequest.start().setBytesReceived(-1).stop(-1); // stop will add the web request to the beacon

        // then
        verify(beaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0"                      // web request end timestamp (relative to start time)
        );
    }

    @Test
    public void canAddBothSentBytesAndReceivedBytesToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        Beacon beacon = createBeacon().build();
        String testURL = "https://localhost";
        WebRequestTracerStringURL webRequest = new WebRequestTracerStringURL(mockLogger, parentOpenKitObject, beacon, testURL);
        int bytesReceived = 12321;
        int bytesSent = 123;

        // when
        webRequest.start()
                .setBytesSent(bytesSent)
                .setBytesReceived(bytesReceived)
                .stop(-1); // stop will add the web request to the beacon

        // then
        verify(mockBeaconCache, times(1)).addEventData(
                SESSION_ID,                 // session number
                0,                          // web request start timestamp
                "et=30&" +                  // event type
                        "na=" + URLEncoder.encode(testURL, "UTF-8") + "&" + // reported URL
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // web request start sequence number
                        "t0=0&" +                   // web request start timestamp (relative to session start)
                        "s1=2&" +                   // web request end sequence number
                        "t1=0&" +                   // web request end timestamp (relative to start time)
                        "bs=" + bytesSent + "&" +   // number of sent bytes
                        "br=" + bytesReceived       // number of received bytes
        );
    }

    @Test
    public void canAddRootActionIfDataSendingIsAllowed() {
        // given
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(true);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        Beacon beacon = createBeacon().build();

        // when
        beacon.addAction(rootAction);

        // then
        verify(mockBeaconCache, times(1)).addActionData(
                SESSION_ID,                 // session number
                0,                          // action start timestamp
                "et=1&" +                   // event type
                        "na=" + actionName + "&" +  // action name
                        "it=" + THREAD_ID + "&" +   // thread Id
                        "ca=0&" +                   // action ID
                        "pa=0&" +                   // parent action ID
                        "s0=0&" +                   // action start sequence number
                        "t0=0&" +                   // action start time (relative to session start)
                        "s1=0&" +                   // action end sequence number
                        "t1=0"                      // action end time (relative to start time)
        );
    }

    @Test
    public void cannotAddRootActionIfCapturingIsDisabled() {
        // given
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);
        String actionName = "rootAction";
        RootActionImpl rootAction = mock(RootActionImpl.class);
        when(rootAction.getName()).thenReturn(actionName);

        Beacon beacon = createBeacon().build();

        // when
        beacon.addAction(rootAction);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void canHandleNoDataInBeaconSend() {
        // given
        Beacon beacon = createBeacon().build();
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient mockClient = mock(HTTPClient.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(mockClient);

        // when
        StatusResponse response = beacon.send(httpClientProvider, mockAdditionalParameters);

        // then (verify, that null is returned as no data was sent)
        assertThat(response, nullValue());
    }

    @Test
    public void sendValidData() {
        // given
        String ipAddress = "127.0.0.1";
        BeaconCache beaconCache = new BeaconCacheImpl(mockLogger);
        Beacon beacon = createBeacon()
                .withIpAddress(ipAddress)
                .with(beaconCache)
                .build();
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 200;
        StatusResponse successResponse = StatusResponse.createSuccessResponse(
                mockLogger,
                ResponseAttributesImpl.withJsonDefaults().build(),
                responseCode,
                Collections.<String, List<String>>emptyMap()
        );
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class), any(AdditionalQueryParameters.class)))
                .thenReturn(successResponse);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        beacon.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = beacon.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class), eq(mockAdditionalParameters));
    }

    @Test
    public void sendDataAndFakeErrorResponse() {
        // given
        String ipAddress = "127.0.0.1";
        BeaconCache beaconCache = new BeaconCacheImpl(mockLogger);
        Beacon beacon = createBeacon()
                .withIpAddress(ipAddress)
                .with(beaconCache)
                .build();
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 418;
        StatusResponse errorResponse = StatusResponse.createErrorResponse(mockLogger, responseCode);
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class), any(AdditionalQueryParameters.class)))
                .thenReturn(errorResponse);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        beacon.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = beacon.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class), eq(mockAdditionalParameters));
    }

    @Test
    public void sendCatchesUnsupportedEncodingException() throws Exception {
        // given
        String beaconChunk = "some beacon string";
        when(mockBeaconCache.getNextBeaconChunk(anyInt(), anyString(), anyInt(), anyChar())).thenReturn(beaconChunk);

        HTTPClient httpClient = mock(HTTPClient.class);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        final UnsupportedEncodingException exception = new UnsupportedEncodingException();

        BeaconInitializer beaconInitializer = mock(BeaconInitializer.class);
        when(beaconInitializer.getLogger()).thenReturn(mockLogger);
        when(beaconInitializer.getBeaconCache()).thenReturn(mockBeaconCache);
        when(beaconInitializer.getClientIpAddress()).thenReturn("127.0.0.1");
        when(beaconInitializer.getSessionIdProvider()).thenReturn(mockSessionIdProvider);
        when(beaconInitializer.getThreadIdProvider()).thenReturn(mockThreadIDProvider);
        when(beaconInitializer.getTimingProvider()).thenReturn(mockTimingProvider);
        when(beaconInitializer.getRandomNumberGenerator()).thenReturn(mockRandom);

        Beacon target = new Beacon(beaconInitializer, mockBeaconConfiguration) {
            @Override
            protected byte[] encodeBeaconChunk(String chunkToEncode) throws UnsupportedEncodingException {
                throw exception;
            }
        };

        // when
        StatusResponse obtained = target.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(obtained, is(nullValue()));
        verify(mockBeaconCache, times(1)).resetChunkedData(SESSION_ID);
        verify(mockLogger, times(1)).error(": Required charset \"UTF-8\" is not supported.", exception);
    }

    @Test
    public void beaconDataPrefix() {
        // given
        int sessionSequence = 1213;
        int visitStoreVersion = 9;
        String appVersion = "1111";
        String ipAddress = "192.168.0.1";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("system");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("manufacturer");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("model");
        when(mockBeaconCache.getNextBeaconChunk(anyInt(), anyString(), anyInt(), anyChar())).thenReturn(null);
        when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        Beacon target = createBeacon().withIpAddress(ipAddress).withSessionSequenceNumber(sessionSequence).build();

        // when
        target.send(mock(HTTPClientProvider.class), null);

        // then
        String expectedPrefix = "vv=" + ProtocolConstants.PROTOCOL_VERSION +
                "&va=" + ProtocolConstants.OPENKIT_VERSION +
                "&ap=" + APP_ID +
                "&an=" + APP_NAME +
                "&vn=" + appVersion +
                "&pt=" + ProtocolConstants.PLATFORM_TYPE_OPENKIT +
                "&tt=" + ProtocolConstants.AGENT_TECHNOLOGY_TYPE +
                "&vi=" + DEVICE_ID +
                "&sn=" + SESSION_ID +
                "&ss=" + sessionSequence +
                "&ip=" + ipAddress +
                "&os=system" +
                "&mf=manufacturer" +
                "&md=model" +
                "&dl=2" +
                "&cl=2" +
                "&vs=" + visitStoreVersion +
                "&tx=0" +
                "&tv=0" +
                "&mp=0";

        verify(mockBeaconCache, times(1)).getNextBeaconChunk(eq(SESSION_ID), eq(expectedPrefix), anyInt(), anyChar());
    }

    @Test
    public void clearDataFromBeaconCache() {
        // given
        BeaconCacheImpl beaconCache = new BeaconCacheImpl(mockLogger);
        Beacon beacon = createBeacon().with(beaconCache).build();
        // add various data
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        beacon.addAction(action);
        beacon.reportValue(ACTION_ID, "IntValue", 42);
        beacon.reportValue(ACTION_ID, "DoubleValue", 3.1415);
        beacon.reportValue(ACTION_ID, "StringValue", "HelloWorld");
        beacon.reportEvent(ACTION_ID, "SomeEvent");
        beacon.reportError(ACTION_ID, "SomeError", -123, "SomeReason");
        beacon.reportCrash("SomeCrash", "SomeReason", "SomeStacktrace");
        beacon.endSession();

        // when
        beacon.clearData();

        // then (verify, all data is cleared)
        String[] events = beaconCache.getEvents(beacon.getSessionNumber());
        assertThat(events, emptyArray());
        String[] actions = beaconCache.getActions(beacon.getSessionNumber());
        assertThat(actions, emptyArray());
        assertThat(beacon.isEmpty(), is(true));
    }

    @Test
    public void noSessionIsAddedIfCapturingDisabled() {
        // given
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);
        Beacon target = createBeacon().build();

        // when
        target.endSession();

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noActionIsAddedIfCapturingIsDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);

        // when
        target.addAction(action);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noIntValueIsReportedIfCapturingIsDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        int intValue = 42;

        // when
        target.reportValue(ACTION_ID, "intValue", intValue);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noDoubleValueIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        double doubleValue = Math.E;

        // when
        target.reportValue(ACTION_ID, "doubleValue", doubleValue);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noStringValueIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        String stringValue = "Write once, debug everywhere";

        // when
        target.reportValue(ACTION_ID, "doubleValue", stringValue);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noEventIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        // when
        target.reportEvent(ACTION_ID, "Event name");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noEventIsReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportEvent(ACTION_ID, "Event name");

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noErrorIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "Error name", 123, "The reason for this error");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noErrorIsReportedIfSendingErrorDataDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "Error name", 123, "The reason for this error");

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noCrashIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        // when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noCrashIsReportedIfSendingCrashDataDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(false);

        // when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noWebRequestIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
        verifyZeroInteractions(webRequestTracer);
    }

    @Test
    public void noUserIdentificationIsReportedIfCapturingDisabled() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);

        // when
        target.identifyUser("jane.doe@acme.com");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noWebRequestIsReportedIfWebRequestTracingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(false);
        WebRequestTracerURLConnection mockWebRequestTracer = mock(WebRequestTracerURLConnection.class);
        //when
        target.addWebRequest(ACTION_ID, mockWebRequestTracer);

        //then
        //verify nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
        verifyZeroInteractions(mockWebRequestTracer);
    }

    @Test
    public void webRequestIsReportedIfWebRequestTracingAllowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(true);
        WebRequestTracerURLConnection mockWebRequestTracer = mock(WebRequestTracerURLConnection.class);

        //when
        target.addWebRequest(ACTION_ID, mockWebRequestTracer);

        //then
        verify(mockWebRequestTracer, times(1)).getBytesReceived();
        verify(mockWebRequestTracer, times(1)).getBytesSent();
        verify(mockWebRequestTracer, times(1)).getResponseCode();

        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void beaconReturnsEmptyTagIfWebRequestTracingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(false);

        //when
        String returnedTag = target.createTag(ACTION_ID, 1);

        //then
        assertThat(returnedTag, isEmptyString());
    }

    @Test
    public void beaconReturnsValidTagIfWebRequestTracingIsAllowed() {
        //given
        int sequenceNo = 1;
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(true);

        //when
        String returnedTag = target.createTag(ACTION_ID, sequenceNo);

        //then
        assertThat(returnedTag, is(
                "MT" +
                        "_" + ProtocolConstants.PROTOCOL_VERSION +
                        "_" + SERVER_ID +
                        "_" + DEVICE_ID +
                        "_" + SESSION_ID +
                        "_" + APP_ID +
                        "_" + ACTION_ID +
                        "_" + THREAD_ID +
                        "_" + sequenceNo
        ));
    }

    @Test
    public void beaconReturnsValidTagWithSessionNumberIfSessionNumberReportingAllowed() {
        //given
        int sequenceNo = 1;
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(true);
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(true);

        Beacon target = createBeacon().build();

        //when
        String returnedTag = target.createTag(ACTION_ID, sequenceNo);

        //then
        assertThat(returnedTag, is(
                "MT" +
                        "_" + ProtocolConstants.PROTOCOL_VERSION +
                        "_" + SERVER_ID +
                        "_" + DEVICE_ID +
                        "_" + SESSION_ID +
                        "_" + APP_ID +
                        "_" + ACTION_ID +
                        "_" + THREAD_ID +
                        "_" + sequenceNo
        ));
    }

    @Test
    public void beaconReturnsValidTagWithSessionNumberOneIfSessionNumberReportingDisallowed() {
        //given
        int sequenceNo = 1;
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(false);
        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(true);

        Beacon target = createBeacon().build();

        //when
        String returnedTag = target.createTag(ACTION_ID, sequenceNo);

        //then
        assertThat(returnedTag, is(
                "MT" +
                        "_" + ProtocolConstants.PROTOCOL_VERSION +
                        "_" + SERVER_ID +
                        "_" + DEVICE_ID +
                        "_1" +                      // session number must always be 1
                        "_" + APP_ID +
                        "_" + ACTION_ID +
                        "_" + THREAD_ID +
                        "_" + sequenceNo
        ));
    }

    @Test
    public void cannotIdentifyUserIfUserIdentificationDisabled() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isUserIdentificationAllowed()).thenReturn(false);

        //when
        target.identifyUser("jane@doe.com");

        //then
        //verify nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void canIdentifyUserIfUserIdentificationIsAllowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isUserIdentificationAllowed()).thenReturn(true);

        //when
        target.identifyUser("jane@doe.com");

        //then
        //verify user tag has been serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void deviceIDIsRandomizedIfDeviceIdSendingDisallowed() {
        // given
        when(mockPrivacyConfiguration.isDeviceIDSendingAllowed()).thenReturn(false);
        RandomNumberGenerator mockRandom = mock(RandomNumberGenerator.class);

        // when
        createBeacon().with(mockRandom).build();

        // then verify that the device id is not taken from the configuration
        // this means it must have been generated randomly
        verify(mockOpenKitConfiguration, times(0)).getDeviceID();
        verify(mockRandom, times(1)).nextPositiveLong();
    }

    @Test
    public void givenDeviceIDIsUsedIfDeviceIdSendingIsAllowed() {
        long testDeviceId = 1338;
        //given
        when(mockPrivacyConfiguration.isDeviceIDSendingAllowed()).thenReturn(true);
        when(mockOpenKitConfiguration.getDeviceID()).thenReturn(testDeviceId);
        RandomNumberGenerator mockRandom = mock(RandomNumberGenerator.class);

        //when
        Beacon target = createBeacon().with(mockRandom).build();
        long obtained = target.getDeviceID();

        //then verify that device id is taken from configuration
        verify(mockOpenKitConfiguration, times(1)).getDeviceID();
        verifyNoMoreInteractions(mockRandom);
        assertThat(obtained, is(testDeviceId));
    }

    @Test
    public void sessionIDIsAlwaysValueOneIfSessionNumberReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(false);

        //when
        int sessionNumber = target.getSessionNumber();

        //then
        assertThat(sessionNumber, is(1));
    }

    @Test
    public void sessionIDIsValueFromSessionIDIfSessionNumberReportingAllowed() {
        // given
        final int SESSION_ID = 1234;
        when(mockSessionIdProvider.getNextSessionID()).thenReturn(SESSION_ID);
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(true);

        Beacon target = createBeacon().build();

        //when
        int sessionNumber = target.getSessionNumber();

        //then
        assertThat(sessionNumber, is(SESSION_ID));
    }

    @Test
    public void reportCrashDoesNotReportIfCrashReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isCrashReportingAllowed()).thenReturn(false);

        //when
        target.reportCrash("OutOfMemory exception", "insufficient memory", "stacktrace:123");

        //then
        verify(mockTimingProvider, times(1)).provideTimestampInMilliseconds();
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashDoesReportIfCrashReportingAllowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isCrashReportingAllowed()).thenReturn(true);

        //when
        target.reportCrash("OutOfMemory exception", "insufficient memory", "stacktrace:123");

        //then
        verify(mockTimingProvider, times(2)).provideTimestampInMilliseconds();
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void actionNotReportedIfActionReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isActionReportingAllowed()).thenReturn(false);
        BaseActionImpl action = mock(BaseActionImpl.class);

        //when
        target.addAction(action);

        //then
        //verify action has not been serialized
        verifyZeroInteractions(action);
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void actionNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);
        BaseActionImpl action = mock(BaseActionImpl.class);

        // when
        target.addAction(action);

        // then
        verifyZeroInteractions(action);
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void actionReportedIfActionReportingAllowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isActionReportingAllowed()).thenReturn(true);
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);

        //when
        target.addAction(action);

        //then
        //verify action has been serialized
        verify(action, times(1)).getID();
        verify(mockBeaconCache, times(1)).addActionData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void sessionNotReportedIfSessionReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isSessionReportingAllowed()).thenReturn(false);

        //when
        target.endSession();

        //then
        //verify session has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sessionNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.endSession();

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sessionReportedIfSessionReportingAllowed() {
        //given
        when(mockPrivacyConfiguration.isSessionReportingAllowed()).thenReturn(true);
        Beacon target = createBeacon().build();

        //when
        target.endSession();

        //then
        //verify serialized session get added to beacon
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void errorNotReportedIfErrorReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isErrorReportingAllowed()).thenReturn(false);

        //when
        target.reportError(ACTION_ID, "DivByZeroError", 127, "out of math");

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorReportedIfErrorReportingAllowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isErrorReportingAllowed()).thenReturn(true);

        //when
        target.reportError(ACTION_ID, "DivByZeroError", 127, "out of math");

        //then
        //verify error has been serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void IntValueIsNotReportedIfReportValueDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 123);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void IntValueNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 123);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void IntValueIsReportedIfReportValueAllowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(true);

        // when
        target.reportValue(ACTION_ID, "testValue", 123);

        // then ensure that error was serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }


    @Test
    public void DoubleValueIsNotReportedIfReportValueDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 2.71);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void DoubleValueIsNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 2.71);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void DoubleValueIsReportedIfValueReportingAllowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(true);

        // when
        target.reportValue(ACTION_ID, "test value", 2.71);

        // then ensure that error was serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void StringValueIsNotReportedIfValueReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", "test data");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void StringValueIsNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", "test data");

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void StringValueIsReportedIfValueReportingAllowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(true);

        // when
        target.reportValue(ACTION_ID, "test value", "test data");

        // then ensure that error was serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void NamedEventIsNotReportedIfEventReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isEventReportingAllowed()).thenReturn(false);

        // when
        target.reportEvent(ACTION_ID, "test event");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void NamedEventIsReportedIfEventReportingAllowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isEventReportingAllowed()).thenReturn(true);

        // when
        target.reportEvent(ACTION_ID, "test event");

        // then ensure that error was serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void sessionStartIsReported() {
        // given
        Beacon target = createBeacon().build();

        // when
        target.startSession();

        // then ensure session start has been serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
    }

    @Test
    public void sessionStartIsReportedRegardlessOfPrivacyConfiguration() {
        // given
        Beacon target = createBeacon().build();
        reset(mockPrivacyConfiguration);

        // when
        target.startSession();

        // then ensure session start has been serialized
        verify(mockBeaconCache, times(1)).addEventData(anyInt(), anyLong(), anyString());
        verifyNoMoreInteractions(mockPrivacyConfiguration);
    }

    @Test
    public void noSessionStartIsReportedIfCapturingDisabled() {
        // given
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);
        Beacon target = createBeacon().build();

        // when
        target.startSession();

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void updateServerConfigurationDelegatesToBeaconConfig() {
        // given
        Beacon target = createBeacon().build();
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        reset(mockBeaconConfiguration);

        // when
        target.updateServerConfiguration(serverConfig);

        // then
        verify(mockBeaconConfiguration, times(1)).updateServerConfiguration(serverConfig);
        verifyNoMoreInteractions(mockBeaconConfiguration);
        verifyZeroInteractions(serverConfig);
    }

    @Test
    public void isServerConfigurationSetDelegatesToBeaconConfig() {
        // given
        Beacon target = createBeacon().build();
        reset(mockBeaconConfiguration);

        // when
        when(mockBeaconConfiguration.isServerConfigurationSet()).thenReturn(false);
        boolean obtained = target.isServerConfigurationSet();

        // then
        assertThat(obtained, is(false));

        // and when
        when(mockBeaconConfiguration.isServerConfigurationSet()).thenReturn(true);
        obtained = target.isServerConfigurationSet();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void isCaptureEnabledReturnsValueFromServerConfig() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(false);
        boolean obtained = target.isCaptureEnabled();

        // then
        assertThat(obtained, is(false));

        // and when
        when(mockServerConfiguration.isCaptureEnabled()).thenReturn(true);
        obtained = target.isCaptureEnabled();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void enableCaptureDelegatesToBeaconConfig() {
        // given
        Beacon target = createBeacon().build();
        reset(mockBeaconConfiguration);

        // when
        target.enableCapture();

        // then
        verify(mockBeaconConfiguration, times(1)).enableCapture();
        verifyNoMoreInteractions(mockBeaconConfiguration);
    }

    @Test
    public void disableCaptureDelegatesToBeaconConfig() {
        // given
        Beacon target = createBeacon().build();
        reset(mockBeaconConfiguration);

        // when
        target.disableCapture();

        // then
        verify(mockBeaconConfiguration, times(1)).disableCapture();
        verifyNoMoreInteractions(mockBeaconConfiguration);
    }

    @Test
    public void setServerConfigUpdateCallbackDelegatesToBeaconConfig() {
        // given
        ServerConfigurationUpdateCallback callback = mock(ServerConfigurationUpdateCallback.class);
        Beacon target = createBeacon().build();

        // when
        target.setServerConfigurationUpdateCallback(callback);

        // then
        verify(mockBeaconConfiguration, times(1)).setServerConfigurationUpdateCallback(callback);
    }

    private BeaconBuilder createBeacon() {
        BeaconBuilder builder = new BeaconBuilder();
        builder.logger = mockLogger;
        builder.beaconCache = mockBeaconCache;
        builder.configuration = mockBeaconConfiguration;
        builder.ipAddress = "127.0.0.1";
        builder.sessionIdProvider = mockSessionIdProvider;
        builder.threadIdProvider = mockThreadIDProvider;
        builder.timingProvider = mockTimingProvider;
        builder.random = mockRandom;

        return builder;
    }

    private static class BeaconBuilder {
        private Logger logger;
        private BeaconCache beaconCache;
        private BeaconConfiguration configuration;
        private String ipAddress;
        private SessionIDProvider sessionIdProvider;
        private ThreadIDProvider threadIdProvider;
        private TimingProvider timingProvider;
        private RandomNumberGenerator random;
        private int sessionSequenceNumber;

        private BeaconBuilder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        private BeaconBuilder with(BeaconCache beaconCache) {
            this.beaconCache = beaconCache;
            return this;
        }

        private BeaconBuilder with(BeaconConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        private BeaconBuilder with(RandomNumberGenerator random) {
            this.random = random;
            return this;
        }

        private BeaconBuilder withSessionSequenceNumber(int sessionSequenceNumber) {
            this.sessionSequenceNumber = sessionSequenceNumber;
            return this;
        }

        private Beacon build() {
            BeaconInitializer beaconInitializer = mock(BeaconInitializer.class);
            when(beaconInitializer.getLogger()).thenReturn(logger);
            when(beaconInitializer.getBeaconCache()).thenReturn(beaconCache);
            when(beaconInitializer.getClientIpAddress()).thenReturn(ipAddress);
            when(beaconInitializer.getSessionIdProvider()).thenReturn(sessionIdProvider);
            when(beaconInitializer.getSessionSequenceNumber()).thenReturn(sessionSequenceNumber);
            when(beaconInitializer.getThreadIdProvider()).thenReturn(threadIdProvider);
            when(beaconInitializer.getTimingProvider()).thenReturn(timingProvider);
            when(beaconInitializer.getRandomNumberGenerator()).thenReturn(random);

            return new Beacon(beaconInitializer, configuration);
        }
    }
}
