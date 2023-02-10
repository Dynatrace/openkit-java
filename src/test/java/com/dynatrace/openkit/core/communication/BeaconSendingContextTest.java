/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.core.objects.SessionState;
import com.dynatrace.openkit.protocol.AdditionalQueryParameters;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.ResponseAttributes;
import com.dynatrace.openkit.protocol.ResponseAttributesDefaults;
import com.dynatrace.openkit.protocol.ResponseAttributesImpl;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class BeaconSendingContextTest {

    private Logger logger;
    private HTTPClientConfiguration httpClientConfig;
    private HTTPClientProvider httpClientProvider;
    private TimingProvider timingProvider;
    private AbstractBeaconSendingState mockState;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);

        httpClientConfig = mock(HTTPClientConfiguration.class);

        final HTTPClient httpClient = mock(HTTPClient.class);
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withKeyValueDefaults().build();
        final StatusResponse statusResponse = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                200,
                Collections.<String, List<String>>emptyMap()
        );
        when(httpClient.sendBeaconRequest(isA(String.class), any(byte[].class), any(AdditionalQueryParameters.class), anyInt()))
                .thenReturn(statusResponse);

        httpClientProvider = mock(HTTPClientProvider.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

        timingProvider = mock(TimingProvider.class);
        mockState = mock(AbstractBeaconSendingState.class);
    }

    @Test
    public void currentStateIsInitializedAccordingly() {
        // given, when
        BeaconSendingContext target = createBeaconSendingContext().build();

        // then
        assertThat(target.getCurrentState(), notNullValue());
        assertThat(target.getCurrentState(), instanceOf(BeaconSendingInitState.class));
    }

    @Test
    public void setCurrentStateChangesState() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        target.setNextState(mockState);

        // then
        assertThat(target.getCurrentState(), notNullValue());
        assertThat(target.getNextState(), is(sameInstance(mockState)));
    }

    @Test
    public void executeCurrentStateCallsExecuteOnCurrentState() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().with(mockState).build();
        verifyNoInteractions(mockState);

        // when
        target.executeCurrentState();

        // then
        verify(mockState, times(1)).execute(target);
    }

    @Test
    public void initCompleteSuccessAndWait() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        target.initCompleted(true);
        boolean obtained = target.waitForInit();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void requestShutdown() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        assertThat(target.isShutdownRequested(), is(false));

        // when
        target.requestShutdown();

        // then
        assertThat(target.isShutdownRequested(), is(true));
    }

    @Test
    public void initCompleteFailureAndWait() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        target.initCompleted(false);
        boolean obtained = target.waitForInit();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void waitForInitCompleteTimeout() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void waitForInitCompleteWhenInitCompletedSuccessfully() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        target.initCompleted(true);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void waitForInitCompleteWhenInitCompletedNotSuccessfully() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        target.initCompleted(false);

        // when init complete was never set and timeout will be reached
        boolean obtained = target.waitForInit(1);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void aDefaultConstructedContextIsNotInitialized() {
        // given, when
        BeaconSendingContext target = createBeaconSendingContext().build();

        // then
        assertThat(target.isInitialized(), is(false));
    }

    @Test
    public void successfullyInitializedContextIsInitialized() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when initialized
        target.initCompleted(true);

        // then
        assertThat(target.isInitialized(), is(true));
    }

    @Test
    public void isInTerminalStateChecksCurrentState() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
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
    public void isCaptureOnIsTakenFromDefaultServerConfig() {
        // given, when
        BeaconSendingContext target = createBeaconSendingContext().build();

        // then
        assertThat(target.isCaptureOn(), is(ServerConfiguration.DEFAULT.isCaptureEnabled()));
    }

    @Test
    public void setAndGetLastOpenSessionBeaconSendTime() {

        BeaconSendingContext target = createBeaconSendingContext().build();

        target.setLastOpenSessionBeaconSendTime(1234L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(1234L));

        target.setLastOpenSessionBeaconSendTime(5678L);
        assertThat(target.getLastOpenSessionBeaconSendTime(), is(5678L));
    }

    @Test
    public void setAndGetLastStatusCheckTime() {

        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

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
    public void getSendIntervalIsTakenFromDefaultResponseAttributeDefaults() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        int obtained = target.getSendInterval();

        // then
        assertThat(obtained, is(equalTo(ResponseAttributesDefaults.UNDEFINED.getSendIntervalInMilliseconds())));
    }

    @Test
    public void getHttpClientProvider() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        HTTPClientProvider obtained = target.getHTTPClientProvider();

        // then
        assertThat(obtained, is(sameInstance(httpClientProvider)));
    }

    @Test
    public void getHttpClient() {
        // given
        HTTPClient mockClient = mock(HTTPClient.class);
        when(httpClientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(mockClient);

        BeaconSendingContext target = createBeaconSendingContext().build();
        verifyNoInteractions(httpClientConfig, httpClientProvider);

        // when
        HTTPClient obtained = target.getHTTPClient();

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained, is(sameInstance(mockClient)));

        verify(httpClientProvider, times(1)).createClient(httpClientConfig);
        verifyNoMoreInteractions(httpClientProvider);
        verifyNoInteractions(mockClient, httpClientConfig);
    }

    @Test
    public void getCurrentTimestamp() {
        // given
        long timeStamp = 1234567890L;
        when(timingProvider.provideTimestampInMilliseconds()).thenReturn(timeStamp);

        BeaconSendingContext target = createBeaconSendingContext().build();
        verifyNoInteractions(timingProvider);

        // when
        long obtained = target.getCurrentTimestamp();

        // then
        assertThat(obtained, is(timeStamp));
        verify(timingProvider, times(1)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepDefaultTime() throws InterruptedException {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        verifyNoInteractions(timingProvider);

        // when
        target.sleep();

        // then
        verify(timingProvider, times(1)).sleep(BeaconSendingContext.DEFAULT_SLEEP_TIME_MILLISECONDS);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void sleepWithGivenTime() throws InterruptedException {
        // given
        long sleepTime = 1234L;
        BeaconSendingContext target = createBeaconSendingContext().build();
        verifyNoInteractions(timingProvider);

        // when
        target.sleep(sleepTime);

        // then
        verify(timingProvider, times(1)).sleep(sleepTime);
        verifyNoMoreInteractions(timingProvider);
    }

    @Test
    public void aDefaultConstructedContextDoesNotStoreAnySessions() {
        // given, when
        BeaconSendingContext target = createBeaconSendingContext().build();

        // then
        assertThat(target.getAllNotConfiguredSessions(), is(empty()));
        assertThat(target.getAllOpenAndConfiguredSessions(), is(empty()));
        assertThat(target.getAllFinishedAndConfiguredSessions(), is(empty()));
    }

    @Test
    public void addSession() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        // when starting first session
        target.addSession(mockSessionOne);

        // then
        assertThat(target.getSessionCount(), is(equalTo(1)));

        // when starting second sessions
        target.addSession(mockSessionTwo);

        // then
        assertThat(target.getSessionCount(), is(equalTo(2)));
    }

    @Test
    public void removeSession() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        SessionImpl mockSessionOne = mock(SessionImpl.class);
        SessionImpl mockSessionTwo = mock(SessionImpl.class);

        target.addSession(mockSessionOne);
        target.addSession(mockSessionTwo);
        assertThat(target.getSessionCount(), is(equalTo(2)));

        // when
        target.removeSession(mockSessionOne);

        // then
        assertThat(target.getSessionCount(), is(equalTo(1)));

        // and when
        target.removeSession(mockSessionTwo);

        // then
        assertThat(target.getSessionCount(), is(equalTo(0)));
    }

    @Test
    public void disableCaptureAndClearModifiesCaptureFlag() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        assertThat(target.isCaptureOn(), is(true));

        // when
        target.disableCaptureAndClear();

        // then
        assertThat(target.isCaptureOn(), is(false));
    }

    @Test
    public void disableCaptureAndClearClearsCapturedSessionData() {
        // given
        SessionState sessionState = mock(SessionState.class);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(sessionState);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.disableCaptureAndClear();

        // then
        assertThat(target.getSessionCount(), is(1));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void disableCaptureAndClearRemovesFinishedSession() {
        // given
        SessionState sessionState = mock(SessionState.class);
        when(sessionState.isFinished()).thenReturn(true);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(sessionState);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.disableCaptureAndClear();

        // then
        assertThat(target.getSessionCount(), is(0));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseDisablesCaptureIfResponseIsNull() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        assertThat(target.isCaptureOn(), is(true));

        // when
        target.handleStatusResponse(null);

        // then
        assertThat(target.isCaptureOn(), is(false));
    }

    @Test
    public void handleStatusResponseClearsSessionDataIfResponseIsNull() {
        // given
        SessionState state = mock(SessionState.class);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.handleStatusResponse(null);

        // then
        assertThat(target.getSessionCount(), is(1));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseRemovesFinishedSessionsIfResponseIsNull() {
        // given
        SessionState state = mock(SessionState.class);
        when(state.isFinished()).thenReturn(true);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.handleStatusResponse(null);

        // then
        assertThat(target.getSessionCount(), is(0));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseDisablesCaptureIfResponseCodeIsNotOk() {
        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.isErroneousResponse()).thenReturn(true);

        BeaconSendingContext target = createBeaconSendingContext().build();
        assertThat(target.isCaptureOn(), is(true));

        // when
        target.handleStatusResponse(response);

        // then
        assertThat(target.isCaptureOn(), is(false));
    }

    @Test
    public void handleStatusResponseClearsSessionDataIfResponseCodeIsNotOk() {
        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.isErroneousResponse()).thenReturn(true);

        SessionState state = mock(SessionState.class);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.handleStatusResponse(response);

        // then
        assertThat(target.getSessionCount(), is(1));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseRemovesFinishedSessionsIfResponseCodeIsNotOk() {
        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.isErroneousResponse()).thenReturn(true);

        SessionState state = mock(SessionState.class);
        when(state.isFinished()).thenReturn(true);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.handleStatusResponse(response);

        // then
        assertThat(target.getSessionCount(), is(0));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseClearsSessionDataIfResponseIsCaptureOff() {
        // given
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults().withCapture(false).build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        SessionState state = mock(SessionState.class);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.handleStatusResponse(response);

        // then
        assertThat(target.getSessionCount(), is(1));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseRemovesFinishedSessionsIfResponseIsCaptureOff() {
        // given
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults().withCapture(false).build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        SessionState state = mock(SessionState.class);
        when(state.isFinished()).thenReturn(true);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);

        // when
        target.handleStatusResponse(response);

        // then
        assertThat(target.getSessionCount(), is(0));
        verify(session, times(1)).clearCapturedData();
    }

    @Test
    public void handleStatusResponseUpdatesSendInterval() {
        // given
        int sendInterval = 999;
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval)
                .build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        SessionImpl session = mock(SessionImpl.class);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);
        assertThat(target.getSendInterval(), is(not(sendInterval)));

        // when
        target.handleStatusResponse(response);
        int obtained = target.getSendInterval();

        // then
        verifyNoInteractions(session);
        assertThat(obtained, is(sendInterval));
    }

    @Test
    public void handleStatusResponseUpdatesCaptureStateToFalse() {
        // given
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults().withCapture(false).build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        SessionState state = mock(SessionState.class);
        SessionImpl session = mock(SessionImpl.class);
        when(session.getState()).thenReturn(state);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(session);
        assertThat(target.isCaptureOn(), is(true));

        // when
        target.handleStatusResponse(response);

        // then
        verify(session, times(1)).clearCapturedData();
        assertThat(target.isCaptureOn(), is(false));
    }

    @Test
    public void handleStatusResponseUpdatesCaptureStateToTrue() {
        // given
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults().withCapture(true).build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        SessionImpl session = mock(SessionImpl.class);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.disableCaptureAndClear();
        target.addSession(session);
        assertThat(target.isCaptureOn(), is(false));

        // when
        target.handleStatusResponse(response);

        // then
        verifyNoInteractions(session);
        assertThat(target.isCaptureOn(), is(true));
    }

    @Test
    public void handleStatusResponseUpdatesHttpClientConfig() {
        // given
        when(httpClientConfig.getBaseURL()).thenReturn("https://localhost:9999/1");
        when(httpClientConfig.getApplicationID()).thenReturn("some cryptic appId");
        when(httpClientConfig.getServerID()).thenReturn(42);
        when(httpClientConfig.getSSLTrustManager()).thenReturn(mock(SSLTrustManager.class));

        int serverId = 73;
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults().withServerId(serverId).build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        BeaconSendingContext target = spy(createBeaconSendingContext().build());
        verifyNoInteractions(httpClientConfig);

        // when
        target.handleStatusResponse(response);

        // then
        verify(target, times(1)).createHttpClientConfigurationWith(serverId);
        verify(httpClientConfig, times(2)).getServerID();
        verify(httpClientConfig, times(1)).getBaseURL();
        verify(httpClientConfig, times(1)).getApplicationID();
        verify(httpClientConfig, times(1)).getSSLTrustManager();

        // and when
        ArgumentCaptor<HTTPClientConfiguration> captor = ArgumentCaptor.forClass(HTTPClientConfiguration.class);
        target.getHTTPClient();

        // then
        verify(target, times(1)).getHTTPClient(captor.capture());
        HTTPClientConfiguration obtained = captor.getValue();

        assertThat(obtained, is(not(sameInstance(httpClientConfig))));
        assertThat(obtained.getServerID(), is(serverId));
        assertThat(obtained.getBaseURL(), is(httpClientConfig.getBaseURL()));
        assertThat(obtained.getApplicationID(), is(httpClientConfig.getApplicationID()));
        assertThat(obtained.getSSLTrustManager(), is(httpClientConfig.getSSLTrustManager()));
    }

    @Test
    public void handleStatusResponseMergesLastStatusResponse() {
        // given
        int beaconSize = 1234;
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withJsonDefaults().withMaxBeaconSizeInBytes(beaconSize).build();
        StatusResponse response = StatusResponse.createSuccessResponse(
                logger,
                responseAttributes,
                StatusResponse.HTTP_OK,
                Collections.<String, List<String>>emptyMap()
        );

        BeaconSendingContext target = createBeaconSendingContext().build();
        ResponseAttributes initialAttributes = target.getLastResponseAttributes();

        // when
        target.handleStatusResponse(response);
        ResponseAttributes obtained = target.getLastResponseAttributes();

        // then
        assertThat(obtained, notNullValue());
        assertThat(initialAttributes, not(equalTo(obtained)));
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void createHttpClientConfigUpdatesOnlyServerId() {
        // given
        int serverId = 73;
        String baseUrl = "https://localhost:9999/1";
        String applicationId = "some cryptic appId";
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        when(httpClientConfig.getServerID()).thenReturn(37);
        when(httpClientConfig.getBaseURL()).thenReturn(baseUrl);
        when(httpClientConfig.getApplicationID()).thenReturn(applicationId);
        when(httpClientConfig.getSSLTrustManager()).thenReturn(trustManager);

        BeaconSendingContext target = createBeaconSendingContext().build();
        assertThat(httpClientConfig.getServerID(), is(not(serverId)));

        // when
        HTTPClientConfiguration obtained = target.createHttpClientConfigurationWith(serverId);

        // then
        assertThat(obtained.getServerID(), is(serverId));
        assertThat(obtained.getBaseURL(), is(baseUrl));
        assertThat(obtained.getApplicationID(), is(applicationId));
        assertThat(obtained.getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void getAllNotConfiguredSessionsReturnsOnlyNotConfiguredSessions() {
        // given
        SessionState relevantSessionState = mock(SessionState.class);
        when(relevantSessionState.isConfigured()).thenReturn(false);
        SessionImpl relevantSession = mock(SessionImpl.class);
        when(relevantSession.getState()).thenReturn(relevantSessionState);

        SessionState ignoredSessionState = mock(SessionState.class);
        when(ignoredSessionState.isConfigured()).thenReturn(true);
        SessionImpl ignoredSession = mock(SessionImpl.class);
        when(ignoredSession.getState()).thenReturn(ignoredSessionState);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(relevantSession);
        target.addSession(ignoredSession);

        assertThat(target.getSessionCount(), is(2));

        // when
        List<SessionImpl> obtained = target.getAllNotConfiguredSessions();

        // then
        assertThat(obtained, containsInAnyOrder(relevantSession));
    }

    @Test
    public void getAllOpenAndConfiguredSessionsReturnsOnlyConfiguredNotFinishedSessions() {
        // given
        SessionState relevantState = mock(SessionState.class);
        when(relevantState.isConfiguredAndOpen()).thenReturn(true);
        SessionImpl relevantSession = mock(SessionImpl.class);
        when(relevantSession.getState()).thenReturn(relevantState);

        SessionState ignoredState = mock(SessionState.class);
        when(ignoredState.isConfiguredAndOpen()).thenReturn(false);
        SessionImpl ignoredSession = mock(SessionImpl.class);
        when(ignoredSession.getState()).thenReturn(ignoredState);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(relevantSession);
        target.addSession(ignoredSession);

        assertThat(target.getSessionCount(), is(2));

        // when
        List<SessionImpl> obtained = target.getAllOpenAndConfiguredSessions();

        // then
        assertThat(obtained, containsInAnyOrder(relevantSession));
    }

    @Test
    public void getAllFinishedAndConfiguredSessionsReturnsOnlyConfiguredAndFinishedSessions() {
        // given
        SessionState relevantState = mock(SessionState.class);
        when(relevantState.isConfiguredAndFinished()).thenReturn(true);
        SessionImpl relevantSession = mock(SessionImpl.class);
        when(relevantSession.getState()).thenReturn(relevantState);

        SessionState ignoredState = mock(SessionState.class);
        when(ignoredState.isConfiguredAndFinished()).thenReturn(false);
        SessionImpl ignoredSession = mock(SessionImpl.class);
        when(ignoredSession.getState()).thenReturn(ignoredState);

        BeaconSendingContext target = createBeaconSendingContext().build();
        target.addSession(relevantSession);
        target.addSession(ignoredSession);

        assertThat(target.getSessionCount(), is(2));

        // when
        List<SessionImpl> obtained = target.getAllFinishedAndConfiguredSessions();

        // then
        assertThat(obtained, containsInAnyOrder(relevantSession));
    }

    @Test
    public void getCurrentServerIdReturnsServerIdOfHttpClientConfig() {
        // given
        int serverId = 37;
        when(httpClientConfig.getServerID()).thenReturn(serverId);

        BeaconSendingContext context = createBeaconSendingContext().build();

        // when
        int obtained = context.getCurrentServerId();

        // then
        assertThat(obtained, is(serverId));
    }

    @Test
    public void updateFromDoesNothingIfStatusResponseIsNull() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();
        ResponseAttributes initialAttributes = target.getLastResponseAttributes();

        // when
        ResponseAttributes obtained = target.updateFrom(null);

        // then
        assertThat(obtained, is(equalTo(initialAttributes)));
    }

    @Test
    public void updateFromDoesNothingIfStatusResponseIsNotSuccessful() {
        // given
        StatusResponse response = mock(StatusResponse.class);
        when(response.isErroneousResponse()).thenReturn(true);
        BeaconSendingContext target = createBeaconSendingContext().build();
        ResponseAttributes initialAttributes = target.getLastResponseAttributes();

        // when
        ResponseAttributes obtained = target.updateFrom(response);

        // then
        assertThat(obtained, is(equalTo(initialAttributes)));
        verify(response, times(1)).isErroneousResponse();
        verifyNoMoreInteractions(response);
    }

    @Test
    public void updateFromMergesResponseAttributesFromStatusResponse() {
        // given
        int serverId = 9999;
        ResponseAttributes attributes = ResponseAttributesImpl.withUndefinedDefaults()
                .withServerId(serverId).build();
        StatusResponse response = mock(StatusResponse.class);
        when(response.getResponseAttributes()).thenReturn(attributes);
        when(response.isErroneousResponse()).thenReturn(false);

        BeaconSendingContext target = createBeaconSendingContext().build();
        ResponseAttributes initialAttributes  = target.getLastResponseAttributes();

        // when
        ResponseAttributes obtained = target.updateFrom(response);

        // then
        assertThat(obtained, is(equalTo(target.getLastResponseAttributes())));
        assertThat(obtained, is(not(equalTo(initialAttributes))));
        assertThat(obtained, is(not(equalTo(attributes))));
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void updateFromDisablesCapturingIfReceivedApplicationIdMismatches() {
        // given
        when(httpClientConfig.getApplicationID()).thenReturn("some application id");
        ResponseAttributes attributes = ResponseAttributesImpl.withUndefinedDefaults()
                                                              .withApplicationId("different application id").build();
        StatusResponse response = mock(StatusResponse.class);
        when(response.getResponseAttributes()).thenReturn(attributes);
        when(response.isErroneousResponse()).thenReturn(false);

        BeaconSendingContext target = createBeaconSendingContext().build();
        boolean initialCaptureOn = target.isCaptureOn();

        // when
        target.updateFrom(response);

        // then
        assertThat(initialCaptureOn, is(true));
        assertThat(target.isCaptureOn(), is(false));
    }

    @Test
    public void getConfigurationTimestampReturnsZeroOnDefault() {
        // given
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        long obtained = target.getConfigurationTimestamp();

        // then
        assertThat(obtained, is(0L));
    }

    @Test
    public void getConfigurationTimestampReturnsValueFromResponseAttributes() {
        // given
        long timestamp = 1234;
        ResponseAttributes attributes = ResponseAttributesImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();
        StatusResponse response = StatusResponse.createSuccessResponse(logger, attributes, 200, Collections.<String, List<String>>emptyMap()
        );
        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        target.updateFrom(response);

        // then
        assertThat(target.getConfigurationTimestamp(), is(timestamp));
    }

    @Test
    public void applicationIdMatchesIfApplicationIdWasNotReceived() {
        // given
        when(httpClientConfig.getApplicationID()).thenReturn("application id");
        ResponseAttributes attributes = ResponseAttributesImpl.withUndefinedDefaults().build();

        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        boolean obtained = target.isApplicationIdMismatch(attributes);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void applicationIdMatchesIfStoredAndReceivedApplicationIdsAreEqual() {
        // given
        String applicationId = "application id";
        when(httpClientConfig.getApplicationID()).thenReturn(applicationId);
        ResponseAttributes attributes = ResponseAttributesImpl.withUndefinedDefaults()
                                                              .withApplicationId(applicationId).build();

        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        boolean obtained = target.isApplicationIdMismatch(attributes);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void applicationIdMismatchesIfStoredAndReceivedApplicationIdsAreNotEqual() {
        // given
        when(httpClientConfig.getApplicationID()).thenReturn("application id");
        ResponseAttributes attributes = ResponseAttributesImpl.withUndefinedDefaults()
                                                              .withApplicationId("application ID").build();

        BeaconSendingContext target = createBeaconSendingContext().build();

        // when
        boolean obtained = target.isApplicationIdMismatch(attributes);

        // then
        assertThat(obtained, is(true));
    }

    private TestBeaconSendingContextBuilder createBeaconSendingContext() {
        TestBeaconSendingContextBuilder builder = new TestBeaconSendingContextBuilder();
        builder.logger = logger;
        builder.httpClientConfig = httpClientConfig;
        builder.httpClientProvider = httpClientProvider;
        builder.timingProvider = timingProvider;
        builder.initState = new BeaconSendingInitState();

        return builder;
    }

    private static class TestBeaconSendingContextBuilder {
        private Logger logger;
        private HTTPClientConfiguration httpClientConfig;
        private HTTPClientProvider httpClientProvider;
        private TimingProvider timingProvider;
        private AbstractBeaconSendingState initState;

        private TestBeaconSendingContextBuilder with(AbstractBeaconSendingState initState) {
            this.initState = initState;
            return this;
        }

        private BeaconSendingContext build() {
            return new BeaconSendingContext(
                    logger,
                    httpClientConfig,
                    httpClientProvider,
                    timingProvider,
                    initState
            );
        }
    }
}
