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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class BeaconConfigurationTest {

    private OpenKitConfiguration openKitConfiguration;
    private PrivacyConfiguration privacyConfiguration;
    private static final int SERVER_ID = 1;

    @Before
    public void setUp() {
        openKitConfiguration = mock(OpenKitConfiguration.class);
        privacyConfiguration = mock(PrivacyConfiguration.class);
    }

    @Test
    public void fromWithNullOpenKitConfigurationGivesNull() {
        // when, then
        assertThat(BeaconConfiguration.from(null, privacyConfiguration, SERVER_ID), is(nullValue()));
    }

    @Test
    public void fromWithNullPrivacyConfigurationGivesNull() {
        // when, then
        assertThat(BeaconConfiguration.from(openKitConfiguration, null, SERVER_ID), is(nullValue()));
    }

    @Test
    public void fromWithNonNullArgumentsGivesNonNullBeaconConfiguration() {
        // when
        BeaconConfiguration obtained = createBeaconConfig();

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void getOpenKitConfigurationReturnsPassedObject() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        OpenKitConfiguration obtained = target.getOpenKitConfiguration();

        // then
        assertThat(obtained, is(sameInstance(openKitConfiguration)));
    }

    @Test
    public void getPrivacyConfigurationReturnsPassedObject() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        PrivacyConfiguration obtained = target.getPrivacyConfiguration();

        // then
        assertThat(obtained, is(sameInstance(privacyConfiguration)));
    }

    @Test
    public void newInstanceReturnsDefaultServerConfiguration() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        ServerConfiguration obtained = target.getServerConfiguration();

        // then
        assertThat(obtained, is(sameInstance(ServerConfiguration.DEFAULT)));
    }

    @Test
    public void newInstanceReturnsIsServerConfigurationSetFalse() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        boolean obtained = target.isServerConfigurationSet();

        // then
        assertThat(obtained, is(equalTo(false)));
    }

    @Test
    public void newInstanceReturnsHttpClientConfigWithGivenServerId() {
        // given
        String endpointUrl = "https://localhost:9999/1";
        String applicationId = "some cryptic appId";
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        int serverId = 73;
        when(openKitConfiguration.getEndpointURL()).thenReturn(endpointUrl);
        when(openKitConfiguration.getApplicationID()).thenReturn(applicationId);
        when(openKitConfiguration.getSSLTrustManager()).thenReturn(trustManager);
        when(openKitConfiguration.getDefaultServerID()).thenReturn(serverId);

        BeaconConfiguration target = BeaconConfiguration.from(openKitConfiguration, privacyConfiguration, serverId);

        // when
        HTTPClientConfiguration obtained = target.getHTTPClientConfiguration();

        // then
        assertThat(obtained.getBaseURL(), is(equalTo(endpointUrl)));
        assertThat(obtained.getApplicationID(), is(equalTo(applicationId)));
        assertThat(obtained.getSSLTrustManager(), is(sameInstance(trustManager)));
        assertThat(obtained.getServerID(), is(equalTo(serverId)));
    }

    @Test
    public void updateServerConfigurationSetsIsServerConfigurationSet() {
        // given
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        BeaconConfiguration target = createBeaconConfig();

        // when
        target.updateServerConfiguration(serverConfiguration);

        // then
        assertThat(target.isServerConfigurationSet(), is(equalTo(true)));
        verifyNoMoreInteractions(serverConfiguration);
    }

    @Test
    public void updateServerConfigurationTakesOverServerConfigurationIfNotSet() {
        // given
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        BeaconConfiguration target = createBeaconConfig();

        // when
        target.updateServerConfiguration(serverConfig);

        // then
        assertThat(target.getServerConfiguration(), is(sameInstance(serverConfig)));
    }

    @Test
    public void updateServerConfigurationMergesServerConfigIfAlreadySet() {
        // given
        ServerConfiguration serverConfig1 = mock(ServerConfiguration.class);
        ServerConfiguration serverConfig2 = mock(ServerConfiguration.class);
        when(serverConfig1.merge(serverConfig2)).thenReturn(serverConfig2);

        BeaconConfiguration target = createBeaconConfig();

        // when
        target.updateServerConfiguration(serverConfig1);

        // then
        verifyNoMoreInteractions(serverConfig1);

        // when
        target.updateServerConfiguration(serverConfig2);

        // then
        verify(serverConfig1, times(1)).merge(serverConfig2);
    }

    @Test
    public void updateServerConfigurationDoesNotUpdateHttpClientConfig() {
        // given
        int serverId = 73;
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        when(serverConfig.getServerID()).thenReturn(serverId);
        BeaconConfiguration target = createBeaconConfig();
        HTTPClientConfiguration httpConfig = target.getHTTPClientConfiguration();

        // when

        target.updateServerConfiguration(serverConfig);
        HTTPClientConfiguration obtained = target.getHTTPClientConfiguration();

        // then
        assertThat(obtained.getServerID(), is(equalTo(SERVER_ID)));
        assertThat(obtained, is(sameInstance(httpConfig)));
    }

    @Test
    public void updateServerConfigurationDoesNotUpdateHttpClientConfigurationIfServerIdEquals() {
        // given
        int serverId = 73;
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        when(serverConfig.getServerID()).thenReturn(serverId);
        when(openKitConfiguration.getDefaultServerID()).thenReturn(serverId);
        BeaconConfiguration target = createBeaconConfig();
        HTTPClientConfiguration httpConfig = target.getHTTPClientConfiguration();

        // when
        target.updateServerConfiguration(serverConfig);
        HTTPClientConfiguration obtained = target.getHTTPClientConfiguration();

        // then
        assertThat(obtained, is(sameInstance(httpConfig)));
    }

    @Test
    public void updateServerConfigurationWithNullDoesNothing() {
        // given
        BeaconConfiguration target = createBeaconConfig();
        HTTPClientConfiguration httpConfig = spy(target.getHTTPClientConfiguration());

        // when
        target.updateServerConfiguration(null);

        // then
        verifyZeroInteractions(httpConfig);
        assertThat(target.isServerConfigurationSet(), is(false));
        assertThat(target.getServerConfiguration(), is(sameInstance(ServerConfiguration.DEFAULT)));
    }

    @Test
    public void enableCaptureSetsIsConfigurationSet() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        target.enableCapture();

        // then
        assertThat(target.isServerConfigurationSet(), is(true));
    }

    @Test
    public void enableCaptureUpdatesServerConfigIfCaptureIsDisabled() {
        // given
        ServerConfiguration initialServerConfig = mock(ServerConfiguration.class);
        when(initialServerConfig.isCaptureEnabled()).thenReturn(false);

        BeaconConfiguration target = createBeaconConfig();
        target.updateServerConfiguration(initialServerConfig);

        // when
        target.enableCapture();
        ServerConfiguration obtained = target.getServerConfiguration();

        // then
        assertThat(obtained, is(not(sameInstance(initialServerConfig))));
        assertThat(obtained, is(not(sameInstance(ServerConfiguration.DEFAULT))));
        assertThat(obtained.isCaptureEnabled(), is(true));
    }

    @Test
    public void enableCaptureDoesOnlyModifyCaptureFlag() {
        // given
       ServerConfiguration initialServerConfig = mockServerConfig(false);

        BeaconConfiguration target = createBeaconConfig();
        target.updateServerConfiguration(initialServerConfig);

        // when
        target.enableCapture();
        ServerConfiguration obtained = target.getServerConfiguration();

        // then
        assertThat(obtained, is(not(sameInstance(initialServerConfig))));
        assertThat(obtained.isCaptureEnabled(), is(not(equalTo(initialServerConfig.isCaptureEnabled()))));
        assertThat(obtained.isCrashReportingEnabled(), is(equalTo(initialServerConfig.isCrashReportingEnabled())));
        assertThat(obtained.isErrorReportingEnabled(), is(equalTo(initialServerConfig.isErrorReportingEnabled())));
        assertThat(obtained.getSendIntervalInMilliseconds(),
                is(equalTo(initialServerConfig.getSendIntervalInMilliseconds())));
        assertThat(obtained.getServerID(), is(equalTo(initialServerConfig.getServerID())));
        assertThat(obtained.getBeaconSizeInBytes(), is(equalTo(initialServerConfig.getBeaconSizeInBytes())));
        assertThat(obtained.getMultiplicity(), is(equalTo(initialServerConfig.getMultiplicity())));
    }

    @Test
    public void disableCaptureDoesSetsIsServerConfigurationSet() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        target.disableCapture();

        // then
        assertThat(target.isServerConfigurationSet(), is(true));
    }

    @Test
    public void disableCaptureUpdatesServerConfigIfCaptureGetsDisabled() {
        // given
        ServerConfiguration initialServerConfig = mock(ServerConfiguration.class);
        when(initialServerConfig.isCaptureEnabled()).thenReturn(true);

        BeaconConfiguration target = createBeaconConfig();
        target.updateServerConfiguration(initialServerConfig);

        // when
        target.disableCapture();
        ServerConfiguration obtained = target.getServerConfiguration();

        // then
        assertThat(obtained, is(not(sameInstance(initialServerConfig))));
        assertThat(obtained, is(not(sameInstance(ServerConfiguration.DEFAULT))));
        assertThat(obtained.isCaptureEnabled(), is(equalTo(false)));
    }

    @Test
    public void disableCaptureDoesOnlyModifyCaptureFlag() {
        // given
        ServerConfiguration initialServerConfig = mockServerConfig(true);

        BeaconConfiguration target = createBeaconConfig();
        target.updateServerConfiguration(initialServerConfig);

        // when
        target.disableCapture();
        ServerConfiguration obtained = target.getServerConfiguration();

        // then
        assertThat(obtained, is(not(sameInstance(initialServerConfig))));
        assertThat(obtained.isCaptureEnabled(), is(not(equalTo(initialServerConfig.isCaptureEnabled()))));
        assertThat(obtained.isCrashReportingEnabled(), is(equalTo(initialServerConfig.isCrashReportingEnabled())));
        assertThat(obtained.isErrorReportingEnabled(), is(equalTo(initialServerConfig.isErrorReportingEnabled())));
        assertThat(obtained.getSendIntervalInMilliseconds(),
                is(equalTo(initialServerConfig.getSendIntervalInMilliseconds())));
        assertThat(obtained.getServerID(), is(equalTo(initialServerConfig.getServerID())));
        assertThat(obtained.getBeaconSizeInBytes(), is(equalTo(initialServerConfig.getBeaconSizeInBytes())));
        assertThat(obtained.getMultiplicity(), is(equalTo(initialServerConfig.getMultiplicity())));
    }

    @Test
    public void disableCaptureSetsIsServerConfigurationSet() {
        // given
        BeaconConfiguration target = createBeaconConfig();

        // when
        target.disableCapture();

        // then
        assertThat(target.isServerConfigurationSet(), is(true));
    }

    private BeaconConfiguration createBeaconConfig() {
        return BeaconConfiguration.from(openKitConfiguration, privacyConfiguration, SERVER_ID);
    }

    private ServerConfiguration mockServerConfig(boolean enableCapture) {
        ServerConfiguration initialServerConfig = mock(ServerConfiguration.class);
        when(initialServerConfig.isCaptureEnabled()).thenReturn(enableCapture);
        when(initialServerConfig.isCrashReportingEnabled()).thenReturn(true);
        when(initialServerConfig.isErrorReportingEnabled()).thenReturn(true);
        when(initialServerConfig.getSendIntervalInMilliseconds()).thenReturn(999);
        when(initialServerConfig.getServerID()).thenReturn(73);
        when(initialServerConfig.getBeaconSizeInBytes()).thenReturn(1024);
        when(initialServerConfig.getMultiplicity()).thenReturn(37);

        return initialServerConfig;
    }
}
