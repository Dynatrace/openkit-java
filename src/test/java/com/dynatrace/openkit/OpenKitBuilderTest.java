/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit;

import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class OpenKitBuilderTest {
    private static final String endpoint = "https://localhost:12345";
    private static final String appID = "asdf123";
    private static final String appName = "myName";
    private static final long deviceID = 1234L;
    private static final String appVersion = "1.2.3.4";
    private static final String os = "custom OS";
    private static final String manufacturer = "custom manufacturer";
    private static final String modelID = "custom model id";

    @Test
    public void defaultsAreSetForAppMon() {
        verifyDefaultsAreSet(new AppMonOpenKitBuilder(endpoint, appID, deviceID).buildConfiguration());
    }

    @Test
    public void defaultsAreSetForDynatrace() {
        verifyDefaultsAreSet(new DynatraceOpenKitBuilder(endpoint, appName, deviceID).buildConfiguration());
    }

    private void verifyDefaultsAreSet(Configuration configuration) {

        // default values
        assertThat(configuration.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
        assertThat(configuration.getDevice().getManufacturer(), is(equalTo(OpenKitConstants.DEFAULT_MANUFACTURER)));
        assertThat(configuration.getDevice()
                                .getOperatingSystem(), is(equalTo(OpenKitConstants.DEFAULT_OPERATING_SYSTEM)));
        assertThat(configuration.getDevice().getModelID(), is(equalTo(OpenKitConstants.DEFAULT_MODEL_ID)));

        // default trust manager
        assertThat(configuration.getHttpClientConfig().getSSLTrustManager(), instanceOf(SSLStrictTrustManager.class));
    }

    @Test
    public void applicationNameIsSetCorrectlyForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceID).buildConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(appName)));
        assertThat(target.getApplicationName(), is(equalTo(target.getApplicationID())));
    }

    @Test
    public void canOverrideTrustManagerForAppMon() {
        SSLTrustManager trustManager = mock(SSLTrustManager.class);

        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceID)
            .withTrustManager(trustManager)
            .buildConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void canOverrideTrustManagerForDynatrace() {
        SSLTrustManager trustManager = mock(SSLTrustManager.class);

        Configuration target = new DynatraceOpenKitBuilder(endpoint, appID, deviceID)
            .withTrustManager(trustManager)
            .buildConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void canSetApplicationVersionForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceID)
            .withApplicationVersion(appVersion)
            .buildConfiguration();

        assertThat(target.getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetApplicationVersionForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appID, deviceID)
            .withApplicationVersion(appVersion)
            .buildConfiguration();

        assertThat(target.getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetOperatingSystemForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceID)
            .withOperatingSystem(os)
            .buildConfiguration();

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetOperatingSystemForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appID, deviceID)
            .withOperatingSystem(os)
            .buildConfiguration();

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetManufacturerForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceID)
            .withManufacturer(manufacturer)
            .buildConfiguration();

        assertThat(target.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetManufactureForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appID, deviceID)
            .withManufacturer(manufacturer)
            .buildConfiguration();

        assertThat(target.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetModelIDForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceID)
            .withModelID(modelID)
            .buildConfiguration();

        assertThat(target.getDevice().getModelID(), is(equalTo(modelID)));
    }

    @Test
    public void canSetModelIDForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appID, deviceID)
            .withModelID(modelID)
            .buildConfiguration();

        assertThat(target.getDevice().getModelID(), is(equalTo(modelID)));
    }

    @Test
    public void canSetAppNameForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appID, deviceID)
            .withApplicationName(appName)
            .buildConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(appName)));
    }
}
