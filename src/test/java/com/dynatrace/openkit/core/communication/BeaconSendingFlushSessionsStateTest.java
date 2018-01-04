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

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

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

        StatusResponse mockResponse = mock(StatusResponse.class);

        HTTPClient mockHttpClient = mock(HTTPClient.class);
        when(mockHttpClient.sendStatusRequest()).thenReturn(mockResponse);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.getHTTPClient()).thenReturn(mockHttpClient);
        when(mockContext.getAllOpenSessions()).thenReturn(new SessionImpl[]{mockSession1Open, mockSession2Open});
        when(mockContext.getNextFinishedSession()).thenReturn(mockSession3Closed)
                                                  .thenReturn(mockSession2Open)
                                                  .thenReturn(mockSession1Open)
                                                  .thenReturn(null);
    }

    @Test
    public void aBeaconSendingFlushSessionsStateIsNotATerminalState() {

        //given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        //verify that BeaconSendingCaptureOffState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingFlushSessionsStateHasTerminalStateBeaconSendingFlushSessions() {

        //given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingTerminalState.class)));
    }

    @Test
    public void aBeaconSendingFlushSessionsTransitionsToTimeSyncStateWhenCapturingNotActive() {

        //given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        //when
        target.doExecute(mockContext);

        // verify transition to terminal state
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTerminalState.class));
    }

    @Test
    public void aBeaconSendingFlushSessionsClosesOpenSessions() {
        //given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        //when
        target.doExecute(mockContext);

        //verify that open sessions are closed
        verify(mockSession1Open, times(1)).end();
        verify(mockSession2Open, times(1)).end();
    }

    @Test
    public void aBeaconSendingFlushSessionsEndsAllSessions() {
        //given
        BeaconSendingFlushSessionsState target = new BeaconSendingFlushSessionsState();

        //when
        target.doExecute(mockContext);

        //verify that open sessions are closed
        verify(mockSession1Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession2Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
        verify(mockSession3Closed, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class));
    }

}
