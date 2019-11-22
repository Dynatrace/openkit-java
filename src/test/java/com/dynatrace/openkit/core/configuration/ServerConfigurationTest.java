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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.protocol.ResponseAttributes;
import com.dynatrace.openkit.protocol.ResponseAttribute;
import com.dynatrace.openkit.protocol.StatusResponse;
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

    private ResponseAttributes responseAttributes;

    private ServerConfiguration mockServerConfig;

    @Before
    public void setUp() {
        responseAttributes = mock(ResponseAttributes.class);
        when(responseAttributes.isCapture()).thenReturn(ServerConfiguration.DEFAULT_CAPTURE_ENABLED);
        when(responseAttributes.isCaptureCrashes()).thenReturn(ServerConfiguration.DEFAULT_CRASH_REPORTING_ENABLED);
        when(responseAttributes.isCaptureErrors()).thenReturn(ServerConfiguration.DEFAULT_ERROR_REPORTING_ENABLED);
        when(responseAttributes.getSendIntervalInMilliseconds()).thenReturn(ServerConfiguration.DEFAULT_SEND_INTERVAL);
        when(responseAttributes.getServerId()).thenReturn(ServerConfiguration.DEFAULT_SERVER_ID);
        when(responseAttributes.getMaxBeaconSizeInBytes()).thenReturn(ServerConfiguration.DEFAULT_BEACON_SIZE);
        when(responseAttributes.getMultiplicity()).thenReturn(ServerConfiguration.DEFAULT_MULTIPLICITY);
        when(responseAttributes.getMaxSessionDurationInMilliseconds()).thenReturn(ServerConfiguration.DEFAULT_MAX_SESSION_DURATION);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(ServerConfiguration.DEFAULT_MAX_EVENTS_PER_SESSION);
        when(responseAttributes.getSessionTimeoutInMilliseconds()).thenReturn(ServerConfiguration.DEFAULT_SESSION_TIMEOUT);
        when(responseAttributes.getVisitStoreVersion()).thenReturn(ServerConfiguration.DEFAULT_VISIT_STORE_VERSION);

        mockServerConfig = mock(ServerConfiguration.class);
        when(mockServerConfig.isCaptureEnabled()).thenReturn(ServerConfiguration.DEFAULT_CAPTURE_ENABLED);
        when(mockServerConfig.isCrashReportingEnabled()).thenReturn(ServerConfiguration.DEFAULT_CRASH_REPORTING_ENABLED);
        when(mockServerConfig.isErrorReportingEnabled()).thenReturn(ServerConfiguration.DEFAULT_ERROR_REPORTING_ENABLED);
        when(mockServerConfig.getSendIntervalInMilliseconds()).thenReturn(ServerConfiguration.DEFAULT_SEND_INTERVAL);
        when(mockServerConfig.getServerID()).thenReturn(ServerConfiguration.DEFAULT_SERVER_ID);
        when(mockServerConfig.getBeaconSizeInBytes()).thenReturn(ServerConfiguration.DEFAULT_BEACON_SIZE);
        when(mockServerConfig.getMultiplicity()).thenReturn(ServerConfiguration.DEFAULT_MULTIPLICITY);
        when(mockServerConfig.getMaxSessionDurationInMilliseconds()).thenReturn(ServerConfiguration.DEFAULT_MAX_SESSION_DURATION);
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(ServerConfiguration.DEFAULT_MAX_EVENTS_PER_SESSION);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(false);
        when(mockServerConfig.getSessionTimeoutInMilliseconds()).thenReturn(ServerConfiguration.DEFAULT_SESSION_TIMEOUT);
        when(mockServerConfig.getVisitStoreVersion()).thenReturn(ServerConfiguration.DEFAULT_VISIT_STORE_VERSION);
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
    public void inDefaultServerConfigurationSendIntervalIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getSendIntervalInMilliseconds(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationServerIDIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getServerID(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationBeaconSizeIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getBeaconSizeInBytes(), is(-1));
    }

    @Test
    public void inDefaultServerConfigurationMultiplicityIsOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getMultiplicity(), is(1));
    }

    @Test
    public void inDefaultServerConfigurationMaxSessionDurationIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getMaxSessionDurationInMilliseconds(), is(-1));
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
    public void inDefaultServerConfigurationVisitStoreVersionIsMinusOne() {
        // then
        assertThat(ServerConfiguration.DEFAULT.getVisitStoreVersion(), is(-1));
    }

    @Test
    public void creatingAServerConfigurationFromNullStatusResponseGivesNull() {
        // when, then
        assertThat(ServerConfiguration.from(null), is(nullValue()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// creating server config from status response
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
    public void creatingAServerConfigurationFromResponseAttributesCopiesSendingIntervalSettings() {
        // given
        when(responseAttributes.getSendIntervalInMilliseconds()).thenReturn(1234);
        ServerConfiguration target = ServerConfiguration.from(responseAttributes);

        // then
        assertThat(target.getSendIntervalInMilliseconds(), is(1234));

        verify(responseAttributes, times(1)).getSendIntervalInMilliseconds();
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
    public void creatingAServerConfigurationFromResponseAttributesHasSplitBySessionEnabledIfMaxEventsGreaterZero() {
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
    public void creatingAServerConfigurationStatusResponseHasSplitBySessionDisabledIfMaxEventsZero() {
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
    public void creatingAServerConfigurationStatusResponseHasSplitBySessionDisabledIfMaxEventsEventsSmallerZero() {
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
    public void creatingAServerConfigurationStatusResponseHasSplitBySessionDisabledIfMaxEventsIsNotSet() {
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
    public void builderFromServerConfigCopiesSendingIntervalSettings() {
        // given
        when(mockServerConfig.getSendIntervalInMilliseconds()).thenReturn(1234);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.getSendIntervalInMilliseconds(), is(1234));
        verify(mockServerConfig, times(1)).getSendIntervalInMilliseconds();
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
    public void builderFromServerConfigHasSplitBySessionEnabledIfMaxEventsGreaterZero() {
        // given
        int eventsPerSession = 1;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(true));
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDisabledIfMaxEventsZero() {
        // given
        int eventsPerSession = 0;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDisabledIfMaxEventsEventsSmallerZero() {
        // given
        int eventsPerSession = -1;
        when(mockServerConfig.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        when(mockServerConfig.isSessionSplitByEventsEnabled()).thenReturn(true);
        ServerConfiguration target = new ServerConfiguration.Builder(mockServerConfig).build();

        // then
        assertThat(target.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void builderFromServerConfigHasSplitBySessionDisabledIfMaxEventsIsNotSet() {
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
        ServerConfiguration target = new ServerConfiguration.Builder().withCapture(false).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withCapture(true).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCaptureEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverDisabledCapture() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder().withCapture(true).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withCapture(false).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCaptureEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverEnabledCrashReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder().withCrashReporting(false).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withCrashReporting(true).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCrashReportingEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverDisabledCrashReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder().withCrashReporting(true).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withCrashReporting(false).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isCrashReportingEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverEnabledErrorReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder().withErrorReporting(false).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withErrorReporting(true).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isErrorReportingEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverDisabledErrorReporting() {
        // given
        ServerConfiguration target = new ServerConfiguration.Builder().withErrorReporting(true).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withErrorReporting(false).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isErrorReportingEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverSendInterval() {
        // given
        int sendInterval = 73;
        ServerConfiguration target = new ServerConfiguration.Builder().withSendIntervalInMilliseconds(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withSendIntervalInMilliseconds(sendInterval).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesOverBeaconSize() {
        // given
        int beaconSize = 73;
        ServerConfiguration target = new ServerConfiguration.Builder().withBeaconSizeInBytes(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withBeaconSizeInBytes(beaconSize).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeIgnoresMultiplicity() {
        // given
        int multiplicity = 73;
        ServerConfiguration target = new ServerConfiguration.Builder().withMultiplicity(multiplicity).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withMultiplicity(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeIgnoresServerId() {
        // given
        int serverId = 73;
        ServerConfiguration target = new ServerConfiguration.Builder().withServerID(serverId).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withServerID(37).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getServerID(), is(serverId));
    }

    @Test
    public void mergeTakesOverMaxSessionDuration() {
        // given
        int sessionDuration = 73;
        ServerConfiguration target = new ServerConfiguration.Builder()
                .withMaxSessionDurationInMilliseconds(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesOverMaxEventsPerSession() {
        // given
        int eventsPerSession = 73;
        ServerConfiguration target = new ServerConfiguration.Builder().withMaxEventsPerSession(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder().withMaxEventsPerSession(eventsPerSession).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesOverIsSessionSplitByEventsEnabledWhenMaxEventsIsGreaterZeroAndAttributeIsSet() {
        // given
        int eventsPerSession = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = new ServerConfiguration.Builder().build();
        ServerConfiguration other = ServerConfiguration.from(responseAttributes);

        assertThat(target.isSessionSplitByEventsEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByEventsEnabled(), is(true));
    }

    @Test
    public void mergeTakesOverIsSessionSplitByEventsEnabledWhenMaxEventsIsSmallerZeroButAttributeIsSet() {
        // given
        int eventsPerSession = 0;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(true);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = new ServerConfiguration.Builder().build();
        ServerConfiguration other = ServerConfiguration.from(responseAttributes);

        assertThat(target.isSessionSplitByEventsEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverIsSessionSplitByEventsEnabledWhenMaxEventsIsGreaterZeroButAttributeIsNotSet() {
        // given
        int eventsPerSession = 73;
        when(responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)).thenReturn(false);
        when(responseAttributes.getMaxEventsPerSession()).thenReturn(eventsPerSession);
        ServerConfiguration target = new ServerConfiguration.Builder().build();
        ServerConfiguration other = ServerConfiguration.from(responseAttributes);

        assertThat(target.isSessionSplitByEventsEnabled(), is(false));

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.isSessionSplitByEventsEnabled(), is(false));
    }

    @Test
    public void mergeTakesOverSessionTimeout() {
        // given
        int sessionTimeout = 73;
        ServerConfiguration target = new ServerConfiguration.Builder()
                .withMaxSessionDurationInMilliseconds(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();

        // when
        ServerConfiguration obtained = target.merge(other);

        // then
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesOverVisitStoreVersion() {
        // given
        int visitStoreVersion = 73;
        ServerConfiguration target = new ServerConfiguration.Builder()
                .withVisitStoreVersion(37).build();
        ServerConfiguration other = new ServerConfiguration.Builder()
                .withVisitStoreVersion(visitStoreVersion).build();

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
        boolean capture = !ServerConfiguration.DEFAULT_CAPTURE_ENABLED;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withCapture(capture).build();

        // then
        assertThat(obtained.isCaptureEnabled(), is(capture));
    }

    @Test
    public void buildPropagatesCrashReportingEnabledToInstance() {
        // given
        boolean crashReporting = !ServerConfiguration.DEFAULT_CRASH_REPORTING_ENABLED;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withCrashReporting(crashReporting).build();

        // then
        assertThat(obtained.isCrashReportingEnabled(), is(crashReporting));
    }

    @Test
    public void buildPropagatesErrorReportingEnabledToInstance() {
        // given
        boolean errorReporting = !ServerConfiguration.DEFAULT_ERROR_REPORTING_ENABLED;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withErrorReporting(errorReporting).build();

        // then
        assertThat(obtained.isErrorReportingEnabled(), is(errorReporting));
    }

    @Test
    public void buildPropagatesSendIntervalToInstance() {
        // given
        int sendInterval = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withSendIntervalInMilliseconds(sendInterval).build();

        // then
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void buildPropagatesServerIdToInstance() {
        // given
        int serverId = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withServerID(serverId).build();

        // then
        assertThat(obtained.getServerID(), is(serverId));
    }

    @Test
    public void buildPropagatesBeaconSizeToInstance() {
        // given
        int beaconSize = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withBeaconSizeInBytes(beaconSize).build();

        // then
        assertThat(obtained.getBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void buildPropagatesMultiplicityToInstance() {
        // given
        int multiplicity = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder().withMultiplicity(multiplicity).build();

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void buildPropagatesMaxSessionDurationToInstance() {
        // given
        int sessionDuration = 73;

        // when
        ServerConfiguration obtained = new ServerConfiguration.Builder()
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
        ServerConfiguration obtained = new ServerConfiguration.Builder()
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
        ServerConfiguration obtained = new ServerConfiguration.Builder()
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
        ServerConfiguration obtained = new ServerConfiguration.Builder()
                .withVisitStoreVersion(visitStoreVersion)
                .build();

        // then
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }
}
