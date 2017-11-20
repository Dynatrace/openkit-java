package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InOrder;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingInitStateTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private HTTPClient httpClient;
    private BeaconSendingContext stateContext;

    @Before
    public void setUp() {

        httpClient = mock(HTTPClient.class);
        stateContext = mock(BeaconSendingContext.class);

        when(stateContext.getHTTPClient()).thenReturn(httpClient);
    }

    @Test
    public void initStateIsNotATerminalState() {

        // given
        BeaconSendingInitState target = new BeaconSendingInitState();

        // when/then
        assertThat(target.isTerminalState(), is(false));
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
    public void doExecuteSetsLastOpenSessionBeaconSendTime() throws InterruptedException {

        // given
        when(stateContext.getCurrentTimestamp()).thenReturn(123456789L);
        stateContext.requestShutdown(); // requests are not executed, since shutdown is initially requested

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).setLastOpenSessionBeaconSendTime(123456789L);
    }

    @Test
    public void doExecuteSetsLastStatusCheckTime() throws InterruptedException {

        // given
        when(stateContext.getCurrentTimestamp()).thenReturn(123456789L);
        stateContext.requestShutdown(); // requests are not executed, since shutdown is initially requested

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).setLastStatusCheckTime(123456789L);
    }

    @Test
    public void initialStatusRequestIsTriedSeveralTimesBeforeGivingUp() throws InterruptedException {

        // given
        when(httpClient.sendStatusRequest()).thenReturn(null); // always return null -> means erroneous response

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state
        target.doExecute(stateContext);

        // then
        // verify that the requests where sent N times - defined as constants in the state itself
        verify(stateContext, times(BeaconSendingInitState.MAX_INITIAL_STATUS_REQUEST_RETRIES + 1)).getHTTPClient();
        verify(httpClient, times(BeaconSendingInitState.MAX_INITIAL_STATUS_REQUEST_RETRIES + 1)).sendStatusRequest();

        // verify sleeps between each retry
        verify(stateContext, times(BeaconSendingInitState.MAX_INITIAL_STATUS_REQUEST_RETRIES)).sleep(anyLong());

        // verify init is not yet complete & no state transition was performed
        verify(stateContext, times(0)).initCompleted(anyBoolean());
        verify(stateContext, times(0)).setCurrentState(org.mockito.Matchers.any(BeaconSendingTerminalState.class)); // state transition to terminal state
    }

    @Test
    public void reInitializeSleepsBeforeSendingStatusRequestsAgain() throws InterruptedException {

        // given
        when(httpClient.sendStatusRequest()).thenReturn(null); // always return null -> means erroneous response
        InOrder inOrder = inOrder(stateContext);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state multiple times (7 times)
        target.doExecute(stateContext);
        target.doExecute(stateContext);
        target.doExecute(stateContext);
        target.doExecute(stateContext);
        target.doExecute(stateContext);
        target.doExecute(stateContext);
        target.doExecute(stateContext);

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
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.RE_INIT_DELAY_MILLISECONDS[0]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between second and third attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.RE_INIT_DELAY_MILLISECONDS[1]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between third and fourth attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.RE_INIT_DELAY_MILLISECONDS[2]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between fourth and fifth attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.RE_INIT_DELAY_MILLISECONDS[3]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between fifth and sixth attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.RE_INIT_DELAY_MILLISECONDS[4]);
        // and again the sequence
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
        // delay between sixth and seventh attempt
        inOrder.verify(stateContext).sleep(BeaconSendingInitState.RE_INIT_DELAY_MILLISECONDS[4]);
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
        when(httpClient.sendStatusRequest()).thenReturn(null); // always return null -> means erroneous response
        InOrder inOrder = inOrder(stateContext);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state
        target.doExecute(stateContext);

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
        when(httpClient.sendStatusRequest()).thenReturn(null);
        when(stateContext.isShutdownRequested()).thenReturn(false)
                                                .thenReturn(false)
                                                .thenReturn(true);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when executing the state
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).initCompleted(false); // int completed with error
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingTerminalState.class)); // state transition to terminal state

        // verify that the requests where sent N times - defined as constants in the state itself
        verify(stateContext, times(3)).getHTTPClient();
        verify(httpClient, times(3)).sendStatusRequest();

        // verify sleeps between each retry
        verify(stateContext, times(2)).sleep(anyLong());
    }

    @Test
    public void aSuccessfulStatusResponsePerformsStateTransitionToTimeSyncState() throws InterruptedException {

        // given
        StatusResponse statusResponse = mock(StatusResponse.class);
        when(httpClient.sendStatusRequest()).thenReturn(statusResponse);

        BeaconSendingInitState target = new BeaconSendingInitState();

        // when
        target.doExecute(stateContext);

        // verify state transition
        verify(stateContext, times(1)).handleStatusResponse(statusResponse);
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }
}
