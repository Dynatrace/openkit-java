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

import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.providers.TimingProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class BeaconSendingTimeSyncStateTest {

    private HTTPClient httpClient;
    private BeaconSendingContext stateContext;
    private TimingProvider timingProvider;

    @Before
    public void setUp() {

        httpClient = mock(HTTPClient.class);
        stateContext = mock(BeaconSendingContext.class);
        timingProvider = mock(TimingProvider.class);

        // by default true is returned
        when(timingProvider.isTimeSyncSupported()).thenReturn(true);

        when(stateContext.isTimeSyncSupported()).thenReturn(true); // by set time sync support to enabled
        when(stateContext.getLastTimeSyncTime()).thenReturn(-1L);
        when(stateContext.getHTTPClient()).thenReturn(httpClient);
    }

    @Test
    public void timeSyncStateIsNotATerminalState() {

        // given
        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when/then
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void getShutdownStateGivesATerminalStateInstanceForInitialTimeSync() {

        // given
        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when
        AbstractBeaconSendingState obtained = target.getShutdownState();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(BeaconSendingTerminalState.class)));
    }

    @Test
    public void getShutdownStateGivesAFlushSessionsStateInstanceForNotInitialTimeSync() {

        // given
        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        AbstractBeaconSendingState obtained = target.getShutdownState();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void onInterruptedSetsCallsInitCompletedInContextOnlyForInitialTimeSync() {

        // when target sets initialTimeSync to false (which is the default value).
        new BeaconSendingTimeSyncState().onInterrupted(stateContext);

        // then
        verifyZeroInteractions(stateContext);

        // when target set initialTimeSync explicitly to false
        new BeaconSendingTimeSyncState(false).onInterrupted(stateContext);

        // then
        verifyZeroInteractions(stateContext);

        // when target set initialTimeSync explicitly to true
        new BeaconSendingTimeSyncState(true).onInterrupted(stateContext);

        // then
        verify(stateContext, times(1)).initCompleted(false);
        verifyNoMoreInteractions(stateContext);
    }

    @Test
    public void isTimeSyncRequiredReturnsFalseImmediatelyIfTimeSyncIsNotSupported() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(false);
        when(stateContext.getLastTimeSyncTime()).thenReturn(-1L);

        // when/then
        assertThat(BeaconSendingTimeSyncState.isTimeSyncRequired(stateContext), is(false));
    }

    @Test
    public void timeSyncIsRequiredWhenLastTimeSyncTimeIsNegative() {

        // given
        when(stateContext.getLastTimeSyncTime()).thenReturn(-1L);

        // when/then
        assertThat(BeaconSendingTimeSyncState.isTimeSyncRequired(stateContext), is(true));
    }

    @Test
    public void isTimeSyncRequiredBoundaries() {

        // given
        when(stateContext.getLastTimeSyncTime()).thenReturn(0L);

        // when the last sync time is TIME_SYNC_INTERVAL_IN_MILLIS - 1 milliseconds ago
        when(stateContext.getCurrentTimestamp()).thenReturn(BeaconSendingTimeSyncState.TIME_SYNC_INTERVAL_IN_MILLIS - 1);

        // then
        assertThat(BeaconSendingTimeSyncState.isTimeSyncRequired(stateContext), is(false));

        // when the last sync time is TIME_SYNC_INTERVAL_IN_MILLIS milliseconds ago
        when(stateContext.getCurrentTimestamp()).thenReturn(BeaconSendingTimeSyncState.TIME_SYNC_INTERVAL_IN_MILLIS);

        // then
        assertThat(BeaconSendingTimeSyncState.isTimeSyncRequired(stateContext), is(false));

        // when the last sync time is TIME_SYNC_INTERVAL_IN_MILLIS + 1 milliseconds ago
        when(stateContext.getCurrentTimestamp()).thenReturn(BeaconSendingTimeSyncState.TIME_SYNC_INTERVAL_IN_MILLIS + 1);

        // then
        assertThat(BeaconSendingTimeSyncState.isTimeSyncRequired(stateContext), is(true));
    }

    @Test
    public void timeSyncNotRequiredAndCaptureOnTruePerformsStateTransitionToCaptureOnState() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(false);
        when(stateContext.isCaptureOn()).thenReturn(true);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void timeSyncNotRequiredAndCaptureOnFalsePerformsStateTransitionToCaptureOffState() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(false);
        when(stateContext.isCaptureOn()).thenReturn(false);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void timeSyncRequestsAreInterruptedAfterUnsuccessfulRetries() {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(null); // unsuccessful

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS + 1)).sendTimeSyncRequest();
    }

    @Test
    public void sleepTimeDoublesBetweenConsecutiveTimeSyncRequests() throws InterruptedException {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(null); // unsuccessful
        InOrder inOrder = inOrder(stateContext);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(5)).sleep(anyLong()); // verify it's five, since we have 5 further checks
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 16);
    }

    @Test
    public void sleepTimeIsResetToInitialValueAfterASuccessfulTimeSyncResponse() throws InterruptedException {

        // given
        when(httpClient.sendTimeSyncRequest())
            // first time sync request (1 retry)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200, Collections.<String, List<String>>emptyMap()))
            // second time sync request (2 retries)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200, Collections.<String, List<String>>emptyMap()))
            // third time sync request (3 retries)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200, Collections.<String, List<String>>emptyMap()))
            // fourth time sync request (4 retries)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200, Collections.<String, List<String>>emptyMap()))
            // fifth time sync request (0 retries)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200, Collections.<String, List<String>>emptyMap()));

        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        InOrder inOrder = inOrder(stateContext);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then verify init was done
        verify(stateContext, times(1)).initializeTimeSync(2L, true);

        // and verify method calls
        verify(stateContext, times(10)).sleep(anyLong()); // verify it's four, since we have 4 further checks
        // first time sync request -> 1 retry
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        // second time sync request -> 2 retries
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        // third time sync request -> 3 retries
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        // fourth time sync request -> 4 retries
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 2);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 4);
        inOrder.verify(stateContext).sleep(BeaconSendingTimeSyncState.INITIAL_RETRY_SLEEP_TIME_MILLISECONDS * 8);
        // fifth time sync request -> 0 retries

        verify(stateContext, times(1)).setLastTimeSyncTime(66L);
        verify(stateContext, times(0)).initCompleted(anyBoolean());
    }

    @Test
    public void successfulTimeSyncInitializesTimeProvider() {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200, Collections.<String, List<String>>emptyMap()));

        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when being executed
        target.execute(stateContext);

        // verify init was done
        verify(stateContext, times(1)).initializeTimeSync(2L, true);

        // verify number of method calls
        verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS)).sendTimeSyncRequest();
        verify(stateContext, times(2 * BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS + 1)).getCurrentTimestamp();

        verify(stateContext, times(1)).setLastTimeSyncTime(66L);
        verify(stateContext, times(0)).initCompleted(anyBoolean());
    }

    @Test
    public void successfulTimeSyncSetSuccessfulInitCompletionInContextWhenItIsInitialTimeSync() {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200, Collections.<String, List<String>>emptyMap()));

        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when being executed
        target.execute(stateContext);

        // verify init was done
        verify(stateContext, times(1)).initializeTimeSync(2L, true);

        // verify number of method calls
        verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS)).sendTimeSyncRequest();
        verify(stateContext, times(2 * BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS + 1)).getCurrentTimestamp();

        verify(stateContext, times(1)).setLastTimeSyncTime(66L);
        verify(stateContext, times(1)).initCompleted(true);
    }

    @Test
    public void timeSyncSupportIsDisabledIfBothTimeStampsInTimeSyncResponseAreNegative() {

        //given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=-1&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=-2", 200, Collections.<String, List<String>>emptyMap()));

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when being executed
        target.execute(stateContext);

        // verify that time sync was disabled
        verify(stateContext, times(1)).disableTimeSyncSupport();
    }

    @Test
    public void timeSyncSupportIsDisabledIfFirstTimeStampInTimeSyncResponseIsNegative() {

        //given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=-1&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200, Collections.<String, List<String>>emptyMap()));

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when being executed
        target.execute(stateContext);

        // verify that time sync was disabled
        verify(stateContext, times(1)).disableTimeSyncSupport();
    }

    @Test
    public void timeSyncSupportIsDisabledIfSecondTimeStampInTimeSyncResponseIsNegative() {

        //given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=1&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=-1", 200, Collections.<String, List<String>>emptyMap()));

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when being executed
        target.execute(stateContext);

        // verify that time sync was disabled
        verify(stateContext, times(1)).disableTimeSyncSupport();
    }

    @Test
    public void timeProviderInitializeIsCalledIfItIsAnInitialTimeSyncEvenWhenResponseIsErroneous() {

        // given
        timingProvider.initialize(42, true); // explicitly initialize TimeProvider (verify that it's changed later)
        when(httpClient.sendTimeSyncRequest()).thenReturn(null);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).initializeTimeSync(0L, true);
    }

    @Test
    public void stateTransitionToCaptureOffIsPerformedIfTimeSyncIsSupportedButFailed() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(httpClient.sendTimeSyncRequest()).thenReturn(null);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void stateTransitionIsPerformedToAppropriateStateIfTimeSyncIsSupportedAndCapturingIsEnabled() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(stateContext.isCaptureOn()).thenReturn(true);
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200, Collections.<String, List<String>>emptyMap()));
        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(AbstractBeaconSendingState.class));
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void stateTransitionIsPerformedToAppropriateStateIfTimeSyncIsSupportedAndCapturingIsDisabled() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(stateContext.isCaptureOn()).thenReturn(false);
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200, Collections.<String, List<String>>emptyMap()))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200, Collections.<String, List<String>>emptyMap()));
        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(AbstractBeaconSendingState.class));
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void stateTransitionToInitIsMadeIfInitialTimeSyncFails() {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(stateContext.isCaptureOn()).thenReturn(true);
        when(httpClient.sendTimeSyncRequest()).thenReturn(null);
        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L)
            .thenReturn(10L).thenReturn(23L)
            .thenReturn(32L).thenReturn(42L)
            .thenReturn(44L).thenReturn(52L)
            .thenReturn(54L).thenReturn(62L)
            .thenReturn(66L);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when
        target.execute(stateContext);

        // then
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(AbstractBeaconSendingState.class));
        verify(stateContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingInitState.class));
    }
}
