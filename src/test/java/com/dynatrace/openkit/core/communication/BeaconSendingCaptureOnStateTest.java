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
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.protocol.AdditionalQueryParameters;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.ResponseAttributes;
import com.dynatrace.openkit.protocol.ResponseAttributesImpl;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


public class BeaconSendingCaptureOnStateTest {

    private BeaconSendingContext mockContext;
    private SessionImpl mockSession1Open;
    private SessionImpl mockSession2Open;
    private SessionImpl mockSession3Finished;
    private SessionImpl mockSession4Finished;
    private SessionImpl mockSession5New;
    private SessionImpl mockSession6New;

    @Before
    public void setUp() {
        mockSession1Open = mock(SessionImpl.class);
        mockSession2Open = mock(SessionImpl.class);
        mockSession3Finished = mock(SessionImpl.class);
        mockSession4Finished = mock(SessionImpl.class);
        mockSession5New = mock(SessionImpl.class);
        mockSession6New = mock(SessionImpl.class);

        StatusResponse successResponse = StatusResponse.createSuccessResponse(
                mock(Logger.class),
                ResponseAttributesImpl.withJsonDefaults().build(),
                200,
                Collections.<String, List<String>>emptyMap()
        );

        when(mockSession1Open.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(successResponse);
        when(mockSession2Open.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(StatusResponse.createErrorResponse(mock(Logger.class), 404));
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(true);

        HTTPClientProvider mockHTTPClientProvider = mock(HTTPClientProvider.class);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.getCurrentTimestamp()).thenReturn(42L);
        when(mockContext.getAllNotConfiguredSessions()).thenReturn(Collections.<SessionImpl>emptyList());
        when(mockContext.getAllOpenAndConfiguredSessions()).thenReturn(Arrays.asList(mockSession1Open, mockSession2Open));
        when(mockContext.getAllFinishedAndConfiguredSessions()).thenReturn(Arrays.asList(mockSession3Finished, mockSession4Finished));
        when(mockContext.getHTTPClientProvider()).thenReturn(mockHTTPClientProvider);
    }

    @Test
    public void aBeaconSendingCaptureOnStateIsNotATerminalState() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        // verify that BeaconSendingCaptureOnState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingCaptureOnStateHasTerminalStateBeaconSendingFlushSessions() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void toStringReturnsStateName() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        // then
        assertThat(target.toString(), is(equalTo("CaptureOn")));
    }

    @Test
    public void newSessionRequestsAreMadeForNotConfiguredNewSessions() {
        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse successResponse = StatusResponse.createSuccessResponse(
                mock(Logger.class),
                ResponseAttributesImpl.withJsonDefaults().withMultiplicity(5).build(),
                200,
                Collections.<String, List<String>>emptyMap()
        );

        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getAllNotConfiguredSessions()).thenReturn(Arrays.asList(mockSession5New, mockSession6New));
        when(mockContext.updateFrom(any(StatusResponse.class))).thenReturn(successResponse.getResponseAttributes());
        when(mockClient.sendNewSessionRequest(any(AdditionalQueryParameters.class)))
                .thenReturn(successResponse) // first response valid
                .thenReturn(StatusResponse.createErrorResponse(mock(Logger.class), StatusResponse.HTTP_BAD_REQUEST)); // second response invalid
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(true);
        when(mockSession6New.canSendNewSessionRequest()).thenReturn(true);

        ArgumentCaptor<ServerConfiguration> serverConfigCaptor = ArgumentCaptor.forClass(ServerConfiguration.class);

        // when
        target.execute(mockContext);

        // verify for both new sessions a new session request has been made
        verify(mockClient, times(2)).sendNewSessionRequest(mockContext);

        // verify first has been updated, second decreased
        verify(mockSession5New, times(1)).updateServerConfiguration(serverConfigCaptor.capture());
        assertThat(serverConfigCaptor.getAllValues().get(0).getMultiplicity(), is(equalTo(5)));

        // verify for beacon 6 only the number of tries was decreased
        verify(mockSession6New, times(1)).decreaseNumRemainingSessionRequests();
    }

    @Test
    public void successfulNewSessionRequestUpdateLastResponseAttributes() {
        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        int beaconSize = 73;
        ResponseAttributes responseAttributes = mock(ResponseAttributes.class);
        when(responseAttributes.getMaxBeaconSizeInBytes()).thenReturn(beaconSize);
        StatusResponse sessionRequestResponse = mock(StatusResponse.class);
        when(sessionRequestResponse.getResponseAttributes()).thenReturn(responseAttributes);

        when(mockContext.updateFrom(any(StatusResponse.class))).thenReturn(responseAttributes);

        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockClient.sendNewSessionRequest(any(AdditionalQueryParameters.class))).thenReturn(sessionRequestResponse);
        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getAllNotConfiguredSessions()).thenReturn(Collections.singletonList(mockSession5New));
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(true);
        ArgumentCaptor<ServerConfiguration> serverConfigCaptor = ArgumentCaptor.forClass(ServerConfiguration.class);

        // when
        target.execute(mockContext);

        // then
        verify(mockContext, times(1)).updateFrom(sessionRequestResponse);
        verify(mockSession5New, times(1)).updateServerConfiguration(serverConfigCaptor.capture());

        ServerConfiguration serverConfig = serverConfigCaptor.getValue();
        assertThat(serverConfig.getBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void unsuccessfulNewSessionRequestDoesNotMergeStatusResponse() {
        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse sessionRequestResponse = mock(StatusResponse.class);
        when(sessionRequestResponse.isErroneousResponse()).thenReturn(true);
        ResponseAttributes contextAttributes = mock(ResponseAttributes.class);

        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockClient.sendNewSessionRequest(any(AdditionalQueryParameters.class))).thenReturn(sessionRequestResponse);

        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getLastResponseAttributes()).thenReturn(contextAttributes);
        when(mockContext.getAllNotConfiguredSessions()).thenReturn(Collections.singletonList(mockSession5New));
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(true);

        // when
        target.execute(mockContext);

        // then
        verifyNoInteractions(contextAttributes);
        verify(mockSession5New, times(0)).updateServerConfiguration(any(ServerConfiguration.class));
    }

    @Test
    public void captureIsDisabledIfNoFurtherNewSessionRequestsAreAllowed() {
        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse successResponse = StatusResponse.createSuccessResponse(
                mock(Logger.class),
                ResponseAttributesImpl.withJsonDefaults().withMultiplicity(5).build(),
                200,
                Collections.<String, List<String>>emptyMap()
        );
        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getAllNotConfiguredSessions()).thenReturn(Arrays.asList(mockSession5New, mockSession6New));
        when(mockClient.sendNewSessionRequest(any(AdditionalQueryParameters.class)))
                .thenReturn(successResponse) // first response valid
                .thenReturn(StatusResponse.createErrorResponse(mock(Logger.class), StatusResponse.HTTP_BAD_REQUEST)); // second response invalid
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(false);
        when(mockSession6New.canSendNewSessionRequest()).thenReturn(false);

        // when
        target.execute(mockContext);

        // verify for no session a new session request has been made
        verify(mockClient, times(0)).sendNewSessionRequest(mockContext);

        // verify both sessions disabled capture
        verify(mockSession5New, times(1)).disableCapture();
        verify(mockSession6New, times(1)).disableCapture();
    }

    @Test
    public void newSessionRequestsAreAbortedWhenTooManyRequestsResponseIsReceived() {
        // given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_TOO_MANY_REQUESTS);
        when(statusResponse.isErroneousResponse()).thenReturn(true);
        when(statusResponse.getRetryAfterInMilliseconds()).thenReturn(6543L);

        HTTPClient mockClient = mock(HTTPClient.class);
        when(mockContext.getHTTPClient()).thenReturn(mockClient);
        when(mockContext.getAllNotConfiguredSessions()).thenReturn(Arrays.asList(mockSession5New, mockSession6New));
        when(mockClient.sendNewSessionRequest(any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse); // second response invalid
        when(mockSession5New.canSendNewSessionRequest()).thenReturn(true);
        when(mockSession6New.canSendNewSessionRequest()).thenReturn(true);

        // when
        target.execute(mockContext);

        // verify for first new sessions a new session request has been made
        verify(mockClient, times(1)).sendNewSessionRequest(mockContext);

        // verify no changes on first
        verify(mockSession5New, times(1)).canSendNewSessionRequest();
        verifyNoMoreInteractions(mockSession5New);

        // verify second new session was not used at all
        verifyNoInteractions(mockSession6New);

        // verify any other session was not invoked
        verifyNoInteractions(mockSession1Open, mockSession2Open, mockSession3Finished, mockSession4Finished);

        // ensure also transition to CaptureOffState
        ArgumentCaptor<BeaconSendingCaptureOffState> argumentCaptor = ArgumentCaptor.forClass(BeaconSendingCaptureOffState.class);
        verify(mockContext, times(1)).setNextState(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().size(), is(equalTo(1)));
        assertThat(argumentCaptor.getAllValues().get(0).sleepTimeInMilliseconds, is(equalTo(6543L)));
    }

    @Test
    public void aBeaconSendingCaptureOnStateSendsFinishedSessions() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_OK);
        when(statusResponse.isErroneousResponse()).thenReturn(false);

        when(mockSession3Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession4Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.execute(mockContext);

        verify(mockSession3Finished, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession4Finished, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));

        // also verify that the session are removed
        verify(mockContext, times(1)).removeSession(mockSession3Finished);
        verify(mockContext, times(1)).removeSession(mockSession4Finished);
    }

    @Test
    public void aBeaconSendingCaptureOnStateClearsFinishedSessionsIfSendingIsNotAllowed() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        when(mockSession3Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(mock(StatusResponse.class));
        when(mockSession4Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(mock(StatusResponse.class));
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(false);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(false);

        //when calling execute
        target.execute(mockContext);

        verify(mockSession3Finished, times(0))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession4Finished, times(0))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));

        // also verify that the session are removed
        verify(mockContext, times(1)).removeSession(mockSession3Finished);
        verify(mockContext, times(1)).removeSession(mockSession4Finished);
    }

    @Test
    public void aBeaconSendingCaptureOnStateDoesNotRemoveFinishedSessionIfSendWasUnsuccessful() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(statusResponse.isErroneousResponse()).thenReturn(true);

        when(mockSession3Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession4Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(mock(StatusResponse.class));
        when(mockSession3Finished.isEmpty()).thenReturn(false);
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.execute(mockContext);

        verify(mockSession3Finished, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession4Finished, times(0))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));

        verify(mockContext, times(1)).getAllFinishedAndConfiguredSessions();
        verify(mockContext, times(0)).removeSession(any(SessionImpl.class));
    }

    @Test
    public void aBeaconSendingCaptureOnStateContinuesWithNextFinishedSessionIfSendingWasUnsuccessfulButBeaconIsEmpty() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse erroneousStatusResponse = mock(StatusResponse.class);
        when(erroneousStatusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_BAD_REQUEST);
        when(erroneousStatusResponse.isErroneousResponse()).thenReturn(true);

        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_OK);
        when(statusResponse.isErroneousResponse()).thenReturn(false);

        when(mockSession3Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(erroneousStatusResponse);
        when(mockSession4Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession3Finished.isEmpty()).thenReturn(true);
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.execute(mockContext);

        verify(mockSession3Finished, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession4Finished, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession3Finished, times(1)).clearCapturedData();
        verify(mockSession4Finished, times(1)).clearCapturedData();

        verify(mockContext, times(1)).getAllFinishedAndConfiguredSessions();
        verify(mockContext, times(1)).removeSession(mockSession3Finished);
        verify(mockContext, times(1)).removeSession(mockSession4Finished);
    }

    @Test
    public void sendingFinishedSessionsIsAbortedImmediatelyWhenTooManyRequestsResponseIsReceived() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_TOO_MANY_REQUESTS);
        when(statusResponse.isErroneousResponse()).thenReturn(true);
        when(statusResponse.getRetryAfterInMilliseconds()).thenReturn(12345L);

        when(mockSession3Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession4Finished.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession3Finished.isDataSendingAllowed()).thenReturn(true);
        when(mockSession4Finished.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.execute(mockContext);
        verify(mockSession3Finished, times(1)).isDataSendingAllowed();
        verify(mockSession3Finished, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verifyNoMoreInteractions(mockSession3Finished);

        // verify no interaction with second finished session
        verifyNoInteractions(mockSession4Finished);

        // verify no interactions with open sessions
        verifyNoInteractions(mockSession1Open, mockSession2Open);

        verify(mockContext, times(1)).getAllFinishedAndConfiguredSessions();
        verify(mockContext, times(0)).removeSession(any(SessionImpl.class));

        // ensure also transition to CaptureOffState
        ArgumentCaptor<BeaconSendingCaptureOffState> argumentCaptor = ArgumentCaptor.forClass(BeaconSendingCaptureOffState.class);
        verify(mockContext, times(1)).setNextState(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().size(), is(equalTo(1)));
        assertThat(argumentCaptor.getAllValues().get(0).sleepTimeInMilliseconds, is(equalTo(12345L)));
    }

    @Test
    public void aBeaconSendingCaptureOnStateSendsOpenSessionsIfNotExpired() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(true);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(true);

        //when calling execute
        target.execute(mockContext);

        verify(mockSession1Open, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession2Open, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockContext, times(1)).setLastOpenSessionBeaconSendTime(org.mockito.Mockito.anyLong());
    }

    @Test
    public void aBeaconSendingCaptureOnStateClearsOpenSessionDataIfSendingIsNotAllowed() {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(false);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(false);

        //when calling execute
        target.execute(mockContext);

        verify(mockSession1Open, times(0))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession2Open, times(0))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession1Open, times(1)).clearCapturedData();
        verify(mockSession2Open, times(1)).clearCapturedData();
        verify(mockContext, times(1)).setLastOpenSessionBeaconSendTime(org.mockito.Mockito.anyLong());
    }

    @Test
    public void sendingOpenSessionsIsAbortedImmediatelyWhenTooManyRequestsResponseIsReceived() {
        //given
        StatusResponse statusResponse = mock(StatusResponse.class);
        when(statusResponse.getResponseCode()).thenReturn(StatusResponse.HTTP_TOO_MANY_REQUESTS);
        when(statusResponse.isErroneousResponse()).thenReturn(true);
        when(statusResponse.getRetryAfterInMilliseconds()).thenReturn(12345L);

        when(mockSession1Open.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession2Open.sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class)))
                .thenReturn(statusResponse);
        when(mockSession1Open.isDataSendingAllowed()).thenReturn(true);
        when(mockSession2Open.isDataSendingAllowed()).thenReturn(true);

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //when calling execute
        target.execute(mockContext);

        verify(mockSession1Open, times(1))
                .sendBeacon(any(HTTPClientProvider.class), any(AdditionalQueryParameters.class));
        verify(mockSession1Open, times(1)).isDataSendingAllowed();
        verifyNoMoreInteractions(mockSession1Open);

        // ensure that second session was not invoked at all
        verifyNoInteractions(mockSession2Open);

        // ensure also transition to CaptureOffState
        ArgumentCaptor<BeaconSendingCaptureOffState> argumentCaptor = ArgumentCaptor.forClass(BeaconSendingCaptureOffState.class);
        verify(mockContext, times(1)).setNextState(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().size(), is(equalTo(1)));
        assertThat(argumentCaptor.getAllValues().get(0).sleepTimeInMilliseconds, is(equalTo(12345L)));
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToCaptureOffStateWhenCapturingGotDisabled() {
        //given
        when(mockContext.isCaptureOn()).thenReturn(false);
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        // when calling execute
        target.execute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).handleStatusResponse(org.mockito.Mockito.any(StatusResponse.class));
        verify(mockContext, times(1)).isCaptureOn();

        verify(mockContext, times(1)).setNextState(isA(BeaconSendingCaptureOffState.class));
    }
}
