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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.AdditionalQueryParameters;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.ResponseAttributes;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BeaconSendingInitStateTest {

    private HTTPClient httpClient;
    private BeaconSendingContext stateContext;
    private StatusResponse statusResponse;
    private ResponseAttributes responseAttributes;

    @Before
    public void setUp() {

        httpClient = mock(HTTPClient.class);
        stateContext = mock(BeaconSendingContext.class);
        responseAttributes = mock(ResponseAttributes.class);
        statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseAttributes()).thenReturn(responseAttributes);

        // setup state context
        when(stateContext.getHTTPClient()).thenReturn(httpClient);
        // setup http client
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(statusResponse);
        // setup status response
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_OK);
        when(statusResponse.isErroneousResponse()).thenReturn(false);
    }

    @Test
    public void initStateIsNotATerminalState() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();

        // when/then
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void toStringReturnsTheStateName() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();

        // then
        assertThat(target.toString(), is(equalTo("Initial")));
    }

    @Test
    public void getShutdownStateGivesABeaconSendingTerminalStateInstance() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        AbstractBeaconSendingState obtained = target.getShutdownState();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(BeaconSendingTerminalState.class)));
    }

    @Test
    public void getShutdownStateAlwaysCreatesANewInstance() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();

        // when calling getShutDownState twice
        AbstractBeaconSendingState obtainedOne = target.getShutdownState();
        AbstractBeaconSendingState obtainedTwo = target.getShutdownState();

        // then
        assertThat(obtainedOne, is(notNullValue()));
        assertThat(obtainedTwo, is(notNullValue()));
        assertThat(obtainedOne, is(not(sameInstance(obtainedTwo))));
    }

    @Test
    public void onInterruptedCallsInitCompletedInContext() {

        // given
        BeaconSendingContext mockContext = mock(BeaconSendingContext.class);
        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.onInterrupted(mockContext);

        // then
        verify(mockContext, times(1)).initCompleted(false);
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void executeSetsLastOpenSessionBeaconSendTime() {

        // given
        when(stateContext.getCurrentTimestamp()).thenReturn(123456789L);
        when(stateContext.isShutdownRequested()).thenReturn(true);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setLastOpenSessionBeaconSendTime(123456789L);
    }

    @Test
    public void executeSetsLastStatusCheckTime() {

        // given
        when(stateContext.getCurrentTimestamp()).thenReturn(123456789L);
        when(stateContext.isShutdownRequested()).thenReturn(true); // requests are not executed, since shutdown is initially requested

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setLastStatusCheckTime(123456789L);
    }


    @Test
    public void initIsTerminatedIfShutdownRequestedWithValidResponse() {

        // given
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(mock(StatusResponse.class));
        when(stateContext.isShutdownRequested()).thenReturn(true);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).initCompleted(false);
        verify(stateContext, times(1)).setNextState(isA(BeaconSendingTerminalState.class));

    }

    @Test
    public void reinitializeSleepsBeforeSendingStatusRequestsAgain() throws InterruptedException {

        // given
        StatusResponse erroneousResponse = mock(StatusResponse.class);
        when(erroneousResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(erroneousResponse.isErroneousResponse()).thenReturn(true);

        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(erroneousResponse);
        when(stateContext.isShutdownRequested()).thenAnswer(new Answer<Boolean>() {
            private int count = 0;

            @Override
            public Boolean answer(InvocationOnMock invocation) {
                return count++ > 40;
            }
        });

        InOrder inOrder = inOrder(stateContext);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state multiple times (7 times)
        target.execute(stateContext);

        // then
        // verify sleeps
        verify(stateContext, times(41)).sleep(anyLong());
        // from first round
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between first and second attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.REINIT_DELAY_MILLISECONDS[0]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between second and third attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.REINIT_DELAY_MILLISECONDS[1]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between third and fourth attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.REINIT_DELAY_MILLISECONDS[2]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between fourth and fifth attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.REINIT_DELAY_MILLISECONDS[3]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between fifth and sixth attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.REINIT_DELAY_MILLISECONDS[4]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between sixth and seventh attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.REINIT_DELAY_MILLISECONDS[4]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
    }

    @Test
    public void sleepTimeIsDoubledBetweenStatusRequestRetries() throws InterruptedException {

        // given
        StatusResponse erroneousResponse = mock(StatusResponse.class);
        when(erroneousResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(erroneousResponse.isErroneousResponse()).thenReturn(true);

        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(erroneousResponse);
        when(stateContext.isShutdownRequested()).thenReturn(false, false, false, false, false, true);
        InOrder inOrder = inOrder(stateContext);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state
        target.execute(stateContext);

        // then
        verify(stateContext, times(5)).sleep(anyLong()); // verify it's five, since we have 5 further checks
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
    }

    @Test
    public void initialStatusRequestGivesUpWhenShutdownRequestIsSetDuringExecution() throws InterruptedException {

        // given
        StatusResponse erroneousResponse = mock(StatusResponse.class);
        when(erroneousResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(erroneousResponse.isErroneousResponse()).thenReturn(true);

        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(erroneousResponse);
        when(stateContext.isShutdownRequested()).thenReturn(false)
                                                .thenReturn(false)
                                                .thenReturn(true);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).initCompleted(false); // int completed with error
        verify(stateContext, times(1)).setNextState(isA(BeaconSendingTerminalState.class)); // state transition to terminal state

        // verify that the requests where sent N times - defined as constants in the state itself
        verify(stateContext, times(3)).getHTTPClient();
        verify(httpClient, times(3)).sendStatusRequest(any(AdditionalQueryParameters.class));

        // verify sleeps between each retry
        verify(stateContext, times(2)).sleep(anyLong());
    }

    @Test
    public void aSuccessfulStatusResponseSetsInitCompletedToTrueForCaptureOn() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();
        when(responseAttributes.isCapture()).thenReturn(true);

        // when
        target.execute(stateContext);

        // verify init completed is called
        verify(stateContext, times(1)).initCompleted(true);
    }

    @Test
    public void aSuccessfulStatusResponseSetsInitCompletedToTrueForCaptureOff() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();
        when(responseAttributes.isCapture()).thenReturn(false);

        // when
        target.execute(stateContext);

        // verify init completed is called
        verify(stateContext, times(1)).initCompleted(true);
    }

    @Test
    public void aSuccessfulStatusResponsePerformsStateTransitionToCaptureOnIfCapturingIsEnabled() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();
        when(stateContext.isCaptureOn()).thenReturn(true);

        // when
        target.execute(stateContext);

        // verify state transition
        verify(stateContext, times(1)).handleStatusResponse(statusResponse);
        verify(stateContext, times(1)).setNextState(isA(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void aSuccessfulStatusResponsePerformsStateTransitionToCaptureOffIfCapturingIsDisabled() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();
        when(stateContext.isCaptureOn()).thenReturn(false);

        // when
        target.execute(stateContext);

        // verify state transition
        verify(stateContext, times(1)).handleStatusResponse(statusResponse);
        verify(stateContext, times(1)).setNextState(isA(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void receivingTooManyRequestsResponseUsesSleepTimeFromResponse() throws InterruptedException {

        // given
        StatusResponse tooManyRequestsResponse = mock(StatusResponse.class);
        when(tooManyRequestsResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_TOO_MANY_REQUESTS);
        when(tooManyRequestsResponse.isErroneousResponse()).thenReturn(true);
        when(tooManyRequestsResponse.getRetryAfterInMilliseconds()).thenReturn(1234L * 1000L);
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(tooManyRequestsResponse);
        when(stateContext.isShutdownRequested()).thenReturn(false, true);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.execute(stateContext);

        // verify sleep was performed accordingly
        verify(stateContext, times(1)).sleep(1234L * 1000L);
    }

    @Test
    public void receivingTooManyRequestsResponseDisablesCapturing() {

        // given
        StatusResponse tooManyRequestsResponse = mock(StatusResponse.class);
        when(tooManyRequestsResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_TOO_MANY_REQUESTS);
        when(tooManyRequestsResponse.isErroneousResponse()).thenReturn(true);
        when(tooManyRequestsResponse.getRetryAfterInMilliseconds()).thenReturn(1234L * 1000L);
        when(httpClient.sendStatusRequest(any(AdditionalQueryParameters.class))).thenReturn(tooManyRequestsResponse);
        when(stateContext.isShutdownRequested()).thenReturn(false, true);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.execute(stateContext);

        // verify sleep was performed accordingly
        verify(stateContext, times(1)).disableCaptureAndClear();
    }
}
