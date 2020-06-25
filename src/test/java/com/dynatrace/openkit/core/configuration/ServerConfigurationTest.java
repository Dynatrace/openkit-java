/**
 * Copyright 2018-2020 Dynatrace LLC
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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.protocol.ResponseAttribute;
import com.dynatrace.openkit.protocol.ResponseAttributes;
import com.dynatrace.openkit.protocol.ResponseAttributesDefaults;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServerConfigurationTest {

    private final ResponseAttributesDefaults DEFAULT_VALUES = ResponseAttributesDefaults.UNDEFINED;
    private ResponseAttributes responseAttributes;

    private ServerConfiguration mockServerConfig;

    @Before
    public void setUp() {
        responseAttributes = mock(ResponseAttributes.class);
        when(responseAttributes.isCapture()).thenReturn(DEFAULT_VALUES.isCapture());
        when(responseAttributes.isCaptureCrashes()).thenReturn(DEFAULT_VALUES.isCaptureCrashes());
        when(responseAttributes.isCaptureErrors()).thenReturn(DEFAULT_VALUES.isCaptureErrors());
        when(responseAttributes.getSendIntervalInMilliseconds()).thenReturn(DEFAULT_VALUES.getSendIntervalInMilliseconds());
        when(responseAttributes.getServerId()).thenReturn(DEFAULT_VALUES.getServerId());
        when(responseAttributes.getMaxBeaconSizeInBytes()).thenReturn(DEFAULT_VALUES.getMaxBeaconSizeInBytes());
        when(responseAttributes.getMultiplicity()).thenReturn(DEFAULT_VALUES.getMultiplicity());
        when(responseAttributes.getSendIntervalInMilliseconds()).thenReturn(DEFAULT_VALUES.getSendIntervalInMilliseconds());
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(DEFAULT_VALUES.getMaxSessionDurationInMilliseconds());
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(DEFAULT_VALUES.getMaxEventsPerSession());
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(DEFAULT_VALUES.getSessionTimeoutInMilliseconds());
        when(responseAttributes.getVisitStoreVersion()).thenReturn(DEFAULT_VALUES.getVisitStoreVersion());

        mockServerConfig = mock(ServerConfiguration.class);
        when(mockServerConfig.isCaptureEnabled()).thenReturn(DEFAULT_VALUES.isCapture());
        when(mockServerConfig.isCrashReportingEnabled()).thenReturn(DEFAULT_VALUES.isCaptureCrashes());
        when(mockServerConfig.isErrorReportingEnabled()).thenReturn(DEFAULT_VALUES.isCaptureErrors());
        when(mockServerConfig.getServerID()).thenReturn(DEFAULT_VALUES.getServerId());
        when(mockServerConfig.getBeaconSizeInBytes()).thenReturn(DEFAULT_VALUES.getMaxBeaconSizeInBytes());
        when(mockServerConfig.getMultiplicity()).thenReturn(DEFAULT_VALUES.getMultiplicity());
        when(mockServerConfig.getSendIntervalInMilliseconds()).thenReturn(DEFAULT_VALUES.getSendIntervalInMilliseconds());
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(DEFAULT_VALUES.getMaxSessionDurationInMilliseconds());
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(DEFAULT_VALUES.getMaxEventsPerSession());
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(DEFAULT_VALUES.getSessionTimeoutInMilliseconds());
        when(mockServerConfig.getVisitStoreVersion()).thenReturn(DEFAULT_VALUES.getVisitStoreVersion());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Default tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void inDefaultServerConfigurationCapturingIsEnabled() {
        // then
        assertThat(ServerConfiguration.DEFAULT.isCaptureEnabled(), is(true));
    }

    @Test
    public void inDefaultServerConfigurationCrashReportingIsEnabled() {
        // then
        assertThat(ServerConfiguration.DEFAULT.isCrashReportingEnabled(), is(true));
    }

    @Test
    public void inDefaultServerConfigurationErrorReportingIsEnabled() {
        // then
        assertThat(ServerConfiguration.DEFAULT.isErrorReportingEnabled(), is(true));
    }

    @Test
    public void inDefaultServerConfigurationServerIDIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getServerID(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationBeaconSizeIsThirtyKB() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getBeaconSizeInBytes(), is(30 * 1024));
    }

    @Test
    public void inDefaultServerConfigurationMultiplicityIsOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getMultiplicity(), is(1));
    }

    @Test
    public void inDefaultServerConfigurationSendIntervalIs120Seconds() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getSendIntervalInMilliseconds(), is(120 * 1000));
    }

    @Test
    public void inDefaultServerConfigurationMaxSessionDurationIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getMaxSessionDurationInMilliseconds(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationIsSessionSplitBySessionDurationEnabledIsFalse() {
        // then
        assertThat(ServerConfiguration.DEFAULT.isSessionSplitBySessionDurationEnabled(), is(false));
    }

    @Test
    public void inDefaultServerConfigurationMaxEventsPerSessionIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getMaxEventsPerSession(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationIsSessionSplitByEventsEnabledIsFalse() {
        // then
        assertThat(ServerConfiguration.DEFAULT.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void inDefaultServerConfigurationSessionTimeoutIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getSessionTimeoutInMilliseconds(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationIsSessionSplitByIdleTimeoutEnabledIsFalse() {
        // then
        assertThat(ServerConfiguration.DEFAULT.isSessionSplitByIdleTimeoutEnabled(), is(false));
    }

    @Test
    public void inDefaultServerConfigurationVisitStoreVersionIsOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getVisitStoreVersion(), is(1));
    }

    @Test
    public void creatingAServerConfigurationFromNullRequestAttributesGivesNull() {
        // when, then
        assertThat(ServerConfiguration.from(null), is(nullValue()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// creating server config from response attributes
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesCaptureSettings() {
        // given
        when(responseAttributes.isCapture()).thenReturn(false);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isCaptureEnabled(), is(false));

        verify(responseAttributes, times(1)).isCapture();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesCrashReportingSettings() {
        // given
        when(responseAttributes.isCaptureCrashes()).thenReturn(false);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isCrashReportingEnabled(), is(false));

        verify(responseAttributes, times(1)).isCaptureCrashes();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesErrorReportingSettings() {
        // given
        when(responseAttributes.isCaptureErrors()).thenReturn(false);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isErrorReportingEnabled(), is(false));

        verify(responseAttributes, times(1)).isCaptureErrors();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesServerIDSettings() {
        // given
        when(responseAttributes.getServerId()).thenReturn(42);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getServerID(), is(42));

        verify(responseAttributes, times(1)).getServerId();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesBeaconSizeSettings() {
        // given
        when(responseAttributes.getMaxBeaconSizeInBytes()).thenReturn(100 * 1024);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getBeaconSizeInBytes(), is(100 * 1024));

        verify(responseAttributes, times(1)).getMaxBeaconSizeInBytes();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesMultiplicitySettings() {
        // given
        when(responseAttributes.getMultiplicity()).thenReturn(7);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getMultiplicity(), is(7));

        verify(responseAttributes, times(1)).getMultiplicity();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesSendInterval() {
        // given
        int sendInterval = 1234;
        when(responseAttributes.getSendIntervalInMilliseconds()).thenReturn(sendInterval);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getSendIntervalInMilliseconds(), is(sendInterval));
        verify(responseAttributes, times(1)).getSendIntervalInMilliseconds();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesSessionDuration() {
        // given
        int sessionDuration = 73;
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
        verify(responseAttributes, times(1)).getMaxSessionDurationInMilliseconds();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesHasSplitBySessionDurationEnabledIfMaxSessionDurationGreaterZero() {
        // given
        int sessionDuration = 1;
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(true));
        verify(responseAttributes, times(1)).getMaxSessionDurationInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitBySessionDurationDisabledIfMaxDurationZero() {
        // given
        int sessionDuration = 0;
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));
        verify(responseAttributes, times(1)).getMaxSessionDurationInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitBySessionDurationDisabledIfMaxDurationEventsSmallerZero() {
        // given
        int sessionDuration = -1;
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));
        verify(responseAttributes, times(1)).getMaxSessionDurationInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitBySessionDurationDisabledIfMaxDurationIsNotSet() {
        // given
        int sessionDuration = 1;
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(false);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));
        verify(responseAttributes, times(1)).getMaxSessionDurationInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION);
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesMaxEventsPerSession() {
        // given
        int eventsPerSession = 37;
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getMaxEventsPerSession(), is(eventsPerSession));
        verify(responseAttributes, times(1)).getMaxEventsPerSession();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesHasSplitByEventsEnabledIfMaxEventsGreaterZero() {
        // given
        int eventsPerSession = 1;
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(true));
        verify(responseAttributes, times(1)).getMaxEventsPerSession();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitByEventsDisabledIfMaxEventsZero() {
        // given
        int eventsPerSession = 0;
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
        verify(responseAttributes, times(1)).getMaxEventsPerSession();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitByEventsDisabledIfMaxEventsEventsSmallerZero() {
        // given
        int eventsPerSession = -1;
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
        verify(responseAttributes, times(1)).getMaxEventsPerSession();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitByEventsDisabledIfMaxEventsIsNotSet() {
        // given
        int eventsPerSession = 1;
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(false);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
        verify(responseAttributes, times(1)).getMaxEventsPerSession();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION);
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesCopiesSessionTimeout() {
        // given
        int sessionTimeout = 42;
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(sessionTimeout);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
        verify(responseAttributes, times(1)).getSessionTimeoutInMilliseconds();
    }

    @Test
    public void creatingAServerConfigurationFromResponseAttributesHasSplitByIdleTimeoutEnabledIfTimeoutGreaterZero() {
        // given
        int idleTimeout = 1;
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(responseAttributes.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(true));
        verify(responseAttributes, times(1)).getSessionTimeoutInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.SESSION_TIMEOUT);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitByIdleTimeoutDisabledIfTimeoutZero() {
        // given
        int idleTimeout = 0;
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(responseAttributes.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));
        verify(responseAttributes, times(1)).getSessionTimeoutInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.SESSION_TIMEOUT);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitByIdleTimeoutDisabledIfTimeoutSmallerZero() {
        // given
        int idleTimeout = -1;
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(responseAttributes.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT)).thenReturn(true);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));
        verify(responseAttributes, times(1)).getSessionTimeoutInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.SESSION_TIMEOUT);
    }

    @Test
    public void creatingAServerConfigurationFromRequestAttributesHasSplitByIdleTimeoutDisabledIfTimeoutIsNotSet() {
        // given
        int idleTimeout = 1;
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(responseAttributes.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT)).thenReturn(false);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));
        verify(responseAttributes, times(1)).getSessionTimeoutInMilliseconds();
        verify(responseAttributes, times(1)).isAttributeSet(ResponseAttribute.SESSION_TIMEOUT);
    }

    @Test
    public void creatingASessionConfigurationFromResponseAttributesCopiesVisitStoreVersion() {
        // given
        int visitStoreVersion = 73;
        when(responseAttributes.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getVisitStoreVersion(), is(visitStoreVersion));
        verify(responseAttributes, times(1)).getVisitStoreVersion();
    }

    @Test
    public void sendingDataToTheServerIsAllowedIfCapturingIsEnabledAndMultiplicityIsGreaterThanZero() {
        // given
        when(responseAttributes.isCapture()).thenReturn(true);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingDataAllowed();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void sendingDataToTheServerIsNotAllowedIfCapturingIsDisabled() {
        // given
        when(responseAttributes.isCapture()).thenReturn(false);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingDataAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void sendingDataToTheServerIsNotAllowedIfCapturingIsEnabledButMultiplicityIsZero() {
        // given
        when(responseAttributes.isCapture()).thenReturn(true);
        when(responseAttributes.getMultiplicity()).thenReturn(0);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingDataAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void sendingCrashesToTheServerIsAllowedIfDataSendingIsAllowedAndCaptureCrashesIsEnabled() {
        // given
        when(responseAttributes.isCapture()).thenReturn(true);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        when(responseAttributes.isCaptureCrashes()).thenReturn(true);

        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingCrashesAllowed();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void sendingCrashesToTheServerIsNotAllowedIfDataSendingIsNotAllowed() {
        // given
        when(responseAttributes.isCapture()).thenReturn(false);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        when(responseAttributes.isCaptureCrashes()).thenReturn(true);

        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingCrashesAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void sendingCrashesToTheServerIsNotAllowedIfDataSendingIsAllowedButCaptureCrashesIsDisabled() {
        // given
        when(responseAttributes.isCapture()).thenReturn(true);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        when(responseAttributes.isCaptureCrashes()).thenReturn(false);

        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingCrashesAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void sendingErrorToTheServerIsAllowedIfDataSendingIsAllowedAndCaptureErrorIsEnabled() {
        // given
        when(responseAttributes.isCapture()).thenReturn(true);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        when(responseAttributes.isCaptureErrors()).thenReturn(true);

        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingErrorsAllowed();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void sendingErrorToTheServerIsNotAllowedIfDataSendingIsNotAllowed() {
        // given
        when(responseAttributes.isCapture()).thenReturn(false);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        when(responseAttributes.isCaptureErrors()).thenReturn(true);

        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingErrorsAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void sendingErrorsToTheServerIsNotAllowedIfDataSendingIsAllowedButCaptureErrorsDisabled() {
        // given
        when(responseAttributes.isCapture()).thenReturn(true);
        when(responseAttributes.getMultiplicity()).thenReturn(1);
        when(responseAttributes.isCaptureErrors()).thenReturn(false);

        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // when
        boolean obtained = target.isSendingErrorsAllowed();

        // then
        assertThat(obtained, is(false));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// creating builder from server config
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void builderFromServerConfigCopiesCaptureSettings() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(false);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isCaptureEnabled(), is(false));
        verify(mockServerConfig, times(1)).isCaptureEnabled();
    }

    @Test
    public void builderFromServerConfigCopiesCrashReportingSettings() {
        // given
        when(mockServerConfig.isCrashReportingEnabled()).thenReturn(false);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isCrashReportingEnabled(), is(false));
        verify(mockServerConfig, times(1)).isCrashReportingEnabled();
    }

    @Test
    public void builderFromServerConfigCopiesErrorReportingSettings() {
        // given
        when(mockServerConfig.isErrorReportingEnabled()).thenReturn(false);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isErrorReportingEnabled(), is(false));
        verify(mockServerConfig, times(1)).isErrorReportingEnabled();
    }

    @Test
    public void builderFromServerConfigCopiesServerIDSettings() {
        // given
        when(mockServerConfig.getServerID()).thenReturn(42);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getServerID(), is(42));
        verify(mockServerConfig, times(1)).getServerID();
    }

    @Test
    public void builderFromServerConfigCopiesBeaconSizeSettings() {
        // given
        when(mockServerConfig.getBeaconSizeInBytes()).thenReturn(100 * 1024);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getBeaconSizeInBytes(), is(100 * 1024));
        verify(mockServerConfig, times(1)).getBeaconSizeInBytes();
    }

    @Test
    public void builderFromServerConfigCopiesMultiplicitySettings() {
        // given
        when(mockServerConfig.getMultiplicity()).thenReturn(7);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getMultiplicity(), is(7));
        verify(mockServerConfig, times(1)).getMultiplicity();
    }

    @Test
    public void builderFromServerConfigCopiesSendInterval() {
        // given
        int sendInterval = 4321;
        when(mockServerConfig.getSendIntervalInMilliseconds()).thenReturn(sendInterval);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getSendIntervalInMilliseconds(), is(sendInterval));
        verify(mockServerConfig, times(1)).getSendIntervalInMilliseconds();
    }

    @Test
    public void builderFromServerConfigCopiesSessionDuration() {
        // given
        int sessionDuration = 73;
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
        verify(mockServerConfig, times(1)).getMaxSessionDurationInMilliseconds();
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDurationEnabledIfMaxEventsGreaterZero() {
        // given
        int sessionDuration = 1;
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfig.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(true));
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDurationDisabledIfMaxEventsZero() {
        // given
        int sessionDuration = 0;
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfig.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDurationDisabledIfMaxEventsEventsSmallerZero() {
        // given
        int sessionDuration = -1;
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfig.isSessionSplitBySessionDurationEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDurationDisabledIfMaxEventsIsNotSet() {
        // given
        int sessionDuration = 1;
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        when(mockServerConfig.isSessionSplitBySessionDurationEnabled()).thenReturn(false);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigCopiesMaxEventsPerSession() {
        // given
        int eventsPerSession = 37;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getMaxEventsPerSession(), is(eventsPerSession));
        verify(mockServerConfig, times(1)).getMaxEventsPerSession();
    }

    @Test
    public void builderFromServerConfigHasSplitByEventsEnabledIfMaxEventsGreaterZero() {
        // given
        int eventsPerSession = 1;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(true));
    }

    @Test
    public void builderFromServerConfigHasSplitByEventsDisabledIfMaxEventsZero() {
        // given
        int eventsPerSession = 0;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitByEventsDisabledIfMaxEventsEventsSmallerZero() {
        // given
        int eventsPerSession = -1;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitByEventsDisabledIfMaxEventsIsNotSet() {
        // given
        int eventsPerSession = 1;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(false);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigCopiesSessionTimeout() {
        // given
        int sessionTimeout = 42;
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(sessionTimeout);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
        verify(mockServerConfig, times(1)).getSessionTimeoutInMilliseconds();
    }

    @Test
    public void builderFromServerConfigHasSplitByIdleTimeoutEnabledIfMaxEventsGreaterZero() {
        // given
        int idleTimeout = 1;
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfig.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(true));
    }

    @Test
    public void builderFromServerConfigHasSplitByIdleTimeoutDisabledIfMaxEventsZero() {
        // given
        int idleTimeout = 0;
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfig.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitByIdleTimeoutDisabledIfMaxEventsEventsSmallerZero() {
        // given
        int idleTimeout = -1;
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfig.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitByIdleTimeoutDisabledIfMaxEventsIsNotSet() {
        // given
        int idleTimeout = 1;
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        when(mockServerConfig.isSessionSplitByIdleTimeoutEnabled()).thenReturn(false);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigCopiesVisitStoreVersion() {
        // given
        int visitStoreVersion = 73;
        when(mockServerConfig.getVisitStoreVersion()).thenReturn(visitStoreVersion);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getVisitStoreVersion(), is(visitStoreVersion));
        verify(mockServerConfig, times(1)).getVisitStoreVersion();
    }

    @Test
    public void builderFromServerConfigSendingDataToTheServerIsAllowedIfCapturingIsEnabledAndMultiplicityIsGreaterThanZero() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingDataAllowed();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void builderFromServerConfigSendingDataToTheServerIsNotAllowedIfCapturingIsDisabled() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(false);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingDataAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void builderFromServerConfigSendingDataToTheServerIsNotAllowedIfCapturingIsEnabledButMultiplicityIsZero() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfig.getMultiplicity()).thenReturn(0);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingDataAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void builderFromServerConfigSendingCrashesToTheServerIsAllowedIfDataSendingIsAllowedAndCaptureCrashesIsEnabled() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        when(mockServerConfig.isCrashReportingEnabled()).thenReturn(true);

        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingCrashesAllowed();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void builderFromServerConfigSendingCrashesToTheServerIsNotAllowedIfDataSendingIsNotAllowed() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(false);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        when(mockServerConfig.isCrashReportingEnabled()).thenReturn(true);

        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingCrashesAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void builderFromServerConfigSendingCrashesToTheServerIsNotAllowedIfDataSendingIsAllowedButCaptureCrashesIsDisabled() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        when(mockServerConfig.isCrashReportingEnabled()).thenReturn(false);

        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingCrashesAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void builderFromServerConfigSendingErrorToTheServerIsAllowedIfDataSendingIsAllowedAndCaptureErrorIsEnabled() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        when(mockServerConfig.isErrorReportingEnabled()).thenReturn(true);

        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingErrorsAllowed();

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void builderFromServerConfigSendingErrorToTheServerIsNotAllowedIfDataSendingIsNotAllowed() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(false);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        when(mockServerConfig.isErrorReportingEnabled()).thenReturn(true);

        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingErrorsAllowed();

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void builderFromServerConfigSendingErrorsToTheServerIsNotAllowedIfDataSendingIsAllowedButCaptureErrorsDisabled() {
        // given
        when(mockServerConfig.isCaptureEnabled()).thenReturn(true);
        when(mockServerConfig.getMultiplicity()).thenReturn(1);
        when(mockServerConfig.isErrorReportingEnabled()).thenReturn(false);

        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // when
        boolean obtained = target.isSendingErrorsAllowed();

        // then
        assertThat(obtained, is(false));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// merge tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void mergeTakesOverEnabledCapture() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withCapture(false).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withCapture(true).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCaptureEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverDisabledCapture() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withCapture(true).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withCapture(false).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCaptureEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverEnabledCrashReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withCrashReporting(false).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withCrashReporting(true).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCrashReportingEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverDisabledCrashReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withCrashReporting(true).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withCrashReporting(false).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCrashReportingEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverEnabledErrorReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withErrorReporting(false).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withErrorReporting(true).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isErrorReportingEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverDisabledErrorReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withErrorReporting(true).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withErrorReporting(false).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isErrorReportingEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverBeaconSize() {
        // given
        int beaconSize = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withBeaconSizeInBytes(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withBeaconSizeInBytes(beaconSize).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeIgnoresMultiplicity() {
        // given
        int multiplicity = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withMultiplicity(multiplicity).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withMultiplicity(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeIgnoresServerId() {
        // given
        int serverId = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES).withServerID(serverId).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).withServerID(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getServerID(), is(serverId));
    }

    @Test
    public void mergeKeepsOriginalMaxSessionDuration() {
        // given
        int sessionDuration = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMaxSessionDurationInMilliseconds(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeKeepsIsSessionSplitBySessionDurationEnabledWhenMaxEventsIsGreaterZeroAndAttributeIsSet() {
        // given
        int sessionDuration = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(true);
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).build();

        assertThat(other.isSessionSplitBySessionDurationEnabled(), is(false));
        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(true));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitBySessionDurationEnabled(), is(true));
    }

    @Test
    public void mergeKeepsIsSessionSplitBySessionDurationEnabledWhenMaxEventsIsSmallerZeroButAttributeIsSet() {
        // given
        int sessionDuration = 0;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(true);
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = mock(ServerConfiguration.class);
        when(other.isSessionSplitBySessionDurationEnabled()).thenReturn(true);

        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitBySessionDurationEnabled(), is(false));
    }

    @Test
    public void mergeKeepsIsSessionSplitBySessionDurationEnabledWhenMaxEventsIsGreaterZeroButAttributeIsNotSet() {
        // given
        int sessionDuration = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(false);
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(sessionDuration);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = mock(ServerConfiguration.class);
        when(other.isSessionSplitBySessionDurationEnabled()).thenReturn(true);

        assertThat(target.isSessionSplitBySessionDurationEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitBySessionDurationEnabled(), is(false));
    }

    @Test
    public void mergeKeepsOriginalMaxEventsPerSession() {
        // given
        int eventsPerSession = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMaxEventsPerSession(eventsPerSession).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMaxEventsPerSession(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeKeepsIsSessionSplitByEventsEnabledWhenMaxEventsIsGreaterZeroAndAttributeIsSet() {
        // given
        int eventsPerSession = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).build();

        assertThat(other.isSessionSplitByEventsEnabled(), is(false));
        assertThat(target.isSessionSplitByEventsEnabled(), is(true));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByEventsEnabled(), is(true));
    }

    @Test
    public void mergeKeepsIsSessionSplitByEventsEnabledWhenMaxEventsIsSmallerZeroButAttributeIsSet() {
        // given
        int eventsPerSession = 0;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = mock(ServerConfiguration.class);
        when(other.isSessionSplitByEventsEnabled()).thenReturn(true);

        assertThat(target.isSessionSplitByEventsEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void mergeKeepsIsSessionSplitByEventsEnabledWhenMaxEventsIsGreaterZeroButAttributeIsNotSet() {
        // given
        int eventsPerSession = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(false);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = mock(ServerConfiguration.class);
        when(other.isSessionSplitByEventsEnabled()).thenReturn(true);

        assertThat(target.isSessionSplitByEventsEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void mergeKeepsOriginalSessionTimeout() {
        // given
        int sessionTimeout = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withSessionTimeoutInMilliseconds(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeKeepsIsSessionSplitByIdleTimeoutEnabledWhenMaxEventsIsGreaterZeroAndAttributeIsSet() {
        // given
        int idleTimeout = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT)).thenReturn(true);
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES).build();

        assertThat(other.isSessionSplitByIdleTimeoutEnabled(), is(false));
        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(true));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByIdleTimeoutEnabled(), is(true));
    }

    @Test
    public void mergeKeepsIsSessionSplitByIdleTimeoutEnabledWhenMaxEventsIsSmallerZeroButAttributeIsSet() {
        // given
        int idleTimeout = 0;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(true);
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = mock(ServerConfiguration.class);
        when(other.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);

        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByIdleTimeoutEnabled(), is(false));
    }

    @Test
    public void mergeKeepsIsSessionSplitByIdleTimeoutEnabledWhenMaxEventsIsGreaterZeroButAttributeIsNotSet() {
        // given
        int idleTimeout = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)).thenReturn(false);
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(idleTimeout);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);
        ServerConfiguration other = mock(ServerConfiguration.class);
        when(other.isSessionSplitByIdleTimeoutEnabled()).thenReturn(true);

        assertThat(target.isSessionSplitByIdleTimeoutEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByIdleTimeoutEnabled(), is(false));
    }

    @Test
    public void mergeKeepsOriginalVisitStoreVersion() {
        // given
        int visitStoreVersion = 73;
        ServerConfiguration target = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withVisitStoreVersion(visitStoreVersion).build();
        ServerConfiguration other = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withVisitStoreVersion(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// empty builder tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void buildPropagatesCaptureEnabledToInstance() {
        // given
        boolean capture = !DEFAULT_VALUES.isCapture();

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES).withCapture(capture).build();

        // then
        assertThat(obtained.isCaptureEnabled(), is(capture));
    }

    @Test
    public void buildPropagatesCrashReportingEnabledToInstance() {
        // given
        boolean crashReporting = !DEFAULT_VALUES.isCaptureCrashes();

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withCrashReporting(crashReporting).build();

        // then
        assertThat(obtained.isCrashReportingEnabled(), is(crashReporting));
    }

    @Test
    public void buildPropagatesErrorReportingEnabledToInstance() {
        // given
        boolean errorReporting = !DEFAULT_VALUES.isCaptureErrors();

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withErrorReporting(errorReporting).build();

        // then
        assertThat(obtained.isErrorReportingEnabled(), is(errorReporting));
    }

    @Test
    public void buildPropagatesServerIdToInstance() {
        // given
        int serverId = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withServerID(serverId).build();

        // then
        assertThat(obtained.getServerID(), is(serverId));
    }

    @Test
    public void buildPropagatesBeaconSizeToInstance() {
        // given
        int beaconSize = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withBeaconSizeInBytes(beaconSize).build();

        // then
        assertThat(obtained.getBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void buildPropagatesMultiplicityToInstance() {
        // given
        int multiplicity = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMultiplicity(multiplicity).build();

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void buildPropagatesSendIntervalToInstance() {
        // given
        int sendInterval = 777;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
            .withSendIntervalInMilliseconds(sendInterval).build();

        // then
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void buildPropagatesMaxSessionDurationToInstance() {
        // given
        int sessionDuration = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMaxSessionDurationInMilliseconds(sessionDuration)
                .build();

        // then
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void buildPropagatesMaxEventsPerSessionToInstance() {
        // given
        int eventsPerSession = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withMaxEventsPerSession(eventsPerSession)
                .build();

        // then
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void buildPropagatesSessionTimeoutToInstance() {
        // given
        int sessionTimeout = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withSessionTimeoutInMilliseconds(sessionTimeout)
                .build();

        // then
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void buildPropagatesVisitStoreVersionToInstance() {
        // given
        int visitStoreVersion = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder(DEFAULT_VALUES)
                .withVisitStoreVersion(visitStoreVersion)
                .build();

        // then
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }
}
