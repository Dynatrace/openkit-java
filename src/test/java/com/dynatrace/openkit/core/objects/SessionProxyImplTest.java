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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLConnection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    private ServerConfiguration mockServerConfiguration;
    private BeaconSender mockBeaconSender;
    private SessionWatchdog mockSessionWatchdog;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        when(mockLogger.isWarnEnabled()).thenReturn(true);

        mockParent = mock(OpenKitComposite.class);
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
        SessionProxyImpl target = createSessionProxy();

        // then
        verify(mockBeaconSender, times(1)).addSession(mockSession);
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
        assertThat(target.getTopLevelEventCount(), is(0));

        // when
        target.enterAction("test");

        // then
        assertThat(target.getTopLevelEventCount(), is(1));
    }

    @Test
    public void enterActionSetsLastInterActionTime() {
        // given
        long timestamp = 17;
        when(mockBeacon.getCurrentTimestamp()).thenReturn(timestamp);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(0L));

        // when
        target.enterAction("test");

        // then
        assertThat(target.getLastInteractionTime(), is(timestamp));
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
        assertThat(target.getTopLevelEventCount(), is(eventCount));
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
        assertThat(target.getTopLevelEventCount(), is(eventCount));
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
        int sessionDuration = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

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
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sessionDuration / 2);
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
    public void identifyUserIncreasesTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelEventCount(), is(0));

        // when
        target.identifyUser("Jane Doe");

        // then
        assertThat(target.getTopLevelEventCount(), is(1));
    }

    @Test
    public void identifyUserSetsLastInterActionTime() {
        // given
        long timestamp = 17;
        when(mockBeacon.getCurrentTimestamp()).thenReturn(timestamp);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(0L));

        // when
        target.identifyUser("Jane Doe");

        // then
        assertThat(target.getLastInteractionTime(), is(timestamp));
    }

    @Test
    public void identifyUserDoesNotSplitSessionIfNoServerConfigurationIsSet() {
        // given
        int eventCount = 10;

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.identifyUser("some user");
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void identifyUserDoesNotSplitSessionIfSessionSplitByEventDisabled() {
        // given
        int eventCount = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.identifyUser("some action");
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void identifyUserSplitsSessionIfSessionSplitByEventsEnabled() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.identifyUser("user 1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.identifyUser("user 2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void identifyUserSplitsSessionEveryNthEvent() {
        // given
        int maxEventCount = 3;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.identifyUser("user 1");
        target.identifyUser("user 2");
        target.identifyUser("user 3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.identifyUser("user 4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.identifyUser("user 5");
        target.identifyUser("user 6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.identifyUser("user 7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void identifyUserSplitsSessionEveryNthEventFromFirstServerConfiguration() {
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
        target.identifyUser("user1 1");
        target.identifyUser("user 2");
        target.identifyUser("user 3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.identifyUser("user 4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.identifyUser("user 5");
        target.identifyUser("user 6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.identifyUser("user 7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void identifyUserCallsWatchdogToCloseOldSessionOnSplitByEvents() {
        // given
        int sessionDuration = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.identifyUser("user 1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("user 2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sessionDuration / 2);
    }

    @Test
    public void identifyUserAddsSplitSessionToBeaconSenderOnSplitByEvents() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        verify(mockBeaconSender, times(1)).addSession(mockSession);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.enterAction("user 1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.enterAction("user2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockBeaconSender, times(1)).addSession(mockSplitSession1);
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
    public void reportCrashIncreasesTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelEventCount(), is(0));

        // when
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        assertThat(target.getTopLevelEventCount(), is(1));
    }

    @Test
    public void reportCrashSetsLastInterActionTime() {
        // given
        long timestamp = 17;
        when(mockBeacon.getCurrentTimestamp()).thenReturn(timestamp);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(0L));

        // when
        target.reportCrash("errorName", "reason", "stacktrace");

        // then
        assertThat(target.getLastInteractionTime(), is(timestamp));
    }

    @Test
    public void reportCrashUserDoesNotSplitSessionIfNoServerConfigurationIsSet() {
        // given
        int eventCount = 10;

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.reportCrash("error", "reason", "stacktrace");
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void reportCrashDoesNotSplitSessionIfSessionSplitByEventDisabled() {
        // given
        int eventCount = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.reportCrash("error", "reason", "stacktrace");
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void reportCrashSplitsSessionIfSessionSplitByEventsEnabled() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash("error 1", "reason 1", "stacktrace 1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 2", "reason 2", "stacktrace 2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void reportCrashSplitsSessionEveryNthEvent() {
        // given
        int maxEventCount = 3;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash("error 1", "reason 1", "stacktrace 1");
        target.reportCrash("error 2", "reason 2", "stacktrace 2");
        target.reportCrash("error 3", "reason 3", "stacktrace 3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 4", "reason 4", "stacktrace 4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.reportCrash("error 5", "reason 5", "stacktrace 5");
        target.reportCrash("error 6", "reason 6", "stacktrace 6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 7", "reason 7", "stacktrace 7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void reportCrashSplitsSessionEveryNthEventFromFirstServerConfiguration() {
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
        target.reportCrash("error 1", "reason 1", "stacktrace 1");
        target.reportCrash("error 2", "reason 2", "stacktrace 2");
        target.reportCrash("error 3", "reason 3", "stacktrace 3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 4", "reason 4", "stacktrace 4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.reportCrash("error 5", "reason 5", "stacktrace 5");
        target.reportCrash("error 6", "reason 6", "stacktrace 6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 7", "reason 7", "stacktrace 7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void reportCrashCallsWatchdogToCloseOldSessionOnSplitByEvents() {
        // given
        int sessionDuration = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash("error 1", "reason 1", "stacktrace 1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 2", "reason 2", "stacktrace 2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sessionDuration / 2);
    }

    @Test
    public void reportCrashAddsSplitSessionToBeaconSenderOnSplitByEvents() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        verify(mockBeaconSender, times(1)).addSession(mockSession);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.reportCrash("error 1", "reason 1", "stacktrace 1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.reportCrash("error 2", "reason 2", "stacktrace 2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockBeaconSender, times(1)).addSession(mockSplitSession1);
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
    public void traceWebRequestWithStringIncreasesTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelEventCount(), is(0));

        // when
        target.traceWebRequest("https://localhost");

        // then
        assertThat(target.getTopLevelEventCount(), is(1));
    }

    @Test
    public void traceWebRequestWithStringUrlSetsLastInterActionTime() {
        // given
        long timestamp = 17;
        when(mockBeacon.getCurrentTimestamp()).thenReturn(timestamp);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(0L));

        // when
        target.traceWebRequest("https://localhost");

        // then
        assertThat(target.getLastInteractionTime(), is(timestamp));
    }

    @Test
    public void traceWebRequestWithStringUrlDoesNotSplitSessionIfNoServerConfigurationIsSet() {
        // given
        int eventCount = 10;

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.traceWebRequest("https://localhost");
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithStringUrlDoesNotSplitSessionIfSessionSplitByEventDisabled() {
        // given
        int eventCount = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.traceWebRequest("https://localhost");
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithStringUrlSplitsSessionIfSessionSplitByEventsEnabled() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest("https://localhost/1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithStringUrlSplitsSessionEveryNthEvent() {
        // given
        int maxEventCount = 3;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest("https://localhost/1");
        target.traceWebRequest("https://localhost/2");
        target.traceWebRequest("https://localhost/3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.traceWebRequest("https://localhost/5");
        target.traceWebRequest("https://localhost/6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithStringUrlSplitsSessionEveryNthEventFromFirstServerConfiguration() {
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
        target.traceWebRequest("https://localhost/1");
        target.traceWebRequest("https://localhost/2");
        target.traceWebRequest("https://localhost/3");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/4");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.traceWebRequest("https://localhost/5");
        target.traceWebRequest("https://localhost/6");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/7");

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithStringUrlCallsWatchdogToCloseOldSessionOnSplitByEvents() {
        // given
        int sessionDuration = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest("https://localhost/1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sessionDuration / 2);
    }

    @Test
    public void traceWebRequestWithStringUrlAddsSplitSessionToBeaconSenderOnSplitByEvents() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        verify(mockBeaconSender, times(1)).addSession(mockSession);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest("https://localhost/1");

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest("https://localhost/2");

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockBeaconSender, times(1)).addSession(mockSplitSession1);
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
    public void traceWebRequestWithUrlConnectionIncreasesTopLevelEventCount() {
        // given
        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getTopLevelEventCount(), is(0));

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(target.getTopLevelEventCount(), is(1));
    }

    @Test
    public void traceWebRequestWithUrlConnectionSetsLastInterActionTime() {
        // given
        long timestamp = 17;
        when(mockBeacon.getCurrentTimestamp()).thenReturn(timestamp);

        SessionProxyImpl target = createSessionProxy();
        assertThat(target.getLastInteractionTime(), is(0L));

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        assertThat(target.getLastInteractionTime(), is(timestamp));
    }

    @Test
    public void traceWebRequestWithUrlConnectionDoesNotSplitSessionIfNoServerConfigurationIsSet() {
        // given
        int eventCount = 10;

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.traceWebRequest(mock(URLConnection.class));
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithUrlConnectionDoesNotSplitSessionIfSessionSplitByEventDisabled() {
        // given
        int eventCount = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        for (int i = 0; i < eventCount; i++) {
            target.traceWebRequest(mock(URLConnection.class));
        }

        // then
        assertThat(target.getTopLevelEventCount(), is(eventCount));
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithUrlConnectionSplitsSessionIfSessionSplitByEventsEnabled() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithUrlConnectionSplitsSessionEveryNthEvent() {
        // given
        int maxEventCount = 3;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(maxEventCount);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest(mock(URLConnection.class)); // 1
        target.traceWebRequest(mock(URLConnection.class)); // 2
        target.traceWebRequest(mock(URLConnection.class)); // 3

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class)); // 4

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.traceWebRequest(mock(URLConnection.class)); // 5
        target.traceWebRequest(mock(URLConnection.class)); // 6

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class)); // 7

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithUrlConnectionSplitsSessionEveryNthEventFromFirstServerConfiguration() {
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
        target.traceWebRequest(mock(URLConnection.class)); // 1
        target.traceWebRequest(mock(URLConnection.class)); // 2
        target.traceWebRequest(mock(URLConnection.class)); // 3

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class)); // 4

        // then
        verify(mockSessionCreator, times(2)).createSession(target);

        // and when
        target.traceWebRequest(mock(URLConnection.class)); // 5
        target.traceWebRequest(mock(URLConnection.class)); // 6

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class)); // 7

        // then
        verify(mockSessionCreator, times(3)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
    }

    @Test
    public void traceWebRequestWithUrlConnectionActionCallsWatchdogToCloseOldSessionOnSplitByEvents() {
        // given
        int sessionDuration = 10;
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);
        when(mockServerConfiguration.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockSessionWatchdog, times(1)).closeOrEnqueueForClosing(mockSession, sessionDuration / 2);
    }

    @Test
    public void traceWebRequestWithUrlConnectionAddsSplitSessionToBeaconSenderOnSplitByEvents() {
        // given
        when(mockServerConfiguration.isSessionSplitByEventsEnabled()).thenReturn(true);
        when(mockServerConfiguration.getMaxEventsPerSession()).thenReturn(1);

        SessionProxyImpl target = createSessionProxy();
        verify(mockSessionCreator, times(1)).createSession(target);
        verify(mockBeaconSender, times(1)).addSession(mockSession);

        target.onServerConfigurationUpdate(mockServerConfiguration);

        // when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        verifyNoMoreInteractions(mockSessionCreator);

        // and when
        target.traceWebRequest(mock(URLConnection.class));

        // then
        verify(mockSessionCreator, times(2)).createSession(target);
        verifyNoMoreInteractions(mockSessionCreator);
        verify(mockBeaconSender, times(1)).addSession(mockSplitSession1);
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
    public void closeSessionEndsTheSession() {
        // given
        SessionProxyImpl target = spy(createSessionProxy());

        // when
        target.close();

        // then
        verify(target, times(1)).end();
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
        return new SessionProxyImpl(mockLogger, mockParent, mockSessionCreator, mockBeaconSender, mockSessionWatchdog);
    }
}
