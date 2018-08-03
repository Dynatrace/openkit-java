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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class BeaconSendingCaptureOnStateTest {

    private BeaconSendingContext mockContext;
    private SessionWrapper mockSession1Open;
    private SessionWrapper mockSession2Open;
    private SessionWrapper mockSession3Finished;
    private SessionWrapper mockSession4Finished;
    private SessionWrapper mockSession5New;
    private SessionWrapper mockSession6New;

    @Before
    public void setUp() {
        mockSession1Open = mock(SessionWrapper.class);
        mockSession2Open = mock(SessionWrapper.class);
        mockSession3Finished = mock(SessionWrapper.class);
        mockSession4Finished = mock(SessionWrapper.class);
        mockSession5New = mock(SessionWrapper.class);
        mockSession6New = mock(SessionWrapper.class);
        when(mockSession1Open.sendBeacon(any(HTTPClientProvider.class))).thenReturn(new StatusResponse(mock(Logger.class), "", 200, Collections.<String, List<String>>emptyMap()));
        when(mockSession2Open.sendBeacon(any(HTTPClientProvider.class))).thenReturn(new StatusResponse(mock(Logger.class), "", 404, Collections.<String, List<String>>emptyMap()));
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(true);
        when(mockSession1Open.getSession()).thenReturn(mock(SessionImpl.class));
        when(mockSession2Open.getSession()).thenReturn(mock(SessionImpl.class));
        when(mockSession3Finished.getSession()).thenReturn(mock(SessionImpl.class));
        when(mockSession4Finished.getSession()).thenReturn(mock(SessionImpl.class));
        when(mockSession5New.getSession()).thenReturn(mock(SessionImpl.class));
        when(mockSession6New.getSession()).thenReturn(mock(SessionImpl.class));

        HTTPClientProvider mockHTTPClientProvider = mock(HTTPClientProvider.class);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.getLastTimeSyncTime()).thenReturn(0L);
        when(mockContext.getCurrentTimestamp()).thenReturn(42L);
        when(mockContext.getAllNewSessions()).thenReturn(Collections.<SessionWrapper>emptyList());
        when(mockContext.getAllOpenAndConfiguredSessions()).thenReturn(Arrays.asList(mockSession1Open, mockSession2Open));
        when(mockContext.getAllFinishedAndConfiguredSessions()).thenReturn(Arrays.asList(mockSession3Finished, mockSession4Finished));
        when(mockContext.getHTTPClientProvider()).thenReturn(mockHTTPClientProvider);
    }

    @Test
    public void aBeaconSendingCaptureOnStateIsNotATerminalState() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        // verify that BeaconSendingCaptureOnState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingCaptureOnStateHasTerminalStateBeaconSendingFlushSessions() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void toStringReturnsStateName() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        // then
        assertThat(target.toString(), is(equalTo("CaptureOn")));
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToTimeSyncStateWhenLastSyncTimeIsNegative() {

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);
        when(mockContext.getLastTimeSyncTime()).thenReturn(-1L);

        // when calling execute
        target.execute(mockContext);

        // then verify that lastStatusCheckTime was updated and next state is time sync state
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToTimeSyncStateWhenCheckIntervalPassed() throws InterruptedException {

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(true);
        when(mockContext.getLastTimeSyncTime()).thenReturn(0L);
        when(mockContext.getCurrentTimestamp()).thenReturn(7500000L);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that lastStatusCheckTime was updated and next state is time sync state
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }

    @Test
    public void newSessionRequestsAreMadeForAllNewSessions() throws InterruptedException {

        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        BeaconConfiguration defaultConfiguration = new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF);

        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getAllNewSessions()).thenReturn(Arrays.asList(mockSession5New, mockSession6New));
        when(mockClient.sendNewSessionRequest())
            .thenReturn(new StatusResponse(mock(Logger.class), "mp=5", 200, Collections.<String, List<String>>emptyMap())) // first response valid
            .thenReturn(null); // second response invalid
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(true);
        when(mockSession5New.getBeaconConfiguration()).thenReturn(defaultConfiguration);
        when(mockSession6New.canSendNewSessionRequest()).thenReturn(true);
        when(mockSession6New.getBeaconConfiguration()).thenReturn(defaultConfiguration);

        ArgumentCaptor<BeaconConfiguration> beaconConfigurationArgumentCaptor = ArgumentCaptor.forClass(BeaconConfiguration.class);

        // when
        target.doExecute(mockContext);

        // verify for both new sessions a new session request has been made
        verify(mockClient, times(2)).sendNewSessionRequest();

        // verify first has been updated, second decreased
        verify(mockSession5New, times(1)).getBeaconConfiguration();
        verify(mockSession5New, times(1)).updateBeaconConfiguration(beaconConfigurationArgumentCaptor.capture());
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(0).getMultiplicity(), is(equalTo(5)));
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(0).getDataCollectionLevel(), is(equalTo(defaultConfiguration.getDataCollectionLevel())));
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(0).getCrashReportingLevel(), is(equalTo(defaultConfiguration.getCrashReportingLevel())));

        // verify for beacon 6 only the number of tries was decreased
        verify(mockSession6New, times(0)).getBeaconConfiguration();
        verify(mockSession6New, times(1)).decreaseNumNewSessionRequests();
    }

    @Test
    public void multiplicityIsSetToZeroIfNoFurtherNewSessionRequestsAreAllowed() throws InterruptedException {

        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        BeaconConfiguration defaultConfiguration = new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF);

        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getAllNewSessions()).thenReturn(Arrays.asList(mockSession5New, mockSession6New));
        when(mockClient.sendNewSessionRequest())
            .thenReturn(new StatusResponse(mock(Logger.class), "mp=5", 200, Collections.<String, List<String>>emptyMap())) // first response valid
            .thenReturn(null); // second response invalid
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(false);
        when(mockSession5New.getBeaconConfiguration()).thenReturn(defaultConfiguration);
        when(mockSession6New.canSendNewSessionRequest()).thenReturn(false);
        when(mockSession6New.getBeaconConfiguration()).thenReturn(defaultConfiguration);

        ArgumentCaptor<BeaconConfiguration> beaconConfigurationArgumentCaptor = ArgumentCaptor.forClass(BeaconConfiguration.class);

        // when
        target.doExecute(mockContext);

        // verify for no session a new session request has been made
        verify(mockClient, times(0)).sendNewSessionRequest();

        // verify bot sessions are updated
        verify(mockSession5New, times(1)).getBeaconConfiguration();
        verify(mockSession5New, times(1)).updateBeaconConfiguration(beaconConfigurationArgumentCaptor.capture());
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(0).getMultiplicity(), is(equalTo(0)));
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(0).getDataCollectionLevel(), is(equalTo(defaultConfiguration.getDataCollectionLevel())));
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(0).getCrashReportingLevel(), is(equalTo(defaultConfiguration.getCrashReportingLevel())));

        // verify for beacon 6 only the number of tries was decreased
        verify(mockSession6New, times(1)).getBeaconConfiguration();
        verify(mockSession6New, times(1)).updateBeaconConfiguration(beaconConfigurationArgumentCaptor.capture());
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(1).getMultiplicity(), is(equalTo(0)));
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(1).getDataCollectionLevel(), is(equalTo(defaultConfiguration.getDataCollectionLevel())));
        assertThat(beaconConfigurationArgumentCaptor.getAllValues().get(1).getCrashReportingLevel(), is(equalTo(defaultConfiguration.getCrashReportingLevel())));
    }

    @Test
    public void aBeaconSendingCaptureOnStateSendsFinishedSessions() throws InterruptedException {

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        when(mockSession3Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession4Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession3Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession4Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));

        // also verify that the session are removed
        verify(mockContext, times(1)).removeSession(mockSession3Finished);
        verify(mockContext, times(1)).removeSession(mockSession4Finished);
    }

    @Test
    public void aBeaconSendingCaptureOnStateClearsFinishedSessionsIfSendingIsNotAllowed() throws InterruptedException {

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        when(mockSession3Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession4Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(false);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(false);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession3Finished, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession4Finished, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));

        // also verify that the session are removed
        verify(mockContext, times(1)).removeSession(mockSession3Finished);
        verify(mockContext, times(1)).removeSession(mockSession4Finished);
    }

    @Test
    public void aBeaconSendingCaptureOnStateDoesNotRemoveFinishedSessionIfSendWasUnsuccessful() throws InterruptedException {

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        when(mockSession3Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(null);
        when(mockSession4Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession3Finished.isEmpty()).thenReturn(false);
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession3Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession4Finished, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));

        verify(mockContext, times(1)).getAllFinishedAndConfiguredSessions();
        verify(mockContext, times(0)).removeSession(any(SessionWrapper.class));
    }

    @Test
    public void aBeaconSendingCaptureOnStateContinuesWithNextFinishedSessionIfSendingWasUnsuccessfulButBeaconIsEmtpy() throws InterruptedException {

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        when(mockSession3Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession4Finished.sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class))).thenReturn(mock(StatusResponse.class));
        when(mockSession3Finished.isEmpty()).thenReturn(true);
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession3Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession4Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession3Finished, times(1)).clearCapturedData();
        verify(mockSession4Finished, times(1)).clearCapturedData();

        verify(mockContext, times(1)).getAllFinishedAndConfiguredSessions();
        verify(mockContext, times(1)).removeSession(mockSession3Finished);
        verify(mockContext, times(1)).removeSession(mockSession4Finished);
    }

    @Test
    public void aBeaconSendingCaptureOnStateSendsOpenSessionsIfNotExpired() throws InterruptedException {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(true);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession1Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession2Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockContext, times(1)).setLastOpenSessionBeaconSendTime(org.mockito.Matchers.anyLong());
    }

    @Test
    public void aBeaconSendingCaptureOnStateClearsOpenSessionDataIfSendingIsNotAllowed() throws InterruptedException {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(false);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(false);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession1Open, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession2Open, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession1Open, times(1)).clearCapturedData();
        verify(mockSession2Open, times(1)).clearCapturedData();
        verify(mockContext, times(1)).setLastOpenSessionBeaconSendTime(org.mockito.Matchers.anyLong());
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToTimeSyncStateIfSessionExpired() throws InterruptedException {

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();
        when(mockContext.getCurrentTimestamp()).thenReturn(72000042L);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockContext, times(1)).isTimeSyncSupported();
        verify(mockContext, times(1)).getCurrentTimestamp();
        verify(mockContext, times(2)).getLastTimeSyncTime();
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToCaptureOffStateWhenCapturingGotDisabled() {

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);

        // when calling execute
        target.execute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).handleStatusResponse(org.mockito.Matchers.any(StatusResponse.class));
        verify(mockContext, times(1)).isCaptureOn();

        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }
}
