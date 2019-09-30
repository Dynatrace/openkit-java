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

import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.Response;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BeaconSendingCaptureOffStateTest {

    private HTTPClient httpClient;
    private BeaconSendingContext mockContext;

    @Before
    public void setUp() {
        StatusResponse mockResponse = mock(StatusResponse.class);
        when(mockResponse.getResponseCode()).thenReturn(Response.HTTP_OK);
        when(mockResponse.isErroneousResponse()).thenReturn(false);

        httpClient = mock(HTTPClient.class);
        when(httpClient.sendStatusRequest()).thenReturn(mockResponse);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.getHTTPClient()).thenReturn(httpClient);
    }

    @Test
    public void aBeaconSendingCaptureOffStateIsNotATerminalState() {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        //verify that BeaconSendingCaptureOffState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void toStringReturnsTheStateName() {

        // given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        // then
        assertThat(target.toString(), is(equalTo("CaptureOff")));
    }

    @Test
    public void aBeaconSendingCaptureOffStateHasTerminalStateBeaconSendingFlushSessions() {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void aBeaconSendingCaptureOffStateTransitionsToCaptureOnStateWhenCapturingActive() {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();
        when(mockContext.isCaptureOn()).thenReturn(true);

        // when calling execute
        target.execute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCaptureAndClear();

        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(org.mockito.Matchers.anyLong());
        //verifyNoMoreInteractions(mockContext);
        verify(mockContext, times(1)).setNextState(isA(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void aBeaconSendingCaptureOffStateWaitsForGivenTime() throws InterruptedException {

        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState(12345L);
        when(mockContext.isCaptureOn()).thenReturn(true);

        // when calling execute
        target.execute(mockContext);

        // then verify the custom amount of time was waited
        verify(mockContext, times(1)).sleep(12345L);//wait for custom time
    }

    @Test
    public void aBeaconSendingCaptureOffStateStaysInOffStateWhenServerRespondsWithTooManyRequests() {

        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState(12345L);

        StatusResponse tooManyRequestsResponse = mock(StatusResponse.class);
        when(tooManyRequestsResponse.getResponseCode()).thenReturn(Response.HTTP_TOO_MANY_REQUESTS);
        when(tooManyRequestsResponse.isErroneousResponse()).thenReturn(true);
        when(tooManyRequestsResponse.getRetryAfterInMilliseconds()).thenReturn(1234L * 1000L);
        when(httpClient.sendStatusRequest()).thenReturn(tooManyRequestsResponse);
        when(mockContext.isCaptureOn()).thenReturn(false);
        ArgumentCaptor<BeaconSendingCaptureOffState> stateCaptor = ArgumentCaptor.forClass(BeaconSendingCaptureOffState.class);

        // when calling execute
        target.execute(mockContext);

        // then verify next state
        verify(mockContext, times(1)).setNextState(stateCaptor.capture());

        // get captured state
        List<BeaconSendingCaptureOffState> capturedStates = stateCaptor.getAllValues();
        BeaconSendingCaptureOffState capturedState = capturedStates.get(0);
        assertThat(capturedState.sleepTimeInMilliseconds, is(equalTo(1234L * 1000L)));
    }
}
