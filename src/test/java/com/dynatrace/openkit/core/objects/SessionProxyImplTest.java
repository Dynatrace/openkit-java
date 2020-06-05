/**
 * Copyright 2018-2019 Dynatrace LLC
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
package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.SessionWatchdog;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLConnection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.endsWith;
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

public class SessionProxyImplTest {

    private Logger mockLogger;
    private OpenKitComposite mockParent;
    private SessionImpl mockSession;
    private Beacon mockBeacon;
    private SessionImpl mockSplitSession1;
    private Beacon mockSplitBeacon1;
    private SessionImpl mockSplitSession2;
    private Beacon mockSplitBeacon2;
    private SessionCreator mockSessionCreator;
    private TimingProvider mockTimingProvider;
    private ServerConfiguration mockServerConfiguration;
    private BeaconSender mockBeaconSender;
    private SessionWatchdog mockSessionWatchdog;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        when(mockLogger.isWarnEnabled()).thenReturn(true);

        mockParent = mock(OpenKitComposite.class);
        mockTimingProvider = mock(TimingProvider.class);
        mockServerConfiguration = mock(ServerConfiguration.class);

        mockBeacon = mock(Beacon.class);
        mockSession = mock(SessionImpl.class);
        when(mockSession.getBeacon()).thenReturn(mockBeacon);

        mockSplitBeacon1 = mock(Beacon.class);
        mockSplitSession1 = mock(SessionImpl.class);
        when(mockSplitSession1.getBeacon()).thenReturn(mockSplitBeacon1);

        mockSplitBeacon2 = mock(Beacon.class);
        mockSplitSession2 = mock(SessionImpl.class);
        when(mockSplitSession2.getBeacon()).thenReturn(mockSplitBeacon2);

        mockSessionCreator = mock(SessionCreator.class);
        when(mockSessionCreator.createSession(any(OpenKitComposite.class)))
                .thenReturn(mockSession)
                .thenReturn(mockSplitSession1)
                .thenReturn(mockSplitSession2);

        mockBeaconSender = mock(BeaconSender.class);
        mockSessionWatchdog = mock(SessionWatchdog.class);
    }

    @Test
    public void constructingASessionCreatorCreatesASessionInitially() {
        // given, when
        SessionProxyImpl target = createSessionProxy();

        // then
        verify(mockSessionCreator, times(1)).createSession(target);
    }

    @Test
    public void initiallyCreatedSessionIsInitializedWithServerConfiguration() {
        // given
        ServerConfiguration initialServerConfig = mock(ServerConfiguration.class);
        when(mockBeaconSender.getLastServerConfiguration()).thenReturn(initialServerConfig);

        // when
        SessionProxyImpl target = createSessionProxy();

        // then
        verify(mockSessionCreator, times(1)).createSession(target);
        verify(mockSession, times(1)).initializeServerConfiguration(initialServerConfig);
    }

    @Test
    public void aNewlyCreatedSessionProxyIsNotFinished() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // then
        assertThat(target.isFinished(), is(false));
    }

    @Test
    public void initiallyCreatedSessionRegistersServerConfigurationUpdateCallback() {
        // given, when
        SessionProxyImpl target = createSessionProxy();

        // then
        verify(mockBeacon, times(1)).setServerConfigurationUpdateCallback(target);
    }

    @Test
    public void initiallyCreatedSessionIsAddedToTheBeaconSender() {
        // given
        createSessionProxy();

        // then
        verify(mockBeaconSender, times(1)).addSession(mockSession);
    }

    @Test
    public void initiallyCreatedSessionProvidesStartTimeAsLastInteractionTime() {
        // given
        long startTime = 73;
        when(mockBeacon.getSessionStartTime()).thenReturn(startTime);

        // when
        SessionProxyImpl target = createSessionProxy();

        // then
        assertThat(target.getLastInteractionTime(), is(startTime));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// enter action tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void enterActionWithNullActionNameGivesNullRootActionObject() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        RootAction obtained = target.enterAction(null);

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        verify(mockLogger, times(1)).warning(endsWith("enterAction: actionName must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionWithEmptyActionNameGivesNullRootActionObject() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        RootAction obtained = target.enterAction("");

        // then
        assertThat(obtained, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(mockLogger, times(1)).warning(endsWith("enterAction: actionName must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionDelegatesToRealSession() {
        // given
        String actionName = "some action";
        SessionProxyImpl target = createSessionProxy();

        // when entering the first time
        target.enterAction(actionName);

        // then
        verify(mockSession, times(1)).enterAction(actionName);
    }

    @Test
    public void enterActionLogsInvocation() {
        // given
        String actionName = "Some action";
        SessionProxyImpl target = createSessionProxy();

        // when
        target.enterAction(actionName);

        // then
        verify(mockLogger, times(1)).debug(endsWith("enterAction(" + actionName + ")"));
        verify(mockLogger, times(1)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void enterActionGivesNullRootActionIfSessionIsAlreadyEnded() {
        // given
        SessionProxyImpl target = createSessionProxy();
        target.end();

        // when entering an action on already ended session
        RootAction obtained = target.enterAction("Test");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
    }

    @Test
    public void enterActionIncreasesTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelActionCount(), is(0));

        // when
        target.enterAction("test");

        // then
        assertThat(target.getTopLevelActionCount(), is(1));
    }

    @Test
    public void enterActionSetsLastInterActionTime() {
        // given
        long sessionCreationTime = 13;
        long lastInteractionTime = 17;
        when(mockBeacon.getSessionStartTime()).thenReturn(sessionCreationTime);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(lastInteractionTime);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(sessionCreationTime));

        // when
        target.enterAction("test");

        // then
        assertThat(target.getLastInteractionTime(), is(lastInteractionTime));
    }

    @Test
    public void enterActionDoesNotSplitSessionIfNoServerConfigurationIsSet() {
        // given
        int eventCount = 10;

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.enterAction("some action");
        }

        // then
        assertThat(target.getTopLevelActionCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void enterActionDoesNotSplitSessionIfSessionSplitByEventDisabled() {
        // given
        int eventCount = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.enterAction("some action");
        }

        // then
        assertThat(target.getTopLevelActionCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void enterActionSplitsSessionIfSessionSplitByEventsEnabled() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.enterAction("some action");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("some other action");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void enterActionSplitsSessionEveryNthEvent() {
        // given
        int maxEventCount = 3;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.enterAction("action 1");
        target.enterAction("action 2");
        target.enterAction("action 3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("action 4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.enterAction("action 5");
        target.enterAction("action 6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("action 7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void enterActionSplitsSessionEveryNthEventFromFirstServerConfiguration() {
        // given
        int maxEventCount = 3;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);
        when(mockServerConfiguration.merge(any(ServerConfiguration.class))).thenCallRealMethod();

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        ServerConfiguration ignoredServerConfig = mock(ServerConfiguration.class);
        when(ignoredServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(ignoredServerConfig.getMaxEventsPerSession()).thenReturn(5);

        target.onServerConfigurationUpdate(ignoredServerConfig);

        // when
        target.enterAction("action 1");
        target.enterAction("action 2");
        target.enterAction("action 3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("action 4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.enterAction("action 5");
        target.enterAction("action 6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("action 7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void enterActionCallsWatchdogToCloseOldSessionOnSplitByEvents() {
        // given
        int sessionIdleTimeout = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(sessionIdleTimeout);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.enterAction("some action");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("some other action");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sessionIdleTimeout / 2);
    }

    @Test
    public void enterActionAddsSplitSessionToBeaconSenderOnSplitByEvents() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        verify(mockBeaconSender, times(1)).addSession(mockSession);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.enterAction("some action");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("some other action");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockBeaconSender, times(1)).addSession(mockSplitSession1);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// identify user tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void identifyUserWithNullTagDoesNothing() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        target.identifyUser(null);

        // then
        verify(mockLogger, times(1)).warning(endsWith("identifyUser: userTag must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).identifyUser(anyString());
    }

    @Test
    public void identifyUserWithEmptyTagDoesNothing() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        target.identifyUser("");

        // then
        verify(mockLogger, times(1)).warning(endsWith("identifyUser: userTag must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).identifyUser(anyString());
    }

    @Test
    public void identifyUserWithNonEmptyTagReportsUser() {
        // given
        String userTag = "user";
        SessionProxyImpl target = createSessionProxy();

        // when
        target.identifyUser(userTag);

        // then
        verify(mockLogger, times(0)).warning(anyString());
        verify(mockSession, times(1)).identifyUser(userTag);
    }

    @Test
    public void identifyUserLogsInvocation() {
        // given
        String userTag = "user";
        SessionProxyImpl target = createSessionProxy();

        // when
        target.identifyUser(userTag);

        // then
        verify(mockLogger, times(1)).debug(endsWith("identifyUser(" + userTag + ")"));
        verify(mockLogger, times(1)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void identifyUserDoesNothingIfSessionIsEnded() {
        // given
        SessionProxyImpl target = createSessionProxy();
        target.end();

        // when trying to identify a user on an ended session
        target.identifyUser("Jane Doe");

        // then
        verify(mockSession, times(0)).identifyUser(anyString());
    }

    @Test
    public void identifyUserDoesNotIncreaseTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelActionCount(), is(0));

        // when
        target.identifyUser("Jane Doe");

        // then
        assertThat(target.getTopLevelActionCount(), is(0));
    }

    @Test
    public void identifyUserSetsLastInterActionTime() {
        // given
        long sessionCreationTime = 13;
        long lastInteractionTime = 17;
        when(mockBeacon.getSessionStartTime()).thenReturn(sessionCreationTime);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(lastInteractionTime);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(sessionCreationTime));

        // when
        target.identifyUser("Jane Doe");

        // then
        assertThat(target.getLastInteractionTime(), is(lastInteractionTime));
    }

    @Test
    public void identifyUserDoesNotSplitSession() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < 10; i++) {
            target.identifyUser("user 1");
        }

        // then
        verifyNoMoreInteractions(mockSessionCreator);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// report crash tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void reportingCrashWithNullErrorNameDoesNotReportAnything() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when reporting a crash, passing null values
        target.reportCrash(null, "some reason", "some stack trace");

        // then verify the correct methods being called
        verify(mockLogger, times(1)).warning(endsWith("reportCrash: errorName must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).reportCrash(anyString(), anyString(), anyString());
    }

    @Test
    public void reportingCrashWithEmptyErrorNameDoesNotReportAnything() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when reporting a crash, passing empty errorName
        target.reportCrash("", "some reason", "some stack trace");

        // verify the correct methods being called
        verify(mockLogger, times(1)).warning(endsWith("reportCrash: errorName must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).reportCrash(anyString(), anyString(), anyString());
    }

    @Test
    public void reportingCrashWithNullReasonAndStacktraceWorks() {
        // given
        String errorName = "errorName";
        String errorReason = null;
        String stacktrace = null;
        SessionProxyImpl target = createSessionProxy();

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when reporting a crash, passing null values
        target.reportCrash(errorName, errorReason, stacktrace);

        // then verify the correct methods being called
        verify(mockSession, times(1)).reportCrash(errorName, errorReason, stacktrace);
    }

    @Test
    public void reportingCrashWithEmptyReasonAndStacktraceStringWorks() {
        // given
        String errorName = "errorName";
        String errorReason = "";
        String stacktrace = "";
        SessionProxyImpl target = createSessionProxy();

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when reporting a crash, passing null values
        target.reportCrash(errorName, errorReason, stacktrace);

        // then verify the correct methods being called
        verify(mockSession, times(1)).reportCrash(errorName, errorReason, stacktrace);
    }

    @Test
    public void reportCrashLogsInvocation() {
        // given
        SessionProxyImpl target = createSessionProxy();

        String errorName = "error name";
        String reason = "error reason";
        String stacktrace = "the stacktrace causing the error";

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(
                endsWith("reportCrash(" + errorName + ", " + reason + ", " + stacktrace + ")"));
    }

    @Test
    public void reportCrashDoesNothingIfSessionIsEnded() {
        // given
        SessionProxyImpl target = createSessionProxy();
        target.end();

        // when trying to identify a user on an ended session
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        verify(mockSession, times(0)).reportCrash(anyString(), anyString(), anyString());
    }

    @Test
    public void reportCrashDoesNotIncreaseTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelActionCount(), is(0));

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        assertThat(target.getTopLevelActionCount(), is(0));
    }

    @Test
    public void reportCrashAlwaysSplitsSessionAfterReportingCrash() {
        // given
        // explicitly disable session splitting
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(-1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash("error 1", "reason 1", "stacktrace 1");

        // then
        verify(mockSession, times(1)).reportCrash("error 1", "reason 1", "stacktrace 1");
        verify(mockSessionCreator, times(2)).createSession(target);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, mockServerConfiguration.getSendIntervalInMilliseconds());

        // and when
        target.reportCrash("error 2", "reason 2", "stacktrace 2");

        // then
        verify(mockSplitSession1, times(1)).reportCrash("error 2", "reason 2", "stacktrace 2");
        verify(mockSessionCreator, times(3)).createSession(target);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSplitSession1, mockServerConfiguration.getSendIntervalInMilliseconds());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// trace web request (with string url) tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void traceWebRequestWithValidUrlStringDelegatesToRealSession() {
        // given
        String url = "https://www.google.com";
        SessionProxyImpl target = createSessionProxy();

        // when
        target.traceWebRequest(url);

        // then
        verify(mockSession, times(1)).traceWebRequest(url);
    }

    @Test
    public void tracingANullStringWebRequestIsNotAllowed() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        WebRequestTracer obtained = target.traceWebRequest((String) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(endsWith("traceWebRequest (String): url must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).traceWebRequest(anyString());
    }

    @Test
    public void tracingAnEmptyStringWebRequestIsNotAllowed() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        WebRequestTracer obtained = target.traceWebRequest("");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(endsWith("traceWebRequest (String): url must not be null or empty"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).traceWebRequest(anyString());
    }

    @Test
    public void tracingAStringWebRequestWithInvalidURLIsNotAllowed() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        WebRequestTracer obtained = target.traceWebRequest("foobar/://");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
                endsWith("traceWebRequest (String): url \"foobar/://\" does not have a valid scheme"));
        verify(mockSession, times(0)).traceWebRequest(anyString());
    }

    @Test
    public void traceWebRequestWithStringArgumentGivesNullTracerIfSessionIsEnded() {
        // given
        SessionProxyImpl target = createSessionProxy();
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest("http://www.google.com");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void traceWebRequestWithStringLogsInvocation() {
        // given
        String url = "https://localhost";
        SessionProxyImpl target = createSessionProxy();

        // when
        target.traceWebRequest(url);

        // then
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(endsWith("traceWebRequest (String) (" + url + ")"));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void traceWebRequestWithStringDoesNotIncreaseTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelActionCount(), is(0));

        // when
        target.traceWebRequest("https://localhost");

        // then
        assertThat(target.getTopLevelActionCount(), is(0));
    }

    @Test
    public void traceWebRequestWithStringUrlSetsLastInterActionTime() {
        // given
        long sessionCreationTime = 13;
        long lastInteractionTime = 17;
        when(mockBeacon.getSessionStartTime()).thenReturn(sessionCreationTime, lastInteractionTime);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(lastInteractionTime);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(sessionCreationTime));

        // when
        target.traceWebRequest("https://localhost");

        // then
        assertThat(target.getLastInteractionTime(), is(lastInteractionTime));
    }

    @Test
    public void traceWebRequestDoesNotSplitSession() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < 10; i++) {
            target.traceWebRequest("https://localhost/" + (i+1));
        }

        // then
        verifyNoMoreInteractions(mockSessionCreator);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// trace web request (url connection) tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void traceWebRequestWithValidURLDelegatesToRealSession() {
        // given
        URLConnection urlConnection = mock(URLConnection.class);
        SessionProxyImpl target = createSessionProxy();

        // when
        target.traceWebRequest(urlConnection);

        // then
        verify(mockSession, times(1)).traceWebRequest(urlConnection);
    }

    @Test
    public void tracingANullURLConnectionWebRequestIsNotAllowed() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        WebRequestTracer obtained = target.traceWebRequest((URLConnection) null);

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));

        // and a warning message has been generated
        verify(mockLogger, times(1)).warning(
                endsWith("traceWebRequest (URLConnection): connection must not be null"));
        verifyNoMoreInteractions(mockLogger);
        verify(mockSession, times(0)).traceWebRequest(any(URLConnection.class));
    }

    @Test
    public void traceWebRequestWithURLConnectionArgumentGivesNullTracerIfSessionIsEnded() {
        // given
        SessionProxyImpl target = createSessionProxy();
        target.end();

        // when
        WebRequestTracer obtained = target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullWebRequestTracer.class)));
    }

    @Test
    public void traceWebRequestWithUrlConnectionLogsInvocation() {
        // given
        String connectionString = "connection";
        URLConnection connection = mock(URLConnection.class);
        when(connection.toString()).thenReturn(connectionString);
        SessionProxyImpl target = createSessionProxy();

        // when
        target.traceWebRequest(connection);

        // then
        verify(mockLogger, times(1)).isDebugEnabled();
        verify(mockLogger, times(1)).debug(endsWith("traceWebRequest (URLConnection) (" + connectionString + ")"));
    }

    @Test
    public void traceWebRequestWithUrlConnectionDoesNotIncreaseTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelActionCount(), is(0));

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(target.getTopLevelActionCount(), is(0));
    }

    @Test
    public void traceWebRequestWithUrlConnectionSetsLastInterActionTime() {
        // given
        long sessionCreationTime = 13;
        long lastInteractionTime = 17;
        when(mockBeacon.getSessionStartTime()).thenReturn(sessionCreationTime);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(lastInteractionTime);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(sessionCreationTime));

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(target.getLastInteractionTime(), is(lastInteractionTime));
    }

    @Test
    public void traceWebRequestWithUrlConnectionDoesNotSplitSession() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < 10 ; i++) {
            target.traceWebRequest(mock(URLConnection.class));
        }

        // then
        verifyNoMoreInteractions(mockSessionCreator);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// end tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void endFinishesTheSession() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        target.end();

        // then
        verify(mockParent, times(1)).onChildClosed(target);
        assertThat(target.isFinished(), is(true));
    }

    @Test
    public void endingAnAlreadyEndedSessionDoesNothing() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when ending a session twice
        target.end();

        // then
        verify(mockParent, times(1)).onChildClosed(target);

        // and when
        target.end();

        // then
        verifyNoMoreInteractions(mockParent);
    }

    @Test
    public void endingASessionImplicitlyClosesAllOpenChildObjects() throws IOException {
        // given
        final OpenKitObject childObjectOne = mock(OpenKitObject.class);
        OpenKitObject childObjectTwo = mock(OpenKitObject.class);
        SessionProxyImpl target = createSessionProxy();

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

        SessionProxyImpl target = createSessionProxy();
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
        SessionProxyImpl target = createSessionProxy();

        // when
        target.end();

        // then
        verify(mockLogger, times(1)).debug(endsWith("end()"));
    }

    @Test
    public void endRemovesSessionProxyFromSessionWatchdog() {
        // given
        SessionProxyImpl target = createSessionProxy();

        // when
        target.end();

        // then
        verify(mockSessionWatchdog, times(1)).removeFromSplitByTimeout(target);
    }

    @Test
    public void closeSessionEndsTheSession() {
        // given
        SessionProxyImpl target = spy(createSessionProxy());

        // when
        target.close();

        // then
        verify(target, times(1)).end();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// split session by time tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void splitSessionByTimeReturnsMinusOneIfSessionProxyIsFinished() {
        // given
        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.end();

        // when
        long obtained = target.splitSessionByTime();

        // then
        assertThat(obtained, is(-1L));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void splitSessionByTimeReturnsMinusOneIfServerConfigurationIsNotSet() {
        // given
        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        // when
        long obtained = target.splitSessionByTime();

        // then
        assertThat(obtained, is(-1L));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void splitByTimeDoesNotPerformSplitIfNeitherSplitByIdleTimeoutNorSplitByDurationEnabled() {
        // given
        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        long obtained = target.splitSessionByTime();

        // then
        assertThat(obtained, is(-1L));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void splitByTimeSplitsCurrentSessionIfIdleTimeoutReached() {
        // given
        long lastInteractionTimeSessionOne = 60;
        int idleTimeout = 10;                       // time to split: last interaction + idle => 70
        long currentTime = 70;
        long sessionTwoCreationTime = 80;
        when(mockTimingProvider.provideTimestampInMilliseconds())
                .thenReturn(lastInteractionTimeSessionOne, currentTime);
        when(mockSplitBeacon1.getSessionStartTime()).thenReturn(sessionTwoCreationTime);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        target.identifyUser("test"); // update last interaction time
        assertThat(target.getLastInteractionTime(), is(lastInteractionTimeSessionOne));

        // when
        long obtained = target.splitSessionByTime();

        // then
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, idleTimeout / 2);
        verify(mockSessionCreator, times(1)).reset();
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(sessionTwoCreationTime + idleTimeout));
    }

    @Test
    public void splitByTimeSplitsCurrentSessionIfIdleTimeoutExceeded() {
        // given
        long lastInteractionTimeSessionOne = 60;
        int idleTimeout = 10;                       // time to split: last interaction + idle => 70
        long currentTime = 80;
        long sessionTwoCreationTime = 90;
        when(mockTimingProvider.provideTimestampInMilliseconds())
                .thenReturn(lastInteractionTimeSessionOne, currentTime);
        when(mockSplitBeacon1.getSessionStartTime()).thenReturn(sessionTwoCreationTime);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        target.identifyUser("test"); // update last interaction time
        assertThat(target.getLastInteractionTime(), is(lastInteractionTimeSessionOne));

        // when
        long obtained = target.splitSessionByTime();

        // then
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, idleTimeout / 2);
        verify(mockSessionCreator, times(1)).reset();
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(sessionTwoCreationTime + idleTimeout));
    }

    @Test
    public void splitByTimeDoesNotSplitCurrentSessionIfIdleTimeoutNotExpired() {
        // given
        long lastInteractionTime = 60;
        int idleTimeout = 20;               // time to split: list interaction + idle => 80
        long currentTime = 70;
        when(mockTimingProvider.provideTimestampInMilliseconds())
                .thenReturn(lastInteractionTime, currentTime);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        target.identifyUser("test"); // update last interaction time
        assertThat(target.getLastInteractionTime(), is(lastInteractionTime));

        // when
        long obtained = target.splitSessionByTime();

        // then
        assertThat(obtained, is(lastInteractionTime + idleTimeout));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void splitByTimeSplitsCurrentSessionIfMaxDurationReached() {
        // given
        long startTimeFirstSession = 60;
        int sessionDuration = 10;           // split time: session start + duration = 70
        int sendInterval = 15;
        long currentTime = 70;
        long startTimeSecondSession = 80;

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(currentTime);
        when(mockBeacon.getSessionStartTime()).thenReturn(startTimeFirstSession);
        when(mockSplitBeacon1.getSessionStartTime()).thenReturn(startTimeSecondSession);

        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        when(mockServerConfiguration.getSendIntervalInMilliseconds()).thenReturn(sendInterval);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        long obtained = target.splitSessionByTime();

        // then
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sendInterval);
        verify(mockSessionCreator, times(1)).reset();
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(startTimeSecondSession + sessionDuration));
    }

    @Test
    public void splitByTimeSplitsCurrentSessionIfMaxDurationExceeded() {
        // given
        long startTimeFirstSession = 60;
        int sessionDuration = 10;           // split time: session start + duration => 70
        int sendInterval = 15;
        long currentTime = 80;
        long startTimeSecondSession = 90;

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(currentTime);
        when(mockBeacon.getSessionStartTime()).thenReturn(startTimeFirstSession);
        when(mockSplitBeacon1.getSessionStartTime()).thenReturn(startTimeSecondSession);

        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        when(mockServerConfiguration.getSendIntervalInMilliseconds()).thenReturn(sendInterval);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        long obtained = target.splitSessionByTime();

        // then
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sendInterval);
        verify(mockSessionCreator, times(1)).reset();
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(startTimeSecondSession + sessionDuration));
    }

    @Test
    public void splitByTimeDoesNotSplitCurrentSessionIfMaxDurationNotReached() {
        // given
        long sessionStartTime = 60;
        int sessionDuration = 20;       // split time: start time + duration => 80
        long currentTime = 70;

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(currentTime);
        when(mockBeacon.getSessionStartTime()).thenReturn(sessionStartTime);

        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);


        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        long obtained = target.splitSessionByTime();

        // then
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(sessionStartTime + sessionDuration));
    }

    @Test
    public void splitBySessionTimeReturnsIdleSplitTimeWhenBeforeSessionDurationSplitTime() {
        // given
        long sessionStartTime = 50;
        int sessionDuration = 40;       // duration split time: start time + duration => 90
        long lastInteractionTime = 60;
        int idleTimeout = 20;           // idle split time: last interaction + idle => 80
        long currentTime = 70;

        when(mockBeacon.getSessionStartTime()).thenReturn(sessionStartTime);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(lastInteractionTime, currentTime);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        target.identifyUser("test"); // update last interaction time

        // when
        long obtained = target.splitSessionByTime();

        // then
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(lastInteractionTime + idleTimeout));
    }

    @Test
    public void splitBySessionTimeReturnsDurationSplitTimeWhenBeforeIdleSplitTime() {
        // given
        long sessionStartTime = 50;
        int sessionDuration = 30;       // duration split time: start time + duration => 80
        long lastInteractionTime = 60;
        int idleTimeout = 50;           // idle split time: last interaction + idle => 110
        long currentTime = 70;

        when(mockBeacon.getSessionStartTime()).thenReturn(sessionStartTime);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(lastInteractionTime, currentTime);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        target.identifyUser("test"); // update last interaction time

        // when
        long obtained = target.splitSessionByTime();

        // then
        verifyNoMoreInteractions(mockSessionCreator);
        assertThat(obtained, is(sessionStartTime + sessionDuration));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// identifyUser on split sessions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void splitByTimeReAppliesUserIdentificationTag() {
        // given
        long lastInteractionTimeSessionOne = 60;
        int idleTimeout = 10;                       // time to split: last interaction + idle => 70
        long currentTime = 80;
        long sessionTwoCreationTime = 90;
        when(mockTimingProvider.provideTimestampInMilliseconds())
            .thenReturn(lastInteractionTimeSessionOne, currentTime);
        when(mockSplitBeacon1.getSessionStartTime()).thenReturn(sessionTwoCreationTime);

        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfiguration.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        target.onServerConfigurationUpdate(mockServerConfiguration);

        target.identifyUser("test"); // update last interaction time
        assertThat(target.getLastInteractionTime(), is(lastInteractionTimeSessionOne));

        // when
        target.splitSessionByTime();

        // then
        verify(mockSplitSession1, times(1)).identifyUser("test");
    }

    @Test
    public void splitByEventCountReAppliesUserIdentificationTag() {
        // given
        int maxEventCount = 1;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);
        when(mockServerConfiguration.merge(any(ServerConfiguration.class))).thenCallRealMethod();

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        ServerConfiguration ignoredServerConfig = mock(ServerConfiguration.class);
        when(ignoredServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(ignoredServerConfig.getMaxEventsPerSession()).thenReturn(5);

        target.onServerConfigurationUpdate(ignoredServerConfig);

        // when
        target.identifyUser("test1");
        target.enterAction("action 1");
        target.enterAction("action 2");

        // then
        verify(mockSplitSession1, times(1)).identifyUser("test1");

        // and when
        target.enterAction("action 3");

        // then
        verify(mockSplitSession2, times(1)).identifyUser("test1");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// further tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void onChildClosedRemovesChildFromList() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getCopyOfChildObjects().size(), is(1)); // initial session

        // when
        OpenKitObject childObject = mock(OpenKitObject.class);
        target.storeChildInList(childObject);

        // then
        assertThat(target.getCopyOfChildObjects().size(), is(2));

        // when child gets closed
        target.onChildClosed(childObject);

        // then
        assertThat(target.getCopyOfChildObjects().size(), is(1));
    }

    @Test
    public void onChildClosedCallsDequeueOnSessionWatchdog() {
        // given
        SessionProxyImpl target = createSessionProxy();
        SessionImpl session = mock(SessionImpl.class);
        target.storeChildInList(session);

        // when
        target.onChildClosed(session);

        // then
        verify(mockSessionWatchdog, times(1)).dequeueFromClosing(session);
    }

    @Test
    public void onServerConfigurationUpdateTakesOverServerConfigurationOnFirstCall() {
        // given
        SessionProxyImpl target = createSessionProxy();

        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        assertThat(target.getServerConfiguration(), is(nullValue()));

        // when
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // then
        assertThat(target.getServerConfiguration(), is(mockServerConfiguration));
    }

    @Test
    public void onServerConfigurationUpdateMergesServerConfigurationOnConsecutiveCalls() {
        // given
        SessionProxyImpl target = createSessionProxy();

        ServerConfiguration mockFirstConfig = mock(ServerConfiguration.class);
        ServerConfiguration mockSecondConfig = mock(ServerConfiguration.class);

        // when
        target.onServerConfigurationUpdate(mockFirstConfig);

        // then
        verify(mockFirstConfig, times(0)).merge(any(ServerConfiguration.class));
        verify(mockFirstConfig, times(1)).isSessionSplitBySessionDurationEnabled();
        verify(mockFirstConfig, times(1)).isSessionSplitByIdleTimeoutEnabled();

        // and when
        target.onServerConfigurationUpdate(mockSecondConfig);

        // then
        verify(mockFirstConfig, times(1)).merge(mockSecondConfig);
        verifyNoMoreInteractions(mockFirstConfig);
        verifyZeroInteractions(mockSecondConfig);
    }

    @Test
    public void onServerConfigurationUpdateAddsSessionProxyToWatchdogIfSplitByDurationEnabled() {
        // given
        SessionProxyImpl target = createSessionProxy();
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(true);

        // when
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // then
        verify(mockSessionWatchdog, times(1)).addToSplitByTimeout(target);
    }

    @Test
    public void onServerConfigurationUpdateAddsSessionProxyToWatchdogIfSplitByIdleTimeoutEnabled() {
        // given
        SessionProxyImpl target = createSessionProxy();
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);

        // when
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // then
        verify(mockSessionWatchdog, times(1)).addToSplitByTimeout(target);
    }

    @Test
    public void onServerConfigurationUpdateDoesNotAddSessionProxyToWatchdogIfSplitByIdleTimeoutAndDurationDisabled() {
        // given
        SessionProxyImpl target = createSessionProxy();
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        // when
        target.onServerConfigurationUpdate(mockServerConfiguration);

        // then
        verifyZeroInteractions(mockSessionWatchdog);
    }

    @Test
    public void onServerConfigurationUpdateDoesNotAddSessionProxyToWatchdogOnConsecutiveCalls() {
        // given
        SessionProxyImpl target = createSessionProxy();
        when(mockServerConfiguration.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        when(mockServerConfiguration.isSessionSplitBySessionDurationEnabled()).thenReturn(false);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        ServerConfiguration mockServerConfigTwo = mock(ServerConfiguration.class);
        when(mockServerConfigTwo.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        when(mockServerConfigTwo.isSessionSplitBySessionDurationEnabled()).thenReturn(true);

        // when
        target.onServerConfigurationUpdate(mockServerConfigTwo);

        // then
        verifyZeroInteractions(mockSessionWatchdog);
    }

    @Test
    public void toStringReturnsAppropriateResult() {
        // given
        when(mockBeacon.getSessionNumber()).thenReturn(37);
        when(mockBeacon.getSessionSequenceNumber()).thenReturn(73);
        SessionProxyImpl target = createSessionProxy();

        // when
        String obtained = target.toString();

        // then
        assertThat(obtained, is(equalTo("SessionProxyImpl [sn=37, seq=73]")));
    }


    private SessionProxyImpl createSessionProxy() {
        return new SessionProxyImpl(
                mockLogger,
                mockParent,
                mockSessionCreator,
                mockTimingProvider,
                mockBeaconSender,
                mockSessionWatchdog
        );
    }
}
