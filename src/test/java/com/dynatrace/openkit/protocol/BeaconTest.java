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

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.api.ConnectionType;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.caching.BeaconKey;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfigurationUpdateCallback;
import com.dynatrace.openkit.core.objects.BaseActionImpl;
import com.dynatrace.openkit.core.objects.EventPayloadAttributes;
import com.dynatrace.openkit.core.objects.OpenKitComposite;
import com.dynatrace.openkit.core.objects.SupplementaryBasicData;
import com.dynatrace.openkit.core.objects.WebRequestTracerBaseImpl;
import com.dynatrace.openkit.core.util.CrashFormatter;
import com.dynatrace.openkit.core.util.PercentEncoder;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.RandomNumberGenerator;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import com.dynatrace.openkit.util.json.objects.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
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
    private static final int ACTION_ID = 17;
    private static final int SERVER_ID = 123;
    private static final long DEVICE_ID = 456;
    private static final int THREAD_ID = 1234567;
    private static final int SESSION_ID = 73;
    private static final int SESSION_SEQ_NO = 13;

    private static final String EVENT_PAYLOAD_APPLICATION_ID = "dt.rum.application.id";
    private static final String EVENT_PAYLOAD_INSTANCE_ID = "dt.rum.instance.id";
    private static final String EVENT_PAYLOAD_SESSION_ID = "dt.rum.sid";

    private BeaconConfiguration mockBeaconConfiguration;
    private OpenKitConfiguration mockOpenKitConfiguration;
    private PrivacyConfiguration mockPrivacyConfiguration;
    private ServerConfiguration mockServerConfiguration;
    private AdditionalQueryParameters mockAdditionalParameters;

    private SessionIDProvider mockSessionIdProvider;
    private ThreadIDProvider mockThreadIDProvider;
    private TimingProvider mockTimingProvider;
    private RandomNumberGenerator mockRandom;

    private Logger mockLogger;
    private BeaconCache mockBeaconCache;
    private SupplementaryBasicData mockSupplementaryData;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        mockOpenKitConfiguration = mock(OpenKitConfiguration.class);
        when(mockOpenKitConfiguration.getApplicationID()).thenReturn(APP_ID);
        when(mockOpenKitConfiguration.getPercentEncodedApplicationID()).thenReturn(APP_ID);
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
        when(mockServerConfiguration.getMultiplicity()).thenReturn(1);
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(true);
        when(mockServerConfiguration.getServerID()).thenReturn(SERVER_ID);
        when(mockServerConfiguration.getBeaconSizeInBytes()).thenReturn(30 * 1024); // 30kB
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(100); // 100%

        HTTPClientConfiguration mockHttpClientConfiguration = mock(HTTPClientConfiguration.class);
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

        OpenKitComposite parentOpenKitObject = mock(OpenKitComposite.class);
        when(parentOpenKitObject.getActionID()).thenReturn(0);

        mockRandom = mock(RandomNumberGenerator.class);
        when(mockRandom.nextPercentageValue()).thenReturn(0);

        mockSupplementaryData = mock(SupplementaryBasicData.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// generic defaults, instance creation and smaller getters/creators
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void defaultBeaconConfigurationDoesNotDisableCapturing() {
        // given
        Beacon target = createBeacon().build();

        // then
        assertThat(target.isDataCapturingEnabled(), is(true));
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
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn("dummy");

        target.send(httpClientProvider, mockAdditionalParameters);

        // then
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient, times(1))
                .sendBeaconRequest(ipCaptor.capture(), any(byte[].class), eq(mockAdditionalParameters), anyInt());

        String capturedIp = ipCaptor.getValue();
        assertThat(capturedIp, is(nullValue()));
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
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn("dummy");

        target.send(httpClientProvider, mockAdditionalParameters);

        // then
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient, times(1))
                .sendBeaconRequest(ipCaptor.capture(), any(byte[].class), eq(mockAdditionalParameters), anyInt());

        String capturedIp = ipCaptor.getValue();
        assertThat(capturedIp, is(nullValue()));
    }

    @Test
    public void createIDs() {
        // create test environment
        final Beacon target = createBeacon().build();

        // verify that the created sequence numbers are incremented
        int id1 = target.createID();
        assertThat(id1, is(1));

        int id2 = target.createID();
        assertThat(id2, is(2));

        int id3 = target.createID();
        assertThat(id3, is(3));
    }

    @Test
    public void getCurrentTimestamp() {
        // given
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(42L);
        Beacon target = createBeacon().build();

        // when obtaining the timestamp
        long timestamp = target.getCurrentTimestamp();

        // then verify
        assertThat(timestamp, is(42L));

        // verify called twice (once in Beacon's ctor) and once when invoking the call
        verify(mockTimingProvider, times(2)).provideTimestampInMilliseconds();
    }

    @Test
    public void createSequenceNumbers() {
        // create test environment
        final Beacon target = createBeacon().build();

        // verify that the created sequence numbers are incremented
        int id1 = target.createSequenceNumber();
        assertThat(id1, is(1));

        int id2 = target.createSequenceNumber();
        assertThat(id2, is(2));

        int id3 = target.createSequenceNumber();
        assertThat(id3, is(3));
    }

    @Test
    public void getSessionStartTime() {
        // given
        long startTime = 73;
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(startTime, 1L);

        // when
        Beacon target = createBeacon().build();

        // then
        assertThat(target.getSessionStartTime(), is(startTime));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// createTag - creating web request tag tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void createWebRequestTag() {
        // given
        final Beacon target = createBeacon().build();

        // when
        int sequenceNo = 42;
        String tag = target.createTag(ACTION_ID, sequenceNo);

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
        final Beacon target = createBeacon().build();

        // when
        int sequenceNo = 42;
        String tag = target.createTag(ACTION_ID, sequenceNo);

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
    public void beaconReturnsValidTagWithSessionNumberOneIfSessionNumberReportingDisallowed() {
        //given
        int sequenceNo = 1;
        when(mockPrivacyConfiguration.isSessionNumberReportingAllowed()).thenReturn(false);

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// addAction tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void addValidActionEvent() {
        // given
        final Beacon target = createBeacon().build();
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        int parentID = 13;
        when(action.getParentID()).thenReturn(parentID);
        String actionName = "MyAction";
        when(action.getName()).thenReturn(actionName);

        // when
        target.addAction(action);

        // then
        String expectedActionData =
                "et=1&" +                       // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + actionName + "&" +      // action name
                "ca=" + ACTION_ID + "&" +       // action ID
                "pa=" + parentID + "&" +        // parent action ID
                "s0=0&" +                       // action start sequence number
                "t0=0&" +                       // action start time (relative to session start)
                "s1=0&" +                       // action end sequence number
                "t1=0"                          // action duration (time from action start to end)\
        ;
        verify(mockBeaconCache, times(1)).addActionData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // action start time
                eq(expectedActionData)
        );
    }

    @Test
    public void addingNullActionThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("action is null or action.getName() is null or empty"));
        // given
        final Beacon target = createBeacon().build();

        // when
        target.addAction(null);
    }

    @Test
    public void addingActionWithNullNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("action is null or action.getName() is null or empty"));

        // given
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getName()).thenReturn(null);

        final Beacon target = createBeacon().build();

        // when
        target.addAction(null);
    }

    @Test
    public void addingActionWithEmptyNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("action is null or action.getName() is null or empty"));

        // given
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getName()).thenReturn("");

        final Beacon target = createBeacon().build();

        // when
        target.addAction(null);
    }

    @Test
    public void actionNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        when(action.getName()).thenReturn("actionName");

        // when
        target.addAction(action);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void actionNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        when(action.getName()).thenReturn("actionName");

        // when
        target.addAction(action);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void actionNotReportedIfActionReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isActionReportingAllowed()).thenReturn(false);
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        when(action.getName()).thenReturn("actionName");

        //when
        target.addAction(action);

        //then
        //verify action has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// startSession tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void addStartSessionEvent() {
        // given
        final Beacon target = createBeacon().build();

        // when
        target.startSession();

        // then
        String expectedEventData =
                "et=18&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "pa=0&" +                       // parent action
                "s0=1&" +                       // end session sequence number
                "t0=0"                          // session end time
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // session end time
            eq(expectedEventData)
        );
    }

    @Test
    public void sessionStartIsReportedRegardlessOfPrivacyConfiguration() {
        // given
        Beacon target = createBeacon().build();
        reset(mockPrivacyConfiguration);

        // when
        target.startSession();

        // then ensure session start has been serialized
        verify(mockBeaconCache, times(1)).addEventData(any(BeaconKey.class), anyLong(), anyString());
        verifyNoMoreInteractions(mockPrivacyConfiguration);
    }

    @Test
    public void sessionStartIsNotReportedIfDataSendingIsDisallowed() {
        // given
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);
        Beacon target = createBeacon().build();

        // when
        target.startSession();

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sessionStartIsNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.startSession();

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// endSession tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void addEndSessionEvent() {
        // given
        final Beacon target = createBeacon().build();

        // when
        target.endSession();

        // then
        String expectedEventData =
                "et=19&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "pa=0&" +                       // parent action
                "s0=1&" +                       // end session sequence number
                "t0=0"                          // session end time
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // session end time
                eq(expectedEventData)
        );
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
    public void sessionNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.endSession();

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportValue(int) tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidValueInt() {
        // given
        final Beacon target = createBeacon().build();
        String valueName = "IntValue";
        int value = 42;

        // when
        target.reportValue(ACTION_ID, valueName, value);

        // then
        String expectedEventData =
                "et=12&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + valueName + "&" +       // name of reported value
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of reported value event
                "t0=0&" +                       // event time since session start
                "vl=" + value                   // reported value
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event time
                eq(expectedEventData)
        );
    }

    @Test
    public void reportingIntValueWithNullValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, null, 42);
    }

    @Test
    public void reportingIntValueWithEmptyValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "", 42);
    }

    @Test
    public void intValueIsNotReportedIfReportValueDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 123);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void intValueNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 123);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void intValueNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "test value", 123);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportValue(long) tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidValueLong() {
        // given
        final Beacon target = createBeacon().build();
        String valueName = "IntValue";
        long value = Long.MAX_VALUE;

        // when
        target.reportValue(ACTION_ID, valueName, value);

        // then
        String expectedEventData =
                "et=12&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + valueName + "&" +       // name of reported value
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of reported value event
                "t0=0&" +                       // event time since session start
                "vl=" + value                   // reported value
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // event time
            eq(expectedEventData)
        );
    }

    @Test
    public void reportingLongValueWithNullValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, null, (long)42);
    }

    @Test
    public void reportingLongValueWithEmptyValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "", (long)42);
    }

    @Test
    public void longValueIsNotReportedIfReportValueDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", Long.MIN_VALUE);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void longValueNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", Long.MIN_VALUE);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void longValueNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "test value", Long.MIN_VALUE);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportValue(double) tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidValueDouble() {
        // given
        final Beacon target = createBeacon().build();
        String valueName = "DoubleValue";
        double value = 3.1415;

        // when
        target.reportValue(ACTION_ID, valueName, value);

        // then
        String expectedEventData =
                "et=13&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + valueName + "&" +       // name of reported value
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of reported value event
                "t0=0&" +                       // event time since session start
                "vl=" + value                   // reported value
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportingDoubleValueWithNullValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, null, Math.PI);
    }

    @Test
    public void reportingDoubleValueWithEmptyValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "", Math.E);
    }

    @Test
    public void doubleValueIsNotReportedIfReportValueDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 2.71);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void doubleValueIsNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", 2.71);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void doubleValueIsNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "test value", 2.71);

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportValue(String) tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidValueString() {
        // given
        final Beacon target = createBeacon().build();
        String valueName = "StringValue";
        String value = "HelloWorld";

        // when
        target.reportValue(ACTION_ID, valueName, value);

        // then
        String expectedEventData =
                "et=11&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + valueName + "&" +       // name of reported value
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of reported value
                "t0=0&" +                       // event time since session start
                "vl=" + value                   // reported value
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportingStringValueWithNullValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, null, "some value");
    }

    @Test
    public void reportingStringValueWithEmptyValueNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("valueName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "", "some other value");
    }

    @Test
    public void reportValueStringWithNullValueWorks() {
        // given
        final Beacon target = createBeacon().build();
        String valueName = "StringValue";

        // when
        target.reportValue(ACTION_ID, valueName, null);

        // then
        String expectedEventData =
                "et=11&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + valueName + "&" +       // name of reported value
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of reported value
                "t0=0"                          // event time since session start
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void stringValueIsNotReportedIfValueReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isValueReportingAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", "test data");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void stringValueIsNotReportedIfDataSendingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportValue(ACTION_ID, "test value", "test data");

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void stringValueIsNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.reportValue(ACTION_ID, "test value", "test data");

        // then
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportEvent tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidEvent() {
        // given
        final Beacon target = createBeacon().build();
        String eventName = "SomeEvent";

        // when
        target.reportEvent(ACTION_ID, eventName);

        // then
        String expectedEventData =
                "et=10&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + eventName + "&" +       // name of event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of reported event
                "t0=0"                          // event time since session start
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportingEventWithNullEventNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("eventName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportEvent(ACTION_ID, null);
    }

    @Test
    public void reportingEventWithEmptyEventNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("eventName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportEvent(ACTION_ID, "");
    }

    @Test
    public void namedEventIsNotReportedIfEventReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isEventReportingAllowed()).thenReturn(false);

        // when
        target.reportEvent(ACTION_ID, "test event");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void namedEventIsNotReportedIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportEvent(ACTION_ID, "Event name");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void namedEventIsNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.reportEvent(ACTION_ID, "Event name");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// sendBizEvent tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void sendValidBizEvent() {
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventType = "SomeType";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("TestString", JSONStringValue.fromString("Test"));
        attributes.put("TestBool", JSONBooleanValue.fromValue(false));

        // when
        target.sendBizEvent(eventType, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("TestString", JSONStringValue.fromString("Test"));
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));
        actualAttributes.put("TestBool", JSONBooleanValue.fromValue(false));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(62));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendBizEventWithNameInAttributes(){
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventType = "SomeType";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("event.name", JSONStringValue.fromString("Test"));

        // when
        target.sendBizEvent(eventType, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString("Test"));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(45));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendBizEventWithEventProviderInAttributes(){
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventType = "SomeType";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString("Test"));

        // when
        target.sendBizEvent(eventType, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString("Test"));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(49));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendBizEventWithTypeAndDtTypeInAttributes(){
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventType = "SomeType";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("event.type", JSONStringValue.fromString("Test"));
        attributes.put("event.kind", JSONStringValue.fromString("Test"));

        // when
        target.sendBizEvent(eventType, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(45));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );

        verify(mockLogger, times(1)).warning(
                "EventPayloadBuilder addNonOverrideableAttribute: event.type is reserved for internal values!");
        verify(mockLogger, times(1)).warning(
                "EventPayloadBuilder addNonOverrideableAttribute: event.kind is reserved for internal values!");
    }

    @Test
    public void sendBizEventWithDtRumSchemaVersionInAttributes(){
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventType = "SomeType";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("dt.rum.schema_version", JSONStringValue.fromString("Test"));

        // when
        target.sendBizEvent(eventType, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(56));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );

        verify(mockLogger, times(1)).warning(
                "EventPayloadBuilder cleanReservedInternalAttributes: dt.rum.schema_version is reserved for internal values!");
    }

    @Test
    public void sendBizEventWithDtRumCustomAttributeSizeInAttributes(){
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventType = "customType";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("dt.rum.custom_attributes_size", JSONStringValue.fromString("overridden"));

        // when
        target.sendBizEvent(eventType, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(72));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );

        verify(mockLogger, times(1)).warning(
                "EventPayloadBuilder cleanReservedInternalAttributes: dt.rum.custom_attributes_size is reserved for internal values!");
    }

    @Test
    public void sendBizEventWithNullEventTypeThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("type is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.sendBizEvent(null, null);
    }

    @Test
    public void sendBizEventWithEmptyEventTypeThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("type is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.sendBizEvent("", null);
    }

    @Test
    public void sendBizEventWithEmptyPayload() {
        // given
        final Beacon target = createBeacon().build();
        String eventType = "SomeType";
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);

        // when
        target.sendBizEvent(eventType, new HashMap<String, JSONValue>());

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(25));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload

                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendBizEventWithNullPayload() {
        // given
        final Beacon target = createBeacon().build();
        String eventType = "SomeType";
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);

        // when
        target.sendBizEvent(eventType, null);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.type", JSONStringValue.fromString(eventType));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("BIZ_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventType));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));
        actualAttributes.put("dt.rum.custom_attributes_size", JSONNumberValue.fromLong(25));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendBizEventIsNotReportedIfEventReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isEventReportingAllowed()).thenReturn(false);
        HashMap<String, JSONValue> attributes = new HashMap<>();

        // when
        target.sendBizEvent("EventType", attributes);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sendBizEventIsNotReportedIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        HashMap<String, JSONValue> attributes = new HashMap<>();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.sendBizEvent("EventType", attributes);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sendBizEventIsNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        HashMap<String, JSONValue> attributes = new HashMap<>();
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.sendBizEvent("EventType", attributes);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sendBizEventPayloadIsToBig() throws UnsupportedEncodingException {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Event payload is exceeding 16384 bytes!"));

        // given
        final Beacon target = createBeacon().build();
        String eventType = "SomeType";

        HashMap<String, JSONValue> attributes = new HashMap<>();

        for(int i = 0; i < 500; i++){
            attributes.put("TestTypeForOversizeMap"+i, JSONStringValue.fromString(eventType));
        }

        // when
        target.sendBizEvent(eventType, attributes);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// sendEvent tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void sendValidEvent() {
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventName = "SomeEvent";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("TestString", JSONStringValue.fromString("Test"));
        attributes.put("TestBool", JSONBooleanValue.fromValue(false));
        attributes.put("event.name", JSONStringValue.fromString("Anything"));

        // when
        target.sendEvent(eventName, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("TestString", JSONStringValue.fromString("Test"));
        actualAttributes.put("event.name", JSONStringValue.fromString(eventName));
        actualAttributes.put("TestBool", JSONBooleanValue.fromValue(false));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("RUM_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                    "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );

        verify(mockLogger, times(1)).warning(
                "EventPayloadBuilder addNonOverrideableAttribute: event.name is reserved for internal values!");
    }

    @Test
    public void sendEventWithDtType() {
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventName = "SomeEvent";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("event.kind", JSONStringValue.fromString("Anything"));

        // when
        target.sendEvent(eventName, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.name", JSONStringValue.fromString(eventName));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("Anything"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        final String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;

        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );

        verify(mockLogger, times(0)).warning(
                "EventPayloadBuilder addNonOverrideableAttribute: dt.type is reserved for internal values!");
    }

    @Test
    public void sendEventWithEventProviderInAttributes() {
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventName = "SomeEvent";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString("Anything"));

        // when
        target.sendEvent(eventName, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.name", JSONStringValue.fromString(eventName));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("RUM_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString("Anything"));
        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        final String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;

        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendEventWithDtRumSchemaInAttributes() {
        // given
        final Beacon target = createBeacon().build();
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        String eventName = "SomeEvent";

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("dt.rum.schema_version", JSONStringValue.fromString("Anything"));

        // when
        target.sendEvent(eventName, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.name", JSONStringValue.fromString(eventName));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("RUM_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        final String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;

        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );

        verify(mockLogger, times(1)).warning(
                "EventPayloadBuilder cleanReservedInternalAttributes: dt.rum.schema_version is reserved for internal values!");
    }

    @Test
    public void sendEventWithNullEventNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("name is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.sendEvent(null, null);
    }

    @Test
    public void sendEventWithEmptyEventNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("name is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.sendEvent("", null);
    }

    @Test
    public void sendEventWithEmptyPayload() {
        // given
        final Beacon target = createBeacon().build();
        String eventName = "SomeEvent";
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);

        HashMap<String, JSONValue> attributes = new HashMap<>();
        attributes.put("event.name", JSONStringValue.fromString(eventName));

        // when
        target.sendEvent(eventName, attributes);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.name", JSONStringValue.fromString(eventName));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("RUM_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                "pl=" + encodedPayload // event payload

                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendEventWithNullPayload() {
        // given
        final Beacon target = createBeacon().build();
        String eventName = "SomeEvent";
        String appVersion = "1111";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);

        // when
        target.sendEvent(eventName, null);

        HashMap<String, JSONValue> actualAttributes = new HashMap<>();
        actualAttributes.put("event.name", JSONStringValue.fromString(eventName));

        actualAttributes.put(EventPayloadAttributes.TIMESTAMP, JSONNumberValue.fromLong(0));
        actualAttributes.put(EventPayloadAttributes.EVENT_KIND, JSONStringValue.fromString("RUM_EVENT"));
        actualAttributes.put(EventPayloadAttributes.EVENT_PROVIDER, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_APPLICATION_ID, JSONStringValue.fromString(APP_ID));
        actualAttributes.put(EVENT_PAYLOAD_INSTANCE_ID, JSONStringValue.fromString(String.valueOf(DEVICE_ID)));
        actualAttributes.put(EVENT_PAYLOAD_SESSION_ID, JSONStringValue.fromString(String.valueOf(SESSION_ID)));
        actualAttributes.put(EventPayloadAttributes.APP_VERSION, JSONStringValue.fromString(appVersion));
        actualAttributes.put(EventPayloadAttributes.OS_NAME, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MANUFACTURER, JSONStringValue.fromString(""));
        actualAttributes.put(EventPayloadAttributes.DEVICE_MODEL_IDENTIFIER, JSONStringValue.fromString(""));
        actualAttributes.put("dt.rum.schema_version", JSONStringValue.fromString("1.0"));

        // then
        String encodedPayload = PercentEncoder.encode(JSONObjectValue.fromMap(actualAttributes).toString(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);
        String expectedEventData =
                "et=98&" +   // event type
                        "pl=" + encodedPayload // event payload
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // event timestamp
                argThat(new EventPayloadMatcher(expectedEventData))
        );
    }

    @Test
    public void sendEventIsNotReportedIfEventReportingDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isEventReportingAllowed()).thenReturn(false);
        HashMap<String, JSONValue> attributes = new HashMap<>();

        // when
        target.sendEvent("EventName", attributes);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sendEventIsNotReportedIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        HashMap<String, JSONValue> attributes = new HashMap<>();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.sendEvent("EventName", attributes);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void sendEventIsNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        HashMap<String, JSONValue> attributes = new HashMap<>();
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.sendEvent("EventName", attributes);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    // Test about Length
    @Test
    public void sendEventPayloadIsToBig() throws UnsupportedEncodingException {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Event payload is exceeding 16384 bytes!"));

        // given
        final Beacon target = createBeacon().build();
        String eventName = "SomeEvent";

        HashMap<String, JSONValue> attributes = new HashMap<>();

        for(int i = 0; i < 500; i++){
            attributes.put("TestNameForOversizeMap"+i, JSONStringValue.fromString(eventName));
        }

        // when
        target.sendEvent(eventName, attributes);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// report mutable supplementary basic data
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportNetworkTechnology() {
        // given
        int sessionSequence = 1213;
        int visitStoreVersion = 2;
        String appVersion = "1111";
        String ipAddress = "192.168.0.1";

        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("system");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("manufacturer");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("model");
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(null);
        when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        when(mockSupplementaryData.getNetworkTechnology()).thenReturn("TestValue");

        Beacon target = createBeacon().withIpAddress(ipAddress).withSessionSequenceNumber(sessionSequence).build();

        // when
        target.send(mock(HTTPClientProvider.class), null);

        // then
        String expectedPrefix = "vv=" + ProtocolConstants.PROTOCOL_VERSION +
                "&va=" + ProtocolConstants.OPENKIT_VERSION +
                "&ap=" + APP_ID +
                "&vn=" + appVersion +
                "&pt=" + ProtocolConstants.PLATFORM_TYPE_OPENKIT +
                "&tt=" + ProtocolConstants.AGENT_TECHNOLOGY_TYPE +
                "&vi=" + DEVICE_ID +
                "&sn=" + SESSION_ID +
                "&ip=" + ipAddress +
                "&os=system" +
                "&mf=manufacturer" +
                "&md=model" +
                "&dl=2" +
                "&cl=2" +
                "&vs=" + visitStoreVersion +
                "&ss=" + sessionSequence +
                "&tx=0" +
                "&tv=0" +
                "&mp=1" +
                "&np=TestValue";

        BeaconKey expectedBeaconKey = new BeaconKey(SESSION_ID, sessionSequence);
        verify(mockBeaconCache, times(1)).prepareDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1)).hasDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1))
                .getNextBeaconChunk(eq(expectedBeaconKey), eq(expectedPrefix), anyInt(), anyChar());
    }

    @Test
    public void reportNetworkCarrier() {
        // given
        int sessionSequence = 1213;
        int visitStoreVersion = 2;
        String appVersion = "1111";
        String ipAddress = "192.168.0.1";

        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("system");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("manufacturer");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("model");
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(null);
        when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        when(mockSupplementaryData.getCarrier()).thenReturn("TestValue");

        Beacon target = createBeacon().withIpAddress(ipAddress).withSessionSequenceNumber(sessionSequence).build();

        // when
        target.send(mock(HTTPClientProvider.class), null);

        // then
        String expectedPrefix = "vv=" + ProtocolConstants.PROTOCOL_VERSION +
                "&va=" + ProtocolConstants.OPENKIT_VERSION +
                "&ap=" + APP_ID +
                "&vn=" + appVersion +
                "&pt=" + ProtocolConstants.PLATFORM_TYPE_OPENKIT +
                "&tt=" + ProtocolConstants.AGENT_TECHNOLOGY_TYPE +
                "&vi=" + DEVICE_ID +
                "&sn=" + SESSION_ID +
                "&ip=" + ipAddress +
                "&os=system" +
                "&mf=manufacturer" +
                "&md=model" +
                "&dl=2" +
                "&cl=2" +
                "&vs=" + visitStoreVersion +
                "&ss=" + sessionSequence +
                "&tx=0" +
                "&tv=0" +
                "&mp=1" +
                "&cr=TestValue";

        BeaconKey expectedBeaconKey = new BeaconKey(SESSION_ID, sessionSequence);
        verify(mockBeaconCache, times(1)).prepareDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1)).hasDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1))
                .getNextBeaconChunk(eq(expectedBeaconKey), eq(expectedPrefix), anyInt(), anyChar());
    }

    @Test
    public void reportConnectionType() {
        // given
        int sessionSequence = 1213;
        int visitStoreVersion = 2;
        String appVersion = "1111";
        String ipAddress = "192.168.0.1";

        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("system");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("manufacturer");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("model");
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(null);
        when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        when(mockSupplementaryData.getConnectionType()).thenReturn(ConnectionType.Lan);

        Beacon target = createBeacon().withIpAddress(ipAddress).withSessionSequenceNumber(sessionSequence).build();

        // when
        target.send(mock(HTTPClientProvider.class), null);

        // then
        String expectedPrefix = "vv=" + ProtocolConstants.PROTOCOL_VERSION +
                "&va=" + ProtocolConstants.OPENKIT_VERSION +
                "&ap=" + APP_ID +
                "&vn=" + appVersion +
                "&pt=" + ProtocolConstants.PLATFORM_TYPE_OPENKIT +
                "&tt=" + ProtocolConstants.AGENT_TECHNOLOGY_TYPE +
                "&vi=" + DEVICE_ID +
                "&sn=" + SESSION_ID +
                "&ip=" + ipAddress +
                "&os=system" +
                "&mf=manufacturer" +
                "&md=model" +
                "&dl=2" +
                "&cl=2" +
                "&vs=" + visitStoreVersion +
                "&ss=" + sessionSequence +
                "&tx=0" +
                "&tv=0" +
                "&mp=1" +
                "&ct=l";

        BeaconKey expectedBeaconKey = new BeaconKey(SESSION_ID, sessionSequence);
        verify(mockBeaconCache, times(1)).prepareDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1)).hasDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1))
                .getNextBeaconChunk(eq(expectedBeaconKey), eq(expectedPrefix), anyInt(), anyChar());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportError with errorCode tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportErrorCode() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        int errorCode = -123;

        // when
        target.reportError(ACTION_ID, errorName, errorCode);

        // then
        String expectedEventData =
                "et=40&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "ev=" + errorCode + "&" +       // reported error value
                "tt=c"                          // error technology type
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // error event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportingErrorCodeWithNullErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, null, 1234);
    }

    @Test
    public void reportingErrorCodeWithEmptyErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, "", 1234);
    }

    @Test
    public void errorCodeNotReportedIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "Error name", 123);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorCodeNotReportedIfErrorSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "Error name", 123);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorCodeNotReportedIfErrorReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isErrorReportingAllowed()).thenReturn(false);

        //when
        target.reportError(ACTION_ID, "DivByZeroError", 127);

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorCodeNotReportedIfDisallowedByTrafficControl() {
        //given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        //when
        target.reportError(ACTION_ID, "DivByZeroError", 127);

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportError with cause tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportErrorWithCause() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeName = "CausedBy";
        String causeReason = "SomeReason";
        String causeStackTrace = "HereComesTheTrace";

        // when
        target.reportError(ACTION_ID, errorName, causeName, causeReason, causeStackTrace);

        // then
        String expectedEventData =
            "et=42&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "ev=" + causeName + "&" +       // reported error value
                "rs=" + causeReason + "&" +     // reported error reason
                "st=" + causeStackTrace + "&" + // reported error stack trace
                "tt=c"                          // error technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // error event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void reportingErrorWithCauseWithNullErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, null, "causeName", "causeDescription", "stackTrace");
    }

    @Test
    public void reportingErrorWithCauseWithEmptyErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, "", "causeName", "causeDescription", "stackTrace");
    }

    @Test
    public void reportErrorIsTruncatingReasonIfTooLong() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeStackTrace = "HereComesTheTrace";

        String causeReason = new String(new char[1001]).replace('\0', 'a');
        String causeReasonTruncated = new String(new char[1000]).replace('\0', 'a');

        // when
        target.reportError(ACTION_ID, errorName, null, causeReason, causeStackTrace);

        // then
        String expectedEventData =
                "et=42&" +                      // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "na=" + errorName + "&" +       // name of error event
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of error event
                        "t0=0&" +                       // timestamp of error event since session start
                        "rs=" + causeReasonTruncated + "&" +     // reported error reason
                        "st=" + causeStackTrace + "&" + // reported error stack trace
                        "tt=c"                          // error technology type
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // error event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportErrorIsTruncatingStacktraceIfTooLong() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeReason = "SomeReason";
        String causeStacktrace = new String(new char[128001]).replace('\0', 'a');
        String causeStacktraceTruncated = new String(new char[128000]).replace('\0', 'a');

        // when
        target.reportError(ACTION_ID, errorName, null, causeReason, causeStacktrace);

        // then
        String expectedEventData =
                "et=42&" +                      // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "na=" + errorName + "&" +       // name of error event
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of error event
                        "t0=0&" +                       // timestamp of error event since session start
                        "rs=" + causeReason + "&" +     // reported error reason
                        "st=" + causeStacktraceTruncated + "&" + // reported error stack trace
                        "tt=c"                          // error technology type
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // error event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportErrorIsTruncatingStacktraceUntilLastBreakIfTooLong() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeReason = "SomeReason";
        String causeStacktraceTruncated = new String(new char[127900]).replace('\0', 'a');
        String causeStacktrace = causeStacktraceTruncated + '\n' + new String(new char[1000]).replace('\0', 'a');

        // when
        target.reportError(ACTION_ID, errorName, null, causeReason, causeStacktrace);

        // then
        String expectedEventData =
                "et=42&" +                      // event type
                        "it=" + THREAD_ID + "&" +       // thread ID
                        "na=" + errorName + "&" +       // name of error event
                        "pa=" + ACTION_ID + "&" +       // parent action ID
                        "s0=1&" +                       // sequence number of error event
                        "t0=0&" +                       // timestamp of error event since session start
                        "rs=" + causeReason + "&" +     // reported error reason
                        "st=" + causeStacktraceTruncated + "&" + // reported error stack trace
                        "tt=c"                          // error technology type
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                         // error event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportErrorWithNullCauseNameWorks() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeReason = "SomeReason";
        String causeStackTrace = "HereComesTheTrace";

        // when
        target.reportError(ACTION_ID, errorName, null, causeReason, causeStackTrace);

        // then
        String expectedEventData =
            "et=42&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "rs=" + causeReason + "&" +     // reported error reason
                "st=" + causeStackTrace + "&" + // reported error stack trace
                "tt=c"                          // error technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // error event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void reportErrorWithNullCauseDescriptionWorks() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeName = "CausedBy";
        String causeStackTrace = "HereComesTheTrace";

        // when
        target.reportError(ACTION_ID, errorName, causeName, null, causeStackTrace);

        // then
        String expectedEventData =
            "et=42&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "ev=" + causeName + "&" +       // reported error value
                "st=" + causeStackTrace + "&" + // reported error stack trace
                "tt=c"                          // error technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // error event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void reportErrorWithNullCauseStackTraceWorks() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String causeName = "CausedBy";
        String causeReason = "SomeReason";

        // when
        target.reportError(ACTION_ID, errorName, causeName, causeReason, null);

        // then
        String expectedEventData =
            "et=42&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "ev=" + causeName + "&" +       // reported error value
                "rs=" + causeReason + "&" +     // reported error reason
                "tt=c"                          // error technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // error event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void errorWithCauseNotReportedIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "error", "causeName", "causeDescription", "stackTrace");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorWithCauseNotReportedIfErrorSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "error", "causeName", "causeDescription", "stackTrace");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorWithCauseNotReportedIfErrorReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isErrorReportingAllowed()).thenReturn(false);

        //when
        target.reportError(ACTION_ID, "error", "causeName", "causeDescription", "stackTrace");

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorWithCauseNotReportedIfDisallowedByTrafficControl() {
        // given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, "error", "causeName", "causeDescription", "stackTrace");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportError with Throwable tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportErrorWithThrowable() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        Throwable t = new IllegalArgumentException("someArg");
        CrashFormatter crashFormatter = new CrashFormatter(t);
        String causeName = crashFormatter.getName();
        String causeReason = crashFormatter.getReason();
        String causeStackTrace = PercentEncoder.encode(crashFormatter.getStackTrace().trim(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);

        // when
        target.reportError(ACTION_ID, errorName, t);

        // then
        String expectedEventData =
            "et=42&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "ev=" + causeName + "&" +       // reported error value
                "rs=" + causeReason + "&" +     // reported error reason
                "st=" + causeStackTrace + "&" + // reported error stack trace
                "tt=c"                          // error technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // error event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void reportingErrorWithThrowableWithNullErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, null, new IllegalStateException("illegal"));
    }

    @Test
    public void reportingErrorWithThrowableWithEmptyErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.reportError(ACTION_ID, "", new IllegalStateException("illegal"));
    }

    @Test
    public void reportErrorWithNullThrowableWorks() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";

        // when
        target.reportError(ACTION_ID, errorName, null);

        // then
        String expectedEventData =
            "et=42&" +                      // event type
                "it=" + THREAD_ID + "&" +       // thread ID
                "na=" + errorName + "&" +       // name of error event
                "pa=" + ACTION_ID + "&" +       // parent action ID
                "s0=1&" +                       // sequence number of error event
                "t0=0&" +                       // timestamp of error event since session start
                "tt=c"                          // error technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                         // error event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void errorWithThrowableNotReportedIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "error", new IllegalStateException("illegal"));

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorWithThrowableNotReportedIfErrorSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(false);

        // when
        target.reportError(ACTION_ID, "error", new IllegalStateException("illegal"));

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorWithThrowableNotReportedIfErrorReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isErrorReportingAllowed()).thenReturn(false);

        //when
        target.reportError(ACTION_ID, "error", new IllegalStateException("illegal"));

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void errorWithThrowableNotReportedIfDisallowedByTrafficControl() {
        //given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        //when
        target.reportError(ACTION_ID, "error", new IllegalStateException("illegal"));

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportCrash with String tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidCrash() {
        // given
        final Beacon target = createBeacon().build();

        String errorName = "SomeEvent";
        String reason = "SomeReason";
        String stacktrace = "SomeStacktrace";

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // then
        String expectedEventData =
                "et=50&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + errorName + "&" +   // reported crash name
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // sequence number of reported crash
                "t0=0&" +                   // timestamp of crash since session start
                "rs=" + reason + "&" +      // reported reason
                "st=" + stacktrace + "&" +  // reported stacktrace
                "tt=c"                      // crash technology type
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // crash event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportingCrashWithNullErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        String reason = "SomeReason";
        String stacktrace = "SomeStacktrace";

        final Beacon target = createBeacon().build();

        // when
        target.reportCrash(null, reason, stacktrace);
    }

    @Test
    public void reportingCrashWithEmptyErrorNameThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("errorName is null or empty"));

        // given
        String reason = "SomeReason";
        String stacktrace = "SomeStacktrace";

        final Beacon target = createBeacon().build();

        // when
        target.reportCrash("", reason, stacktrace);
    }

    @Test
    public void reportCrashWithNullReasonWorks() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String stacktrace = "SomeStacktrace";

        // when
        target.reportCrash(errorName, null, stacktrace);

        // then
        String expectedEventData =
            "et=50&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + errorName + "&" +   // reported crash name
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // sequence number of reported crash
                "t0=0&" +                   // timestamp of crash since session start
                "st=" + stacktrace + "&" +  // reported stacktrace
                "tt=c"                      // crash technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // crash event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void reportCrashIsTruncatingReasonIfTooLong() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String stacktrace = "SomeStacktrace";

        String reason = new String(new char[1001]).replace('\0', 'a');
        String reasonTruncated = new String(new char[1000]).replace('\0', 'a');

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // then
        String expectedEventData =
                "et=50&" +                  // event type
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "na=" + errorName + "&" +   // reported crash name
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // sequence number of reported crash
                        "t0=0&" +                   // timestamp of crash since session start
                        "rs=" + reasonTruncated + "&" +      // reported reason
                        "st=" + stacktrace + "&" +  // reported stacktrace
                        "tt=c"                      // crash technology type
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // crash event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportCrashIsTruncatingStacktraceIfTooLong() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String reason = "SomeReason";

        String stacktrace = new String(new char[128001]).replace('\0', 'a');
        String stacktraceTruncated = new String(new char[128000]).replace('\0', 'a');

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // then
        String expectedEventData =
                "et=50&" +                  // event type
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "na=" + errorName + "&" +   // reported crash name
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // sequence number of reported crash
                        "t0=0&" +                   // timestamp of crash since session start
                        "rs=" + reason + "&" +      // reported reason
                        "st=" + stacktraceTruncated + "&" +  // reported stacktrace
                        "tt=c"                      // crash technology type
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // crash event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportCrashIsTruncatingStacktraceUntilLastBreakIfTooLong() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String reason = "SomeReason";

        String stacktraceTruncated = new String(new char[127900]).replace('\0', 'a');
        String stacktrace = stacktraceTruncated + '\n' + new String(new char[1000]).replace('\0', 'a');

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // then
        String expectedEventData =
                "et=50&" +                  // event type
                        "it=" + THREAD_ID + "&" +   // thread ID
                        "na=" + errorName + "&" +   // reported crash name
                        "pa=0&" +                   // parent action ID
                        "s0=1&" +                   // sequence number of reported crash
                        "t0=0&" +                   // timestamp of crash since session start
                        "rs=" + reason + "&" +      // reported reason
                        "st=" + stacktraceTruncated + "&" +  // reported stacktrace
                        "tt=c"                      // crash technology type
                ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // crash event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportCrashWithNullStacktraceWorks() {
        // given
        final Beacon target = createBeacon().build();
        String errorName = "SomeEvent";
        String reason = "SomeReason";

        // when
        target.reportCrash(errorName, reason, null);

        // then
        String expectedEventData =
            "et=50&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + errorName + "&" +   // reported crash name
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // sequence number of reported crash
                "t0=0&" +                   // timestamp of crash since session start
                "rs=" + reason + "&" +      // reported reason
                "tt=c"                      // crash technology type
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // crash event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void reportCrashDoesNotReportIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashDoesNotReportIfCrashSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(false);

        // when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashDoesNotReportIfCrashReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isCrashReportingAllowed()).thenReturn(false);

        //when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashDoesNotReportIfDisallowedByTrafficControl() {
        //given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        //when
        target.reportCrash("Error name", "The reason for this error", "the stack trace");

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// reportCrash with Throwable tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportValidCrashWithThrowable() {
        // given
        Throwable t = new NullPointerException("SomethingIsNull");
        CrashFormatter crashFormatter = new CrashFormatter(t);
        String errorName = crashFormatter.getName();
        String reason = crashFormatter.getReason();
        String stacktrace = PercentEncoder.encode(crashFormatter.getStackTrace().trim(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);

        final Beacon target = createBeacon().build();

        // when
        target.reportCrash(t);

        // then

        String expectedEventData =
                "et=50&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + errorName + "&" +   // reported crash name
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // sequence number of reported crash
                "t0=0&" +                   // timestamp of crash since session start
                "rs=" + reason + "&" +      // reported reason
                "st=" + stacktrace + "&" +  // reported stacktrace
                "tt=c"                      // crash technology type
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // crash event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void reportCrashWithNullThrowableThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("throwable is null"));

        // given
        Beacon target = createBeacon().build();

        // when
        target.reportCrash(null);
    }

    @Test
    public void reportCrashWithThrowableDoesNotReportIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.reportCrash(new IllegalStateException("illegal"));

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashWithThrowableDoesNotReportIfCrashSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(false);

        // when
        target.reportCrash(new IllegalStateException("illegal"));

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashWithThrowableDoesNotReportIfCrashReportingDisallowed() {
        //given
        Beacon target = createBeacon().build();
        when(mockPrivacyConfiguration.isCrashReportingAllowed()).thenReturn(false);

        //when
        target.reportCrash(new IllegalStateException("illegal"));

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void reportCrashWithThrowableDoesNotReportDisallowedByTrafficControl() {
        //given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        //when
        target.reportCrash(new IllegalStateException("illegal"));

        //then
        //verify error has not been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// addWebRequest tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void addWebRequest() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
                "et=30&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" +  // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "bs=13&" +                  // number of bytes sent
                "br=14&" +                  // number of bytes received
                "rc=15"                     // response code
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // web request start timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void addWebRequestWithNullWebRequestTracerThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("webRequestTracer is null or webRequestTracer.getURL() is null or empty"));

        // given
        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, null);
    }

    @Test
    public void addWebRequestWithNullUrlThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("webRequestTracer is null or webRequestTracer.getURL() is null or empty"));

        // given
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(null);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, null);
    }

    @Test
    public void addWebRequestWithEmptyUrlThrowsException() {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("webRequestTracer is null or webRequestTracer.getURL() is null or empty"));

        // given
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn("");

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, null);
    }

    @Test
    public void canAddSentBytesEqualToZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(0);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
            "et=30&" +                      // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" +  // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "bs=0&" +                   // number of bytes sent
                "br=14&" +                  // number of bytes received
                "rc=15"                     // response code
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // web request start timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void cannotAddSentBytesLessThanZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(-1);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
            "et=30&" +                      // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" +  // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "br=14&" +                  // number of bytes received
                "rc=15"                     // response code
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // web request start timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void canAddReceivedBytesEqualToZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(0);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
            "et=30&" +                      // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" +  // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "bs=13&" +                  // number of bytes sent
                "br=0&" +                   // number of bytes received
                "rc=15"                     // response code
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // web request start timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void cannotAddReceivedBytesLessThanZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(-1);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
            "et=30&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" + // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "bs=13&" +                  // number of bytes sent
                "rc=15"                     // response code
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // web request start timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void canAddResponseCodeEqualToZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(0);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
            "et=30&" +                      // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" +  // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "bs=13&" +                  // number of bytes sent
                "br=14&" +                  // number of bytes received
                "rc=0"                      // response code
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // web request start timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void cannotAddResponseCodeLessThanZeroToWebRequestTracer() throws UnsupportedEncodingException {
        // given
        String rawUrl = "https://www.google.com";
        String encodedUrl = URLEncoder.encode(rawUrl, "UTF-8");
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn(rawUrl);
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(-1);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then
        String expectedEventData =
            "et=30&" +                      // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + encodedUrl + "&" +  // reported URL
                "pa=" + ACTION_ID + "&" +   // parent action ID
                "s0=0&" +                   // web request start sequence number
                "t0=0&" +                   // web request start time (since session start)
                "s1=0&" +                   // web request end sequence number
                "t1=0&" +                   // web request end time (relative to start time)
                "bs=13&" +                  // number of bytes sent
                "br=14"                     // number of bytes received
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // web request start timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void noWebRequestIsReportedIfDataSendingIsDisallowed() {
        // given
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn("https://www.google.com");
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        final Beacon target = createBeacon().build();

        // when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noWebRequestIsReportedIfWebRequestTracingDisallowed() {
        // given
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn("https://www.google.com");
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        when(mockPrivacyConfiguration.isWebRequestTracingAllowed()).thenReturn(false);

        Beacon target = createBeacon().build();

        //when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        //then
        //verify nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    @Test
    public void noWebRequestIsReportedIfDisallowedByTrafficControl() {
        // given
        WebRequestTracerBaseImpl webRequestTracer = mock(WebRequestTracerBaseImpl.class);
        when(webRequestTracer.getURL()).thenReturn("https://www.google.com");
        when(webRequestTracer.getBytesSent()).thenReturn(13);
        when(webRequestTracer.getBytesReceived()).thenReturn(14);
        when(webRequestTracer.getResponseCode()).thenReturn(15);

        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        //when
        target.addWebRequest(ACTION_ID, webRequestTracer);

        //then
        //verify nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// identifyUser tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void validIdentifyUserEvent() {
        // given
        Beacon target = createBeacon().build();
        String userID = "myTestUser";

        // when
        target.identifyUser(userID);

        // then
        String expectedEventData =
                "et=60&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=" + userID + "&" +      // reported user ID
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // identify user sequence number
                "t0=0"                      // event timestamp since session start
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // identify user event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void identifyUserWithNullUserTagWorks() {
        // given
        Beacon target = createBeacon().build();

        // when
        target.identifyUser(null);

        // then
        String expectedEventData =
                "et=60&" +                  // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // identify user sequence number
                "t0=0"
        ;
        verify(mockBeaconCache, times(1)).addEventData(
                eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
                eq(0L),                     // identify user event timestamp
                eq(expectedEventData)
        );
    }

    @Test
    public void identifyUserWithEmptyUserTagWorks() {
        // given
        Beacon target = createBeacon().build();
        String userID = "";

        // when
        target.identifyUser(userID);

        // then
        String expectedEventData =
            "et=60&" +                      // event type
                "it=" + THREAD_ID + "&" +   // thread ID
                "na=&" +                    // reported user ID
                "pa=0&" +                   // parent action ID
                "s0=1&" +                   // identify user sequence number
                "t0=0"                      // event timestamp since session start
            ;
        verify(mockBeaconCache, times(1)).addEventData(
            eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)), // beacon key
            eq(0L),                     // identify user event timestamp
            eq(expectedEventData)
        );
    }

    @Test
    public void cannotIdentifyUserIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);

        // when
        target.identifyUser("jane.doe@acme.com");

        // then ensure nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
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
    public void cannotIdentifyUserIfDisallowedByTrafficControl() {
        //given
        int trafficControlPercentage = 50;
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlPercentage);

        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlPercentage);
        Beacon target = createBeacon().build();

        //when
        target.identifyUser("jane@doe.com");

        //then
        //verify nothing has been serialized
        verifyZeroInteractions(mockBeaconCache);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// send tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void canHandleNoDataInBeaconSend() {
        // given
        Beacon target = createBeacon().build();
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient mockClient = mock(HTTPClient.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(mockClient);

        // when
        StatusResponse response = target.send(httpClientProvider, mockAdditionalParameters);

        // then (verify, that null is returned as no data was sent)
        assertThat(response, nullValue());
    }

    @Test
    public void sendValidData() {
        // given
        String ipAddress = "127.0.0.1";
        BeaconCache beaconCache = new BeaconCacheImpl(mockLogger);
        Beacon target = createBeacon()
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
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class), any(AdditionalQueryParameters.class), anyInt()))
                .thenReturn(successResponse);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        target.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = target.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class), eq(mockAdditionalParameters), anyInt());
    }

    @Test
    public void sendDataAndFakeErrorResponse() {
        // given
        String ipAddress = "127.0.0.1";
        BeaconCache beaconCache = new BeaconCacheImpl(mockLogger);
        Beacon target = createBeacon()
                .withIpAddress(ipAddress)
                .with(beaconCache)
                .build();
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 418;
        StatusResponse errorResponse = StatusResponse.createErrorResponse(mockLogger, responseCode);
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class), any(AdditionalQueryParameters.class), anyInt()))
                .thenReturn(errorResponse);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        target.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = target.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class), eq(mockAdditionalParameters), anyInt());
    }

    @Test
    public void sendCatchesUnsupportedEncodingException() {
        // given
        String beaconChunk = "some beacon string";
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(beaconChunk);
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);

        HTTPClient httpClient = mock(HTTPClient.class);
        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        final UnsupportedEncodingException exception = new UnsupportedEncodingException();

        BeaconInitializer beaconInitializer = mock(BeaconInitializer.class);
        when(beaconInitializer.getLogger()).thenReturn(mockLogger);
        when(beaconInitializer.getBeaconCache()).thenReturn(mockBeaconCache);
        when(beaconInitializer.getClientIpAddress()).thenReturn("127.0.0.1");
        when(beaconInitializer.getSessionIdProvider()).thenReturn(mockSessionIdProvider);
        when(beaconInitializer.getSessionSequenceNumber()).thenReturn(SESSION_SEQ_NO);
        when(beaconInitializer.getThreadIdProvider()).thenReturn(mockThreadIDProvider);
        when(beaconInitializer.getTimingProvider()).thenReturn(mockTimingProvider);
        when(beaconInitializer.getRandomNumberGenerator()).thenReturn(mockRandom);
        when(beaconInitializer.getSupplementaryBasicData()).thenReturn(mockSupplementaryData);

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
        verify(mockBeaconCache, times(1)).resetChunkedData(eq(new BeaconKey(SESSION_ID, SESSION_SEQ_NO)));
        verify(mockLogger, times(1)).error(": Required charset \"UTF-8\" is not supported.", exception);
    }

    @Test
    public void sendCanHandleMultipleChunks() {
        // given
        String firstChunk = "some beacon string";
        String secondChunk = "some more beacon string";
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar()))
            .thenReturn(firstChunk, secondChunk);
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, true, false);

        HTTPClientProvider httpClientProvider = mock(HTTPClientProvider.class);
        HTTPClient httpClient = mock(HTTPClient.class);
        int responseCode = 200;
        StatusResponse firstResponse = StatusResponse.createSuccessResponse(
            mockLogger,
            ResponseAttributesImpl.withJsonDefaults().build(),
            responseCode,
            Collections.<String, List<String>>emptyMap()
        );
        StatusResponse secondResponse = StatusResponse.createSuccessResponse(
            mockLogger,
            ResponseAttributesImpl.withJsonDefaults().build(),
            responseCode,
            Collections.<String, List<String>>emptyMap()
        );
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class), any(AdditionalQueryParameters.class), anyInt()))
            .thenReturn(firstResponse, secondResponse);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        Beacon target = createBeacon().build();

        // when
        StatusResponse response = target.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(response, is(notNullValue()));
        assertThat(response, is(sameInstance(secondResponse)));

        verify(httpClient, times(2)).sendBeaconRequest(any(String.class), any(byte[].class), eq(mockAdditionalParameters), anyInt());

        verify(mockBeaconCache, times(1)).prepareDataForSending(any(BeaconKey.class));
        verify(mockBeaconCache, times(3)).hasDataForSending(any(BeaconKey.class));
        verify(mockBeaconCache, times(2)).getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// misc tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void beaconDataPrefixVS1() {
        // given
        int sessionSequence = 1213;
        int visitStoreVersion = 1;
        String appVersion = "1111";
        String ipAddress = "192.168.0.1";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("system");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("manufacturer");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("model");
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(null);
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        Beacon target = createBeacon().withIpAddress(ipAddress).withSessionSequenceNumber(sessionSequence).build();

        // when
        target.send(mock(HTTPClientProvider.class), null);

        // then
        String expectedPrefix = "vv=" + ProtocolConstants.PROTOCOL_VERSION +
            "&va=" + ProtocolConstants.OPENKIT_VERSION +
            "&ap=" + APP_ID +
            "&vn=" + appVersion +
            "&pt=" + ProtocolConstants.PLATFORM_TYPE_OPENKIT +
            "&tt=" + ProtocolConstants.AGENT_TECHNOLOGY_TYPE +
            "&vi=" + DEVICE_ID +
            "&sn=" + SESSION_ID +
            "&ip=" + ipAddress +
            "&os=system" +
            "&mf=manufacturer" +
            "&md=model" +
            "&dl=2" +
            "&cl=2" +
            "&vs=" + visitStoreVersion +
            "&tx=0" +
            "&tv=0" +
            "&mp=1";

        BeaconKey expectedBeaconKey = new BeaconKey(SESSION_ID, sessionSequence);
        verify(mockBeaconCache, times(1)).prepareDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1)).hasDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1))
            .getNextBeaconChunk(eq(expectedBeaconKey), eq(expectedPrefix), anyInt(), anyChar());
    }

    @Test
    public void beaconDataPrefixVS2() {
        // given
        int sessionSequence = 1213;
        int visitStoreVersion = 2;
        String appVersion = "1111";
        String ipAddress = "192.168.0.1";
        when(mockOpenKitConfiguration.getApplicationVersion()).thenReturn(appVersion);
        when(mockOpenKitConfiguration.getOperatingSystem()).thenReturn("system");
        when(mockOpenKitConfiguration.getManufacturer()).thenReturn("manufacturer");
        when(mockOpenKitConfiguration.getModelID()).thenReturn("model");
        when(mockBeaconCache.hasDataForSending(any(BeaconKey.class))).thenReturn(true, false);
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(null);
        when(mockServerConfiguration.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        Beacon target = createBeacon().withIpAddress(ipAddress).withSessionSequenceNumber(sessionSequence).build();

        // when
        target.send(mock(HTTPClientProvider.class), null);

        // then
        String expectedPrefix = "vv=" + ProtocolConstants.PROTOCOL_VERSION +
                "&va=" + ProtocolConstants.OPENKIT_VERSION +
                "&ap=" + APP_ID +
                "&vn=" + appVersion +
                "&pt=" + ProtocolConstants.PLATFORM_TYPE_OPENKIT +
                "&tt=" + ProtocolConstants.AGENT_TECHNOLOGY_TYPE +
                "&vi=" + DEVICE_ID +
                "&sn=" + SESSION_ID +
                "&ip=" + ipAddress +
                "&os=system" +
                "&mf=manufacturer" +
                "&md=model" +
                "&dl=2" +
                "&cl=2" +
                "&vs=" + visitStoreVersion +
                "&ss=" + sessionSequence +
                "&tx=0" +
                "&tv=0" +
                "&mp=1";

        BeaconKey expectedBeaconKey = new BeaconKey(SESSION_ID, sessionSequence);
        verify(mockBeaconCache, times(1)).prepareDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1)).hasDataForSending(eq(expectedBeaconKey));
        verify(mockBeaconCache, times(1))
            .getNextBeaconChunk(eq(expectedBeaconKey), eq(expectedPrefix), anyInt(), anyChar());
    }

    @Test
    public void clearDataFromBeaconCache() {
        // given
        BeaconCacheImpl beaconCache = new BeaconCacheImpl(mockLogger);
        Beacon target = createBeacon().with(beaconCache).build();
        // add various data
        BaseActionImpl action = mock(BaseActionImpl.class);
        when(action.getID()).thenReturn(ACTION_ID);
        when(action.getName()).thenReturn("actionName");
        target.addAction(action);
        target.reportValue(ACTION_ID, "IntValue", 42);
        target.reportValue(ACTION_ID, "LongValue", 42L);
        target.reportValue(ACTION_ID, "DoubleValue", 3.1415);
        target.reportValue(ACTION_ID, "StringValue", "HelloWorld");
        target.reportEvent(ACTION_ID, "SomeEvent");
        target.reportError(ACTION_ID, "SomeError", -123);
        target.reportCrash("SomeCrash", "SomeReason", "SomeStacktrace");
        target.endSession();

        // when
        target.clearData();

        // then (verify, all data is cleared)
        BeaconKey key = new BeaconKey(SESSION_ID, SESSION_SEQ_NO);
        String[] events = beaconCache.getEvents(key);
        assertThat(events, emptyArray());
        String[] actions = beaconCache.getActions(key);
        assertThat(actions, emptyArray());
        assertThat(target.isEmpty(), is(true));
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
    public void initializeServerConfigurationDelegatesToBeacon() {
        // given
        Beacon target = createBeacon().build();
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        reset(mockBeaconConfiguration);

        // when
        target.initializeServerConfiguration(serverConfig);

        // then
        verify(mockBeaconConfiguration, times(1)).initializeServerConfiguration(serverConfig);
        verifyNoMoreInteractions(mockBeaconConfiguration);
        verifyZeroInteractions(serverConfig);
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
    public void isDataCapturingEnabledReturnsFalseIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);
        boolean obtained = target.isDataCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isDataCapturingEnabledReturnsFalseIfTcValueEqualToTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(true);
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlValue);
        boolean obtained = target.isDataCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isDataCapturingEnabledReturnsFalseIfTcValueGreaterThanTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(true);
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlValue - 1);
        boolean obtained = target.isDataCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isDataCapturingEnabledReturnsTrueIfDataSendingIsAllowedAndTcValueGreaterThanTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(true);
        boolean obtained = target.isDataCapturingEnabled();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void isErrorCapturingEnabledReturnsFalseIfSendingErrorsIsDisallowed() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(false);
        boolean obtained = target.isErrorCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isErrorCapturingEnabledReturnsFalseIfTcValueEqualToTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlValue);
        boolean obtained = target.isErrorCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isErrorCapturingEnabledReturnsFalseIfTcValueGreaterThanTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlValue - 1);
        boolean obtained = target.isErrorCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isErrorCapturingEnabledReturnsTrueIfDataSendingIsAllowedAndTcValueGreaterThanTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        boolean obtained = target.isErrorCapturingEnabled();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void isCrashCapturingEnabledReturnsFalseIfSendingErrorsIsDisallowed() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(false);
        boolean obtained = target.isCrashCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isCrashCapturingEnabledReturnsFalseIfTcValueEqualToTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(true);
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlValue);
        boolean obtained = target.isCrashCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isCrashCapturingEnabledReturnsFalseIfTcValueGreaterThanTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(true);
        when(mockServerConfiguration.getTrafficControlPercentage()).thenReturn(trafficControlValue - 1);
        boolean obtained = target.isCrashCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isCrashCapturingEnabledReturnsTrueIfDataSendingIsAllowedAndTcValueGreaterThanTcPercentageFromServerConfig() {
        // given
        int trafficControlValue = 50;
        when(mockRandom.nextPercentageValue()).thenReturn(trafficControlValue);
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(true);
        boolean obtained = target.isCrashCapturingEnabled();

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

    @Test
    public void isActionReportingAllowedByPrivacySettingsReturnsSettingFromPrivacyConfiguration() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockPrivacyConfiguration.isActionReportingAllowed()).thenReturn(true);
        boolean obtained = target.isActionReportingAllowedByPrivacySettings();

        // then
        assertThat(obtained, is(true));
        verify(mockPrivacyConfiguration, times(1)).isActionReportingAllowed();

        // and when
        when(mockPrivacyConfiguration.isActionReportingAllowed()).thenReturn(false);
        obtained = target.isActionReportingAllowedByPrivacySettings();

        // then
        assertThat(obtained, is(false));
        verify(mockPrivacyConfiguration, times(2)).isActionReportingAllowed();
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
        builder.sessionSequenceNumber = SESSION_SEQ_NO;
        builder.random = mockRandom;
        builder.supplementaryBasicData = mockSupplementaryData;

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
        private SupplementaryBasicData supplementaryBasicData;

        private BeaconBuilder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        private BeaconBuilder with(BeaconCache beaconCache) {
            this.beaconCache = beaconCache;
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
            when(beaconInitializer.getSupplementaryBasicData()).thenReturn(supplementaryBasicData);

            return new Beacon(beaconInitializer, configuration);
        }
    }
}
