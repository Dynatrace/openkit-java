package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class BeaconSendingContextTest {

    private Configuration configuration;
    private HTTPClientProvider httpClientProvider;
    private TimingProvider timingProvider;
    private AbstractBeaconSendingState mockState;

    @Before
    public void setUp() {

        configuration = mock(Configuration.class);
        httpClientProvider = mock(HTTPClientProvider.class);
        timingProvider = mock(TimingProvider.class);
        mockState = mock(AbstractBeaconSendingState.class);
    }

    @Test
    public void currentStateIsInitializedAccordingly() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        assertThat(target.getCurrentState(), notNullValue());
        assertThat(target.getCurrentState(), instanceOf(BeaconSendingInitState.class));
    }

    @Test
    public void setCurrentStateChangesState() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.setNextState(mockState);

        assertThat(target.getCurrentState(), is(sameInstance(mockState)));
    }

    @Test
    public void executeCurrentStateCallsExecuteOnCurrentState() {


        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        target.setNextState(mockState);

        verifyZeroInteractions(mockState);

        target.executeCurrentState();

        verify(mockState, times(1)).execute(target);
    }

    @Test
    public void initCompleteSuccessAndWait() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.initCompleted(true);
        boolean obtained = target.waitForInit();

        assertThat(obtained, is(true));
    }

    @Test
    public void requestShutdown() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        assertThat(target.isShutdownRequested(), is(false));

        target.requestShutdown();

        assertThat(target.isShutdownRequested(), is(true));
    }

    @Test
    public void initCompleteFailureAndWait() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.initCompleted(false);
        boolean obtained = target.waitForInit();

        assertThat(obtained, is(false));
    }

    @Test
    public void waitForInitCompleteTimeout() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void waitForInitCompleteWhenInitCompletedSuccessfully() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        target.initCompleted(true);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void waitForInitCompleteWhenInitCompletedNotSuccessfully() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        target.initCompleted(false);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void aDefaultConstructedContextIsNotInitialized() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // then
        assertThat(target.isInitialized(), is(false));
    }

    @Test
    public void successfullyInitializedContextIsInitialized() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when initialized
        target.initCompleted(true);

        // then
        assertThat(target.isInitialized(), is(true));
    }

    @Test
    public void isInTerminalStateChecksCurrentState() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        AbstractBeaconSendingState nonTerminalState = mock(AbstractBeaconSendingState.class);
        when(nonTerminalState.isTerminalState()).thenReturn(false);
        AbstractBeaconSendingState terminalState = mock(AbstractBeaconSendingState.class);
        when(terminalState.isTerminalState()).thenReturn(true);

        // when non-terminal state is current state
        target.setNextState(nonTerminalState);

        // then
        assertThat(target.isInTerminalState(), is(false));

        // and when terminal state is current state
        target.setNextState(terminalState);

        // then
        assertThat(target.isInTerminalState(), is(true));

        // verify interactions with mock
        verify(nonTerminalState, times(1)).isTerminalState();
        verify(terminalState, times(1)).isTerminalState();
        verifyNoMoreInteractions(nonTerminalState, terminalState);
    }

    @Test
    public void isCaptureOnReturnsValueFromConfiguration() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when capturing is enabled
        when(configuration.isCapture()).thenReturn(true);

        // then
        assertThat(target.isCaptureOn(), is(true));

        // and when capturing is disabled
        when(configuration.isCapture()).thenReturn(false);

        // then
        assertThat(target.isCaptureOn(), is(false));

        // verify call count
        verify(configuration, times(2)).isCapture();
    }

    @Test
    public void timeSyncIsNotSupportedIfDisabled() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when
        target.disableTimeSyncSupport();

        // then
        assertThat(target.isTimeSyncSupported(), is(false));
    }

    @Test
    public void setAndGetLastOpenSessionBeaconSendTime() {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        target.setLastOpenSessionBeaconSendTime(1234L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(1234L));

        target.setLastOpenSessionBeaconSendTime(5678L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(5678L));
    }

    @Test
    public void setAndGetLastStatusCheckTime() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when
        target.setLastStatusCheckTime(1234L);

        // then
        assertThat(target.getLastStatusCheckTime(), is(1234L));

        // and when
        target.setLastStatusCheckTime(5678L);

        // then
        assertThat(target.getLastStatusCheckTime(), is(5678L));
    }

    @Test
    public void getSendIntervalRetrievesItFromConfiguration() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        when(configuration.getSendInterval()).thenReturn(1234);

        // when
        int obtained = target.getSendInterval();

        // then
        assertThat(obtained, is(1234));
        verify(configuration, times(1)).getSendInterval();
        verifyNoMoreInteractions(configuration);
    }

    @Test
    public void getHTTPClientProvider() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when
        HTTPClientProvider obtained = target.getHTTPClientProvider();

        // then
        assertThat(obtained, is(sameInstance(httpClientProvider)));
    }

    @Test
    public void testGetHTTPClient() {

        HTTPClient mockClient = mock(HTTPClient.class);
        HTTPClientConfiguration mockConfiguration = mock(HTTPClientConfiguration.class);

        when(configuration.getHttpClientConfig()).thenReturn(mockConfiguration);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(mockClient);

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(configuration, httpClientProvider);

        HTTPClient obtained = target.getHTTPClient();

        assertThat(obtained, notNullValue());
        assertThat(obtained, is(sameInstance(mockClient)));

        verify(configuration, times(1)).getHttpClientConfig();
        verify(httpClientProvider, times(1)).createClient(mockConfiguration);
        verifyNoMoreInteractions(configuration, httpClientProvider);
        verifyZeroInteractions(mockClient, mockConfiguration);
    }

    @Test
    public void getCurrentTimestamp() {

        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(1234567890L);

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(timingProvider);

        long obtained = target.getCurrentTimestamp();

        assertThat(obtained, is(1234567890L));
        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepDefaultTime() throws InterruptedException {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(timingProvider);

        target.sleep();

        verify(timingProvider, times(1)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepWithGivenTime() throws InterruptedException {

        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        verifyZeroInteractions(timingProvider);

        target.sleep(1234L);

        verify(timingProvider, times(1)).sleep(1234L);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void defaultLastTimeSyncTimeIsMinusOne() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // then
        assertThat(target.getLastTimeSyncTime(), is(-1L));
    }

    @Test
    public void getAndSetLastTimeSyncTime() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when setting first value
        target.setLastTimeSyncTime(1234L);

        // then
        assertThat(target.getLastTimeSyncTime(), is(1234L));

        // and when setting other value
        target.setLastTimeSyncTime(4321L);

        // then
        assertThat(target.getLastTimeSyncTime(), is(4321L));
    }

    @Test
    public void aDefaultConstructedContextDoesNotStoreAnySessions() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // then
        assertThat(target.getAllOpenSessions(), is(emptyArray()));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));
    }

    @Test
    public void startingASessionAddsTheSessionToOpenSessions() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        // when starting first session
        target.startSession(mockSessionOne);

        // then
        assertThat(target.getAllOpenSessions(), is(equalTo(new SessionImpl[] { mockSessionOne })));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));

        // when starting second sessions
        target.startSession(mockSessionTwo);

        // then
        assertThat(target.getAllOpenSessions(), is(equalTo(new SessionImpl[] { mockSessionOne, mockSessionTwo })));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));
    }

    @Test
    public void finishingASessionMovesSessionToFinishedSessions() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        target.startSession(mockSessionOne);
        target.startSession(mockSessionTwo);

        // when finishing the first session
        target.finishSession(mockSessionOne);

        // then
        assertThat(target.getAllOpenSessions(), is(equalTo(new SessionImpl[] { mockSessionTwo })));
        assertThat(target.getAllFinishedSessions(), is(equalTo(new SessionImpl[] { mockSessionOne })));

        // and when finishing the second session
        target.finishSession(mockSessionTwo);

        // then
        assertThat(target.getAllOpenSessions(), is(emptyArray()));
        assertThat(target.getAllFinishedSessions(), is(equalTo(new SessionImpl[] { mockSessionOne, mockSessionTwo })));
    }

    @Test
    public void finishingASessionThatHasNotBeenStartedBeforeIsNotAddedToFinishedSessions() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSession = mock(SessionImpl.class);

        // when the session is not started, but immediately finished
        target.finishSession(mockSession);

        // then
        assertThat(target.getAllOpenSessions(), is(emptyArray()));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));
    }

    @Test
    public void getNextFinishedSessionGetsAndRemovesSession() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        target.startSession(mockSessionOne);
        target.finishSession(mockSessionOne);
        target.startSession(mockSessionTwo);
        target.finishSession(mockSessionTwo);

        // when retrieving the next finished session
        SessionImpl obtained = target.getNextFinishedSession();

        // then
        assertThat(obtained, is(sameInstance(mockSessionOne)));
        assertThat(target.getAllFinishedSessions(), is(equalTo(new SessionImpl[] { mockSessionTwo })));

        // and when retrieving the next finished Session
        obtained = target.getNextFinishedSession();

        // then
        assertThat(obtained, is(sameInstance(mockSessionTwo)));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));
    }

    @Test
    public void getNextFinishedSessionReturnsNullIfThereAreNoFinishedSessions() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when, then
        assertThat(target.getNextFinishedSession(), is(nullValue()));
    }

    @Test
    public void disableCapture() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);
        SessionImpl mockSessionThree = mock(SessionImpl.class);
        SessionImpl mockSessionFour = mock(SessionImpl.class);

        target.startSession(mockSessionOne);
        target.finishSession(mockSessionOne);
        target.startSession(mockSessionTwo);
        target.startSession(mockSessionThree);
        target.startSession(mockSessionFour);
        target.finishSession(mockSessionFour);

        // when
        target.disableCapture();

        // then
        assertThat(target.getAllOpenSessions(), is(equalTo(new SessionImpl[] { mockSessionTwo, mockSessionThree })));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));

        verify(configuration, times(1)).disableCapture();
        verify(mockSessionOne, times(1)).clearCapturedData();
        verify(mockSessionTwo, times(1)).clearCapturedData();
        verify(mockSessionThree, times(1)).clearCapturedData();
        verify(mockSessionFour, times(1)).clearCapturedData();
        verifyNoMoreInteractions(configuration, mockSessionOne, mockSessionTwo, mockSessionThree, mockSessionFour);
    }

    @Test
    public void handleStatusResponseWhenCapturingIsEnabled() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        target.startSession(mockSessionOne);
        target.finishSession(mockSessionOne);
        target.startSession(mockSessionTwo);

        StatusResponse mockStatusResponse = mock(StatusResponse.class);

        when(configuration.isCapture()).thenReturn(true);

        // when
        target.handleStatusResponse(mockStatusResponse);

        // then
        assertThat(target.getAllOpenSessions(), is(equalTo(new SessionImpl[] { mockSessionTwo })));
        assertThat(target.getAllFinishedSessions(), is(equalTo(new SessionImpl[] { mockSessionOne })));

        verify(configuration, times(1)).updateSettings(mockStatusResponse);
        verify(configuration, times(1)).isCapture();
        verifyNoMoreInteractions(configuration);
        verifyZeroInteractions(mockSessionOne, mockSessionTwo);
    }

    @Test
    public void handleStatusResponseWhenCapturingIsDisabled() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);
        SessionImpl mockSessionThree = mock(SessionImpl.class);
        SessionImpl mockSessionFour = mock(SessionImpl.class);

        target.startSession(mockSessionOne);
        target.finishSession(mockSessionOne);
        target.startSession(mockSessionTwo);
        target.startSession(mockSessionThree);
        target.startSession(mockSessionFour);
        target.finishSession(mockSessionFour);

        StatusResponse mockStatusResponse = mock(StatusResponse.class);

        when(configuration.isCapture()).thenReturn(false);

        // when
        target.handleStatusResponse(mockStatusResponse);

        // then
        assertThat(target.getAllOpenSessions(), is(equalTo(new SessionImpl[] { mockSessionTwo, mockSessionThree })));
        assertThat(target.getAllFinishedSessions(), is(emptyArray()));

        verify(configuration, times(1)).updateSettings(mockStatusResponse);
        verify(configuration, times(1)).isCapture();
        verifyNoMoreInteractions(configuration);

        verify(mockSessionOne, times(1)).clearCapturedData();
        verify(mockSessionTwo, times(1)).clearCapturedData();
        verify(mockSessionThree, times(1)).clearCapturedData();
        verify(mockSessionFour, times(1)).clearCapturedData();
        verifyNoMoreInteractions(mockSessionOne, mockSessionTwo, mockSessionThree, mockSessionFour);
    }

    @Test
    public void isTimeSyncedReturnsTrueIfSyncWasNeverPerformed() {
        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);
        when(timingProvider.isTimeSyncSupported()).thenReturn(true);

        assertThat(target.isTimeSynced(), is(false));
    }

    @Test
    public void isTimeSyncedReturnsTrueIfSyncIsNotSupported() {
        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when
        target.disableTimeSyncSupport();

        // then
        assertThat(target.isTimeSynced(), is(true));
    }

    @Test
    public void timingProviderIsCalledOnTimeSyncInit() {
        // given
        BeaconSendingContext target = new BeaconSendingContext(configuration, httpClientProvider, timingProvider);

        // when
        target.initializeTimeSync(1234L, true);

        // then
        verify(timingProvider, times(1)).initialize(1234L, true);
    }
}
