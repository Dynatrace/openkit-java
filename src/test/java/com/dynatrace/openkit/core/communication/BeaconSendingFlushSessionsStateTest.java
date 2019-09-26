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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.Response;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeaconSendingFlushSessionsStateTest {

    private BeaconSendingContext mockContext;
    private SessionImpl mockSession1Open;
    private SessionImpl mockSession2Open;
    private SessionImpl mockSession3Closed;

    @Before
    public void setUp() {

        mockSession1Open = mock(SessionImpl.class);
        mockSession2Open = mock(SessionImpl.class);
        mockSession3Closed = mock(SessionImpl.class);
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(true);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(true);
        when(mockSession3Closed.isDataSendingAllowed()).thenReturn(true);

        StatusResponse mockStatusResponse = mock(StatusResponse.class);
        when(mockStatusResponse.getResponseCode()).thenReturn(200);

        when(mockSession1Open.sendBeacon(any(HTTPClientProvider.class))).thenReturn(mockStatusResponse);
        when(mockSession2Open.sendBeacon(any(HTTPClientProvider.class))).thenReturn(mockStatusResponse);
        when(mockSession3Closed.sendBeacon(any(HTTPClientProvider.class))).thenReturn(mockStatusResponse);

        HTTPClient mockHttpClient = mock(HTTPClient.class);
        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.getHTTPClient()).thenReturn(mockHttpClient);
        when(mockContext.getAllOpenAndConfiguredSessions()).thenReturn(Arrays.asList(mockSession1Open, mockSession2Open));
        when(mockContext.getAllFinishedAndConfiguredSessions()).thenReturn(Arrays.asList(mockSession3Closed,
            mockSession2Open, mockSession1Open));
    }

    @Test
    public void toStringReturnsTheStateName() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        // then
        assertThat(target.toString(), is(equalTo("FlushSessions")));
    }

    @Test
    public void aBeaconSendingFlushSessionsStateIsNotATerminalState() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        // verify that BeaconSendingCaptureOffState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingFlushSessionsStateHasTerminalStateBeaconSendingTerminalState() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        // verify that terminal state is BeaconSendingTerminalState
        assertThat(terminalState, is(instanceOf(BeaconSendingTerminalState.class)));
    }

    @Test
    public void aBeaconSendingFlushSessionsStateTransitionsToTerminalStateWhenDataIsSent() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        // when
        target.doExecute(mockContext);

        // verify transition to terminal state
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTerminalState.class));
    }

    @Test
    public void aBeaconSendingFlushSessionsClosesOpenSessions() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        // when
        target.doExecute(mockContext);

        // verify that open sessions are closed
        verify(mockSession1Open, times(1)).end();
        verify(mockSession2Open, times(1)).end();
    }

    @Test
    public void aBeaconSendingFlushSessionStateSendsAllOpenAndClosedBeacons() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        // when
        target.doExecute(mockContext);

        // verify that beacons are sent
        verify(mockSession1Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession2Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession3Closed, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
    }

    @Test
    public void aBeaconSendingFlushSessionStateDoesNotSendIfSendingIsNotAllowed() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(false);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(false);
        when(mockSession3Closed.isDataSendingAllowed()).thenReturn(false);

        // when
        target.doExecute(mockContext);

        // verify that beacons are not sent, but cleared
        verify(mockSession1Open, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession2Open, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession3Closed, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession1Open, times(1)).clearCapturedData();
        verify(mockSession2Open, times(1)).clearCapturedData();
        verify(mockSession3Closed, times(1)).clearCapturedData();
    }

    @Test
    public void aBeaconSendingFlushSessionStateStopsSendingIfTooManyRequestsResponseWasReceived() {

        // given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        StatusResponse tooManyRequestsReceived = mock(StatusResponse.class);
        when(tooManyRequestsReceived.isErroneousResponse()).thenReturn(true);
        when(tooManyRequestsReceived.getResponseCode()).thenReturn(Response.HTTP_TOO_MANY_REQUESTS);

        when(mockSession3Closed.sendBeacon(any(HTTPClientProvider.class))).thenReturn(tooManyRequestsReceived);

        // when
        target.doExecute(mockContext);

        // verify that beacons are not sent, but cleared
        verify(mockSession1Open, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession2Open, times(0)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession3Closed, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession1Open, times(1)).clearCapturedData();
        verify(mockSession2Open, times(1)).clearCapturedData();
        verify(mockSession3Closed, times(1)).clearCapturedData();
    }
}
