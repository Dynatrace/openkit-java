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
import com.dynatrace.openkit.core.objects.OpenKitComposite;
import com.dynatrace.openkit.core.objects.WebRequestTracerBaseImpl;
import com.dynatrace.openkit.core.util.CrashFormatter;
import com.dynatrace.openkit.core.util.PercentEncoder;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.RandomNumberGenerator;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
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
    private static final int SESSION_SEQ_NO = 13;

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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        when(mockServerConfiguration.getMultiplicity()).thenReturn(1);
        when(mockServerConfiguration.isSendingErrorsAllowed()).thenReturn(true);
        when(mockServerConfiguration.isSendingCrashesAllowed()).thenReturn(true);
        when(mockServerConfiguration.getServerID()).thenReturn(SERVER_ID);
        when(mockServerConfiguration.getBeaconSizeInBytes()).thenReturn(30 * 1024); // 30kB

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
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn("dummy");

        target.send(httpClientProvider, mockAdditionalParameters);

        // then
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient, times(1))
                .sendBeaconRequest(ipCaptor.capture(), any(byte[].class), eq(mockAdditionalParameters));

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
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn("dummy");

        target.send(httpClientProvider, mockAdditionalParameters);

        // then
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient, times(1))
                .sendBeaconRequest(ipCaptor.capture(), any(byte[].class), eq(mockAdditionalParameters));

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
    public void sessionStartIsNotReportedReportedIfDataSendingIsDisallowed() {
        // given
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);
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
        String causeStackTrace = PercentEncoder.encode(crashFormatter.getStackTrace(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);

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
        String stacktrace = PercentEncoder.encode(crashFormatter.getStackTrace(), Beacon.CHARSET, Beacon.RESERVED_CHARACTERS);

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
        when(httpClient.sendBeaconRequest(any(String.class), any(byte[].class), any(AdditionalQueryParameters.class)))
                .thenReturn(successResponse);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        // when (add data and try to send it)
        target.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = target.send(httpClientProvider, mockAdditionalParameters);

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
        Beacon target = createBeacon()
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
        target.reportCrash("errorName", "errorReason", "errorStackTrace");
        StatusResponse response = target.send(httpClientProvider, mockAdditionalParameters);

        // then
        assertThat(response, notNullValue());
        assertThat(response.getResponseCode(), is(responseCode));
        verify(httpClient, times(1)).sendBeaconRequest(eq(ipAddress), any(byte[].class), eq(mockAdditionalParameters));
    }

    @Test
    public void sendCatchesUnsupportedEncodingException() {
        // given
        String beaconChunk = "some beacon string";
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(beaconChunk);

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

        verify(mockBeaconCache, times(1))
            .getNextBeaconChunk(eq(new BeaconKey(SESSION_ID, sessionSequence)), eq(expectedPrefix), anyInt(), anyChar());
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
        when(mockBeaconCache.getNextBeaconChunk(any(BeaconKey.class), anyString(), anyInt(), anyChar())).thenReturn(null);
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

        verify(mockBeaconCache, times(1))
                .getNextBeaconChunk(eq(new BeaconKey(SESSION_ID, sessionSequence)), eq(expectedPrefix), anyInt(), anyChar());
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
    public void isDataCaptureEnabledReturnsFalseIfDataSendingIsDisallowed() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(false);
        boolean obtained = target.isDataCapturingEnabled();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void isDataCaptureEnabledReturnsTrueIfDataSendingIsAllowed() {
        // given
        Beacon target = createBeacon().build();

        // when
        when(mockServerConfiguration.isSendingDataAllowed()).thenReturn(true);
        boolean obtained = target.isDataCapturingEnabled();

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
