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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the session implementation having some knowledge of the internals of beacon and beacon cache.
 */
@SuppressWarnings("resource")
public class SessionImplTest {

    private Logger mockLogger;
    private OpenKitComposite mockParent;
    private Beacon mockBeacon;

    @Before
    public void setUp() {

        // mock Logger
        mockLogger = mock(Logger.class);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        when(mockLogger.isDebugEnabled()).thenReturn(true);

        mockParent = mock(OpenKitComposite.class);

        // mock Beacon
        mockBeacon = mock(Beacon.class);
    }

    @Test
    public void enterActionWithNullActionNameGivesNullRootActionObject() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction(null);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] enterAction: actionName must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionWithEmptyActionNameGivesNullRootActionObject() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction("");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] enterAction: actionName must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionWithNonEmptyNamesGivesRootAction() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction("Some action");

        // then
        assertThat(obtained, is(instanceOf(RootActionImpl.class)));
    }

    @Test
    public void enterActionAlwaysGivesANewInstance() {
        // given
        SessionImpl target = createSession().build();

        // when
        RootAction obtainedOne = target.enterAction("Some action");
        RootAction obtainedTwo = target.enterAction("Some action");

        // then
        assertThat(obtainedOne, is(notNullValue()));
        assertThat(obtainedTwo, is(notNullValue()));
        assertThat(obtainedOne, is(not(sameInstance(obtainedTwo))));
    }

    @Test
    public void enterActionAddsNewlyCreatedActionToTheListOfChildObjects() {
        // given
        SessionImpl target = createSession().build();

        // when entering the first time
        RootAction obtainedOne = target.enterAction("Some action");

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject)obtainedOne))));

        // and when entering a second time
        RootAction obtainedTwo = target.enterAction("Some action");

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Arrays.asList((OpenKitObject) obtainedOne, (OpenKitObject)obtainedTwo))));
    }

    @Test
    public void enterActionLogsInvocation() {
        // given
        String actionName = "Some action";
        SessionImpl target = createSession().build();

        // when
        RootAction obtained = target.enterAction(actionName);

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] enterAction(" + actionName + ")");
        verify(mockLogger, times(1)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void identifyUserWithNullTagDoesNothing() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.identifyUser(null);

        // then
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] identifyUser: userTag must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void identifyUserWithEmptyTagDoesNothing() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.identifyUser("");

        // then
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] identifyUser: userTag must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyNoMoreInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void identifyUserWithNonEmptyTagReportsUser() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.identifyUser("user");

        // then
        verify(mockLogger, times(0)).warning(anyString());
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).identifyUser("user");
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserMultipleTimesAlwaysCallsBeacon() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.identifyUser("user");
        target.identifyUser("user");

        // then
        verify(mockLogger, times(0)).warning(anyString());
        verify(mockBeacon, times(2)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(2)).identifyUser("user");
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void identifyUserLogsInvocation() {
        // given
        String userTag = "user";
        SessionImpl target = createSession().build();

        // when
        target.identifyUser(userTag);

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] identifyUser(" + userTag + ")");
        verify(mockLogger, times(1)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashWithNullErrorNameDoesNotReportAnything() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash(null, "some reason", "some stack trace");

        // then verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: errorName must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyZeroInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashWithEmptyErrorNameDoesNotReportAnything() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing empty errorName
        target.reportCrash("", "some reason", "some stack trace");

        // verify the correct methods being called
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] reportCrash: errorName must not be null or empty");
        verify(mockBeacon, times(1)).getSessionNumber();
        verify(mockBeacon, times(1)).startSession();
        verifyZeroInteractions(mockBeacon);
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void reportingCrashWithNullReasonAndStacktraceWorks() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash("errorName", null, null);

        // then verify the correct methods being called
        verify(mockBeacon, times(1)).reportCrash("errorName", null, null);
    }

    @Test
    public void reportingCrashWithEmptyReasonAndStacktraceStringWorks() {
        // given
        SessionImpl target = createSession().build();

        // when reporting a crash, passing null values
        target.reportCrash("errorName", "", "" );

        // then verify the correct methods being called
        verify(mockBeacon, times(1)).reportCrash("errorName", "", "");
    }

    @Test
    public void reportingCrashWithSameDataMultipleTimesForwardsEachCallToBeacon() {
        // given
        SessionImpl target = createSession().build();

        String errorName = "error name";
        String reason = "error reason";
        String stacktrace = "the stacktrace causing the error";

        // when
        target.reportCrash(errorName, reason, stacktrace);
        target.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(mockBeacon, times(2)).reportCrash(errorName, reason, stacktrace);
    }

    @Test
    public void reportCrashLogsInvocation() {
        // given
        SessionImpl target = createSession().build();

        String errorName = "error name";
        String reason = "error reason";
        String stacktrace = "the stacktrace causing the error";

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                "SessionImpl [sn=0] reportCrash(" + errorName + ", " + reason + ", " + stacktrace + ")");
    }

    @Test
    public void endSessionFinishesSessionOnBeacon() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(1234L);
        SessionImpl target = createSession().build();

        // when
        target.end();

        // then
        verify(mockBeacon, times(1)).endSession();
    }

    @Test
    public void endingAnAlreadyEndedSessionDoesNothing() {
        // given
        SessionImpl target = createSession().build();

        // when ending a session twice
        target.end();

        // then
        verify(mockBeacon, times(1)).endSession();
        assertThat(target.getState().isFinished(),  is(true));

        reset(mockBeacon);

        // and when
        target.end();

        // then
        verify(mockBeacon, times(0)).endSession();
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void endingASessionImplicitlyClosesAllOpenChildObjects() throws IOException {
        // given
        final OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        SessionImpl target = createSession().build();

        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.end();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();
        verifyNoMoreInteractions(childObjectOne, childObjectTwo);
    }

    @Test
    public void ifChildObjectThrowsIOExceptionWhileBeingClosedExceptionIsLogged() throws IOException {
        // given
        IOException exception = new IOException("oops");
        OpenKitObject childObjectOne = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectOne).close();
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        doThrow(exception).when(childObjectTwo).close();

        SessionImpl target = createSession().build();
        target.storeChildInList(childObjectOne);
        target.storeChildInList(childObjectTwo);

        // when
        target.end();

        // then
        verify(childObjectOne, times(1)).close();
        verify(childObjectTwo, times(1)).close();

        verify(mockLogger, times(2)).error(contains("Caught IOException while closing OpenKitObject"), eq(exception));
    }

    @Test
    public void endLogsInvocation() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.end();

        // then
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] end()");
    }

    @Test
    public void sendBeaconForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();
        HTTPClientProvider clientProvider = mock(HTTPClientProvider.class);

        // when
        target.sendBeacon(clientProvider);

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).send(clientProvider);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void clearCapturedDataForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.clearCapturedData();

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).clearData();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void isEmptyForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();

        // when
        target.isEmpty();

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).isEmpty();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void updateServerConfigurationForwardsCallToBeacon() {
        // given
        SessionImpl target = createSession().build();
        ServerConfiguration mockServerConfiguration = mock(ServerConfiguration.class);

        // when
        target.updateServerConfiguration(mockServerConfiguration);

        // then verify the proper methods being called
        verify(mockBeacon, times(1)).startSession();
        verify(mockBeacon, times(1)).updateServerConfiguration(mockServerConfiguration);
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void aNewlyCreatedSessionIsNotFinished() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isFinished(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
    }

    @Test
    public void aNewlyCreatedSessionIsNotInStateConfigured() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isConfigured(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
    }

    @Test
    public void aNotConfiguredNotFinishedSessionHasCorrectState()
    {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isConfigured(), is(false));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isFinished(), is(false));
    }

    @Test
    public void aConfiguredNotFinishedSessionHasCorrectState()
    {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);
        SessionImpl target = createSession().build();

        // when, then
        assertThat(target.getState().isConfigured(), is(true));
        assertThat(target.getState().isConfiguredAndOpen(), is(true));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isFinished(), is(false));
    }

    @Test
    public void aNotConfiguredFinishedSessionHasCorrectState()
    {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();
        target.end();

        // when, then
        assertThat(target.getState().isConfigured(), is(false));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(false));
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void aConfiguredFinishedSessionHasCorrectState()
    {
        // given
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);
        SessionImpl target = createSession().build();
        target.end();

        // then
        assertThat(target.getState().isConfigured(), is(true));
        assertThat(target.getState().isConfiguredAndOpen(), is(false));
        assertThat(target.getState().isConfiguredAndFinished(), is(true));
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void aSessionIsFinishedIfEndIsCalled() {
        // given
        SessionImpl target = createSession().build();

        // when end is called
        target.end();

        // then the session is ended
        assertThat(target.getState().isFinished(), is(true));
    }

    @Test
    public void enterActionGivesNullRootActionIfSessionIsAlreadyEnded() {
        // given
        SessionImpl target = createSession().build();
        target.end();

        // when entering an action on already ended session
        RootAction obtained = target.enterAction("Test");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
    }

    @Test
    public void identifyUserDoesNothingIfSessionIsEnded() {
        // given
        SessionImpl target = createSession().build();
        target.end();

        // when trying to identify a user on an ended session
        target.identifyUser("Jane Doe");

        // then
        verify(mockBeacon, times(0)).identifyUser(anyString());
    }

    @Test
    public void reportCrashDoesNothingIfSessionIsEnded() {
        // given
        SessionImpl target = createSession().build();
        target.end();

        // when trying to identify a user on an ended session
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        verify(mockBeacon, times(0)).reportCrash(anyString(), anyString(), anyString());
    }

    @Test
    public void closeSessionEndsTheSession() {
        // given
        when(mockBeacon.getCurrentTimestamp()).thenReturn(4321L);
        SessionImpl target = spy(createSession().build());

        // when
        target.close();

        // then
        verify(target, times(1)).end();
        verify(mockBeacon, times(1)).endSession();
    }

    @Test
    public void traceWebRequestWithValidUrlStringGivesAppropriateTracer() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerStringURL.class)));
    }

    @Test
    public void traceWebRequestWithValidUrlStringAddsTracerToListOfChildren() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("https://www.google.com");

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject)obtained))));
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest((String) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] traceWebRequest (String): url must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning("SessionImpl [sn=0] traceWebRequest (String): url must not be null or empty");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void tracingAStringWebRequestWithInvalidURLIsNotAllowed() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest("foobar/://");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
            "SessionImpl [sn=0] traceWebRequest (String): url \"foobar/://\" does not have a valid scheme");
    }

    @Test
    public void traceWebRequestWithStringLogsInvocation() {
        // given
        String url = "https://localhost";
        SessionImpl target = createSession().build();

        // when
        target.traceWebRequest(url);

        // then
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] traceWebRequest (String) (" + url + ")");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void traceWebRequestWithUrlConnectionLogsInvocation() {
        // given
        String connectionString = "connection";
        URLConnection connection = mock(URLConnection.class);
        when(connection.toString()).thenReturn(connectionString);
        SessionImpl target = createSession().build();

        // when
        target.traceWebRequest(connection);

        // then
        verify(mockLogger, times(2)).isDebugEnabled(); // 1 on invocation + 1 in getTag when setting tag on connection
        verify(mockLogger, times(1)).debug("SessionImpl [sn=0] traceWebRequest (URLConnection) (" + connection + ")");
    }

    @Test
    public void traceWebRequestWithValidURLConnectionGivesAppropriateTracer() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(instanceOf(WebRequestTracerURLConnection.class)));
    }

    @Test
    public void traceWebRequestWithValidURLConnectionAddsTracerToListOfChildren() {
        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(target.getCopyOfChildObjects(), is(equalTo(Collections.singletonList((OpenKitObject)obtained))));
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {

        // given
        SessionImpl target = createSession().build();

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
            "SessionImpl [sn=0] traceWebRequest (URLConnection): connection must not be null");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void traceWebRequestWithURLConnectionArgumentGivesNullTracerIfSessionIsEnded() {

        // given
        SessionImpl target = createSession().build();
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void traceWebRequestWithStringArgumentGivesNullTracerIfSessionIsEnded() {

        // given
        SessionImpl target = createSession().build();
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest("http://www.google.com");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void onChildClosedRemovesChildFromList() {
        // given
        SessionImpl target = createSession().build();
        OpenKitObject childObject = mock(OpenKitObject.class);
        target.storeChildInList(childObject);

        // when child gets closed
        target.onChildClosed(childObject);

        // then
        assertThat(target.getCopyOfChildObjects(), is(empty()));
    }

    @Test
    public void toStringReturnsAppropriateResult() {
        // given
        when(mockBeacon.getSessionNumber()).thenReturn(21);

        SessionImpl target = createSession().build();

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is(equalTo("SessionImpl [sn=21] ")));
    }

    @Test
    public void aNewSessionCanSendNewSessionRequests() {
        // given
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.canSendNewSessionRequest();

        // then
        assertThat(obtained, is(equalTo(true)));
    }

    @Test
    public void canSendNewSessionRequestIsFalseIfAllRequestsAreUsedUp() {
        // given
        SessionImpl target = createSession().build();

        // when, then
        for(int i = SessionImpl.MAX_NEW_SESSION_REQUESTS; i > 0; i--) {
            assertThat(target.canSendNewSessionRequest(), is(true));

            target.decreaseNumRemainingSessionRequests();
        }

        // then
        assertThat(target.canSendNewSessionRequest(), is(false));
    }

    @Test
    public void isDataSendingAllowedReturnsTrueForConfiguredAndCaptureEnabledSession() {
        // given
        when(mockBeacon.isCaptureEnabled()).thenReturn(true);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);

        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(true)));
    }

    @Test
    public void isDataSendingAllowedReturnsFalseForNotConfiguredSession() {
        // given
        when(mockBeacon.isCaptureEnabled()).thenReturn(true);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void isDataSendingAllowedReturnsFalseForCaptureDisabledSession() {
        // given
        when(mockBeacon.isCaptureEnabled()).thenReturn(false);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(true);
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void isDataSendingAllowedReturnsFalseForNotConfiguredAndCaptureDisabledSession() {
        // given
        when(mockBeacon.isCaptureEnabled()).thenReturn(false);
        when(mockBeacon.isServerConfigurationSet()).thenReturn(false);
        SessionImpl target = createSession().build();

        // when
        boolean obtained = target.isDataSendingAllowed();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void enableCaptureDelegatesToBeacon() {
        // given
        SessionImpl target = createSession().build();
        reset(mockBeacon);

        // when
        target.enableCapture();

        // then
        verify(mockBeacon, times(1)).enableCapture();
        verifyNoMoreInteractions(mockBeacon);
    }

    @Test
    public void disableCaptureDelegatesToBeacon() {
        // given
        SessionImpl target = createSession().build();
        reset(mockBeacon);

        // when
        target.disableCapture();

        // then
        verify(mockBeacon, times(1)).disableCapture();
        verifyNoMoreInteractions(mockBeacon);
    }

    private SessionBuilder createSession() {
        SessionBuilder builder = new SessionBuilder();
        builder.logger = mockLogger;
        builder.parent = mockParent;
        builder.beacon = mockBeacon;

        return builder;
    }

    private static class SessionBuilder {
        private Logger logger;
        private OpenKitComposite parent;
        private Beacon beacon;

        private SessionBuilder with(Logger logger) {
            this.logger = logger;
            return this;
        }

        private SessionBuilder with(OpenKitComposite parent) {
            this.parent = parent;
            return this;
        }

        private SessionBuilder with(Beacon beacon) {
            this.beacon = beacon;
            return this;
        }

        private SessionImpl build() {
            return new SessionImpl(logger, parent, beacon);
        }
    }
}
