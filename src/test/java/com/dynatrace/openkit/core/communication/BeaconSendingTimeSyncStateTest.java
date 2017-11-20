package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.providers.TimeProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.InOrder;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingTimeSyncStateTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private HTTPClient httpClient;
    private BeaconSendingContext stateContext;

    @Before
    public void setUp() {

        httpClient = mock(HTTPClient.class);
        stateContext = mock(BeaconSendingContext.class);

        when(stateContext.isTimeSyncSupported()).thenReturn(true); // by set time sync support to enabled
        when(stateContext.getLastTimeSyncTime()).thenReturn(-1L);
        when(stateContext.getHTTPClient()).thenReturn(httpClient);
    }

    @After
    public void tearDown() {
        TimeProvider.initialize(0, false); // reset TimeProvider to default values
    }

    @Test
    public void timeSyncStateIsNotATerminalState() {

        // given
        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when/then
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void getShutdownStateGivesABeaconSendingTerminalStateInstance() {

        // given
        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        AbstractBeaconSendingState obtained = target.getShutdownState();

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(BeaconSendingTerminalState.class)));
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
    public void timeSyncNotRequiredAndCaptureOnTruePerformsStateTransitionToCaptureOnState() throws InterruptedException {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(false);
        when(stateContext.isCaptureOn()).thenReturn(true);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.doExecute(stateContext);

        // then
        assertThat(TimeProvider.isTimeSynced(), is(false));
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void timeSyncNotRequiredAndCaptureOnFalsePerformsStateTransitionToCaptureOffState() throws InterruptedException {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(false);
        when(stateContext.isCaptureOn()).thenReturn(false);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.doExecute(stateContext);

        // then
        assertThat(TimeProvider.isTimeSynced(), is(false));
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void timeSyncRequestsAreInterruptedAfterUnsuccessfulRetries() throws InterruptedException {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(null); // unsuccessful

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.doExecute(stateContext);

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
        target.doExecute(stateContext);

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
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200))
            // second time sync request (2 retries)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200))
            // third time sync request (3 retries)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200))
            // fourth time sync request (4 retries)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(null)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200))
            // fifth time sync request (0 retries)
            .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200));

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
        target.doExecute(stateContext);

        // then verify init was done
        assertThat(TimeProvider.isTimeSynced(), is(true));
        assertThat(TimeProvider.convertToClusterTime(0), is(2L));

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
    public void successfulTimeSyncInitializesTimeProvider() throws InterruptedException {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200));

        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when being executed
        target.doExecute(stateContext);

        // verify init was done
        assertThat(TimeProvider.isTimeSynced(), is(true));
        assertThat(TimeProvider.convertToClusterTime(0), is(2L));

        // verify number of method calls
        verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS)).sendTimeSyncRequest();
        verify(stateContext, times(2 * BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS + 1)).getCurrentTimestamp();

        verify(stateContext, times(1)).setLastTimeSyncTime(66L);
        verify(stateContext, times(0)).initCompleted(anyBoolean());
    }

    @Test
    public void successfulTimeSyncSetSuccessfulInitCompletionInContextWhenItIsInitialTimeSync() throws InterruptedException {

        // given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200));

        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when being executed
        target.doExecute(stateContext);

        // verify init was done
        assertThat(TimeProvider.isTimeSynced(), is(true));
        assertThat(TimeProvider.convertToClusterTime(0), is(2L));

        // verify number of method calls
        verify(httpClient, times(BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS)).sendTimeSyncRequest();
        verify(stateContext, times(2 * BeaconSendingTimeSyncState.TIME_SYNC_REQUESTS + 1)).getCurrentTimestamp();

        verify(stateContext, times(1)).setLastTimeSyncTime(66L);
        verify(stateContext, times(1)).initCompleted(true);
    }

    @Test
    public void timeSyncSupportIsDisabledIfBothTimeStampsInTimeSyncResponseAreNegative() throws InterruptedException {

        //given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=-1&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=-2", 200));

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when being executed
        target.doExecute(stateContext);

        // verify that time sync was disabled
        verify(stateContext, times(1)).disableTimeSyncSupport();
    }

    @Test
    public void timeSyncSupportIsDisabledIfFirstTimeStampInTimeSyncResponseIsNegative() throws InterruptedException {

        //given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=-1&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200));

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when being executed
        target.doExecute(stateContext);

        // verify that time sync was disabled
        verify(stateContext, times(1)).disableTimeSyncSupport();
    }

    @Test
    public void timeSyncSupportIsDisabledIfSecondTimeStampInTimeSyncResponseIsNegative() throws InterruptedException {

        //given
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=1&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=-1", 200));

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when being executed
        target.doExecute(stateContext);

        // verify that time sync was disabled
        verify(stateContext, times(1)).disableTimeSyncSupport();
    }

    @Test
    public void timeProviderInitializeIsCalledIfItIsAnInitialTimeSyncEvenWhenResponseIsErroneous() throws InterruptedException {

        // given
        TimeProvider.initialize(42, true); // explicitly initialize TimeProvider (verify that it's changed later)
        when(httpClient.sendTimeSyncRequest()).thenReturn(null);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState(true);

        // when
        target.doExecute(stateContext);

        // then
        assertThat(TimeProvider.isTimeSynced(), is(false));
        assertThat(TimeProvider.convertToClusterTime(0L), is(0L)); // if not overwritten the result should be 42
    }

    @Test
    public void stateTransitionToCaptureOffIsPerformedIfTimeSyncIsSupportedButFailed() throws InterruptedException {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(httpClient.sendTimeSyncRequest()).thenReturn(null);

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void stateTransitionIsPerformedToAppropriateStateIfTimeSyncIsSupportedAndCapturingIsEnabled() throws InterruptedException {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(stateContext.isCaptureOn()).thenReturn(true);
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200));
        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(AbstractBeaconSendingState.class));
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void stateTransitionIsPerformedToAppropriateStateIfTimeSyncIsSupportedAndCapturingIsDisabled() throws InterruptedException {

        // given
        when(stateContext.isTimeSyncSupported()).thenReturn(true);
        when(stateContext.isCaptureOn()).thenReturn(false);
        when(httpClient.sendTimeSyncRequest()).thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=6&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=7", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=20&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=22", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=40&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=41", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=48&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=50", 200))
                                              .thenReturn(new TimeSyncResponse(TimeSyncResponse.RESPONSE_KEY_REQUEST_RECEIVE_TIME + "=60&" + TimeSyncResponse.RESPONSE_KEY_RESPONSE_SEND_TIME + "=61", 200));
        when(stateContext.getCurrentTimestamp())
            .thenReturn(2L).thenReturn(8L) // times on client side for responseOne     --> time sync offset = 1
            .thenReturn(10L).thenReturn(23L) // times on client side for responseTwo   --> time sync offset = 4
            .thenReturn(32L).thenReturn(42L) // times on client side for responseThree --> time sync offset = 3
            .thenReturn(44L).thenReturn(52L) // times on client side for responseFour  --> time sync offset = 1
            .thenReturn(54L).thenReturn(62L) // times on client side for responseFive --> time sync offset = 2
            .thenReturn(66L); // time set as last time sync time

        BeaconSendingTimeSyncState target = new BeaconSendingTimeSyncState();

        // when
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(AbstractBeaconSendingState.class));
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }

    @Test
    public void stateTransitionToInitIsMadeIfInitialTimeSyncFails() throws InterruptedException {

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
        target.doExecute(stateContext);

        // then
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(AbstractBeaconSendingState.class));
        verify(stateContext, times(1)).setCurrentState(org.mockito.Matchers.any(BeaconSendingInitState.class));
    }
}
