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

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class BeaconSendingContextTest {

    private Logger logger;
    private Configuration configuration;
    private HTTPClientProvider httpClientProvider;
    private TimingProvider timingProvider;
    private AbstractBeaconSendingState mockState;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
        configuration = mock(Configuration.class);
        final HTTPClient httpClient = mock(HTTPClient.class);
        final StatusResponse statusResponse = new StatusResponse(logger, "", 200, Collections.<String, List<String>>emptyMap());
        when(httpClient.sendBeaconRequest(isA(String.class), any(byte[].class))).thenReturn(statusResponse);
        httpClientProvider = mock(HTTPClientProvider.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);
        timingProvider = mock(TimingProvider.class);
        mockState = mock(AbstractBeaconSendingState.class);
    }

    @Test
    public void currentStateIsInitializedAccordingly() {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        assertThat(target.getCurrentState(), notNullValue());
        assertThat(target.getCurrentState(), instanceOf(BeaconSendingInitState.class));
    }

    @Test
    public void setCurrentStateChangesState() {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        target.setNextState(mockState);

        assertThat(target.getCurrentState(), notNullValue());
        assertThat(target.getNextState(), is(sameInstance(mockState)));
    }

    @Test
    public void executeCurrentStateCallsExecuteOnCurrentState() {
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider, mockState);

        verifyZeroInteractions(mockState);

        target.executeCurrentState();

        verify(mockState, times(1)).execute(target);
    }

    @Test
    public void initCompleteSuccessAndWait() {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        target.initCompleted(true);
        boolean obtained = target.waitForInit();

        assertThat(obtained, is(true));
    }

    @Test
    public void requestShutdown() {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        assertThat(target.isShutdownRequested(), is(false));

        target.requestShutdown();

        assertThat(target.isShutdownRequested(), is(true));
    }

    @Test
    public void initCompleteFailureAndWait() {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        target.initCompleted(false);
        boolean obtained = target.waitForInit();

        assertThat(obtained, is(false));
    }

    @Test
    public void waitForInitCompleteTimeout() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void waitForInitCompleteWhenInitCompletedSuccessfully() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);
        target.initCompleted(true);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void waitForInitCompleteWhenInitCompletedNotSuccessfully() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);
        target.initCompleted(false);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void aDefaultConstructedContextIsNotInitialized() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        // then
        assertThat(target.isInitialized(), is(false));
    }

    @Test
    public void successfullyInitializedContextIsInitialized() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        // when initialized
        target.initCompleted(true);

        // then
        assertThat(target.isInitialized(), is(true));
    }

    @Test
    public void isInTerminalStateChecksCurrentState() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);
        AbstractBeaconSendingState terminalState = mock(AbstractBeaconSendingState.class);
        when(terminalState.isTerminalState()).thenReturn(true);

        // then (context starts with InitState)
        assertThat(target.isInTerminalState(), is(false));
        assertThat(target.getCurrentState(), instanceOf(BeaconSendingInitState.class));

        // when terminal state is next state
        target.setNextState(terminalState);

        // then (current state is not changed)
        assertThat(target.isInTerminalState(), is(false));
        assertThat(target.getCurrentState(), instanceOf(BeaconSendingInitState.class));
        verifyNoMoreInteractions(terminalState);
    }

    @Test
    public void isCaptureOnReturnsValueFromConfiguration() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

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
    public void setAndGetLastOpenSessionBeaconSendTime() {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        target.setLastOpenSessionBeaconSendTime(1234L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(1234L));

        target.setLastOpenSessionBeaconSendTime(5678L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(5678L));
    }

    @Test
    public void setAndGetLastStatusCheckTime() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

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
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);
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
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

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

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

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

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        verifyZeroInteractions(timingProvider);

        long obtained = target.getCurrentTimestamp();

        assertThat(obtained, is(1234567890L));
        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepDefaultTime() throws InterruptedException {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        verifyZeroInteractions(timingProvider);

        target.sleep();

        verify(timingProvider, times(1)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepWithGivenTime() throws InterruptedException {

        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        verifyZeroInteractions(timingProvider);

        target.sleep(1234L);

        verify(timingProvider, times(1)).sleep(1234L);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void aDefaultConstructedContextDoesNotStoreAnySessions() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);

        // then
        assertThat(target.getAllNewSessions(), is(empty()));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));
    }

    @Test
    public void whenStartingASessionTheSessionIsConsideredAsNew() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
                timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        // when starting first session
        target.startSession(mockSessionOne);

        // then
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionOne));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));

        // when starting second sessions
        target.startSession(mockSessionTwo);

        // then
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionOne, mockSessionTwo));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));
    }

    @Test
    public void disableCapture() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
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
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionTwo, mockSessionThree));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));

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
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
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
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionOne, mockSessionTwo));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));

        verify(configuration, times(1)).updateSettings(mockStatusResponse);
        verify(configuration, times(1)).isCapture();
        verifyNoMoreInteractions(configuration);
        verifyZeroInteractions(mockSessionOne, mockSessionTwo);
    }

    @Test
    public void handleStatusResponseWhenCapturingIsDisabled() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
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
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionTwo, mockSessionThree));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));

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
    public void finishingANewSessionStillLeavesItNew() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
        SessionImpl mockSession = mock(SessionImpl.class);

        // when starting the session
        target.startSession(mockSession);

        // then it's in the list of new ones
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSession));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));

        // and when finishing the session
        target.finishSession(mockSession);

        // then it's in the list of new ones
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSession));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));
    }

    @Test
    public void finishingASessionThatHasNotBeenStartedBeforeIsNotAddedToInternalList() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
        SessionImpl mockSession = mock(SessionImpl.class);

        // when the session is not started, but immediately finished
        target.finishSession(mockSession);

        // then
        assertThat(target.getAllNewSessions(), is(empty()));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));
    }

    @Test
    public void afterASessionHasBeenConfiguredItsOpenAndConfigured() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        // when both session are added
        target.startSession(mockSessionOne);
        target.startSession(mockSessionTwo);

        // and configuring the first one
        target.getAllNewSessions().get(0).updateBeaconConfiguration(mock(BeaconConfiguration.class));

        // then
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionTwo));
        assertThat(getAllOpenAndConfiguredSessions(target), containsInAnyOrder(mockSessionOne));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));

        // and when configuring the second open session
        target.getAllNewSessions().get(0).updateBeaconConfiguration(mock(BeaconConfiguration.class));

        // then
        assertThat(target.getAllNewSessions(), is(empty()));
        assertThat(getAllOpenAndConfiguredSessions(target), containsInAnyOrder(mockSessionOne, mockSessionTwo));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));
    }

    @Test
    public void afterAFinishedSessionHasBeenConfiguredItsFinishedAndConfigured() {

        // given
        BeaconSendingContext target = new BeaconSendingContext(logger, configuration, httpClientProvider,
            timingProvider);
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        // when both session are started and finished
        target.startSession(mockSessionOne);
        target.startSession(mockSessionTwo);
        target.finishSession(mockSessionOne);
        target.finishSession(mockSessionTwo);

        // and configuring the first one
        target.getAllNewSessions().get(0).updateBeaconConfiguration(mock(BeaconConfiguration.class));

        // then
        assertThat(getAllNewSessions(target), containsInAnyOrder(mockSessionTwo));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(getAllFinishedAndConfiguredSessions(target), containsInAnyOrder(mockSessionOne));

        // and when configuring the second open session
        target.getAllNewSessions().get(0).updateBeaconConfiguration(mock(BeaconConfiguration.class));

        // then
        assertThat(target.getAllNewSessions(), is(empty()));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(getAllFinishedAndConfiguredSessions(target), containsInAnyOrder(mockSessionOne, mockSessionTwo));
    }


    private static List<SessionImpl> getAllNewSessions(BeaconSendingContext target) {
        return extractSessionImpl(target.getAllNewSessions());
    }

    private static List<SessionImpl> getAllOpenAndConfiguredSessions(BeaconSendingContext target) {
        return extractSessionImpl(target.getAllOpenAndConfiguredSessions());
    }

    private static List<SessionImpl> getAllFinishedAndConfiguredSessions(BeaconSendingContext target) {
        return extractSessionImpl(target.getAllFinishedAndConfiguredSessions());
    }

    private static List<SessionImpl> extractSessionImpl(List<SessionWrapper> obtained) {
        List<SessionImpl> result = new ArrayList<SessionImpl>(obtained.size());
        for (SessionWrapper wrapper : obtained) {
            result.add(wrapper.getSession());
        }

        return result;
    }
}
