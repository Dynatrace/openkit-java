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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.DynatraceOpenKitBuilder;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.api.http.HttpRequestInterceptor;
import com.dynatrace.openkit.api.http.HttpResponseInterceptor;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenKitConfigurationTest {

    private static final String ENDPOINT_URL = "https://localhost:9999/1";
    private static final long DEVICE_ID = 37;
    private static final String OPENKIT_TYPE = "Dynatrace NextGen";
    private static final String APPLICATION_ID = "Application-ID";
    private static final String APPLICATION_VERSION = "1.2.3.4-b4321";
    private static final String OPERATING_SYSTEM = "Linux #253-Microsoft Mon Dec 31 17:49:00 PST 2018 x86_64 GNU/Linux";
    private static final String MANUFACTURER = "Dynatrace";
    private static final String MODEL_ID = "Least Model";
    private static final int DEFAULT_SERVER_ID = 777;

    private DynatraceOpenKitBuilder dynatraceOpenKitBuilder;

    @Before
    public void setUp() {
        dynatraceOpenKitBuilder = mock(DynatraceOpenKitBuilder.class);
        when(dynatraceOpenKitBuilder.getEndpointURL()).thenReturn(ENDPOINT_URL);
        when(dynatraceOpenKitBuilder.getDeviceID()).thenReturn(DEVICE_ID);
        when(dynatraceOpenKitBuilder.getOpenKitType()).thenReturn(OPENKIT_TYPE);
        when(dynatraceOpenKitBuilder.getApplicationID()).thenReturn(APPLICATION_ID);
        when(dynatraceOpenKitBuilder.getApplicationVersion()).thenReturn(APPLICATION_VERSION);
        when(dynatraceOpenKitBuilder.getOperatingSystem()).thenReturn(OPERATING_SYSTEM);
        when(dynatraceOpenKitBuilder.getManufacturer()).thenReturn(MANUFACTURER);
        when(dynatraceOpenKitBuilder.getModelID()).thenReturn(MODEL_ID);
        when(dynatraceOpenKitBuilder.getDefaultServerID()).thenReturn(DEFAULT_SERVER_ID);
    }

    @Test
    public void creatingOpenKitConfigurationFromNullBuilderGivesNull() {
        // then
        assertThat(OpenKitConfiguration.from(null), is(nullValue()));
    }

    @Test
    public void creatingOpenKitFromNonNullBuilderGivesNonNullConfiguration() {
        // when
        OpenKitConfiguration obtained = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesEndpointUrl() {
        // given, when
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getEndpointURL(), is(ENDPOINT_URL));
        verify(dynatraceOpenKitBuilder, times(1)).getEndpointURL();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesDeviceId() {
        // given, when
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getDeviceID(), is(DEVICE_ID));
        verify(dynatraceOpenKitBuilder, times(1)).getDeviceID();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesType() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getOpenKitType(), is(OPENKIT_TYPE));
        verify(dynatraceOpenKitBuilder, times(1)).getOpenKitType();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesApplicationID() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getApplicationID(), is(APPLICATION_ID));
        verify(dynatraceOpenKitBuilder, times(1)).getApplicationID();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderPercentEncodesApplicationId() {
        // given
        when(dynatraceOpenKitBuilder.getApplicationID()).thenReturn("/App_ID%");
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // when
        String obtained = target.getPercentEncodedApplicationID();

        // then
        assertThat(obtained, is(equalTo("%2FApp%5FID%25")));
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesApplicationVersion() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getApplicationVersion(), is(APPLICATION_VERSION));
        verify(dynatraceOpenKitBuilder, times(1)).getApplicationVersion();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesOperatingSystem() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getOperatingSystem(), is(OPERATING_SYSTEM));
        verify(dynatraceOpenKitBuilder, times(1)).getOperatingSystem();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesManufacturer() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getManufacturer(), is(MANUFACTURER));
        verify(dynatraceOpenKitBuilder, times(1)).getManufacturer();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesModelID() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getModelID(), is(MODEL_ID));
        verify(dynatraceOpenKitBuilder, times(1)).getModelID();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesDefaultServerID() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getDefaultServerID(), is(DEFAULT_SERVER_ID));
        verify(dynatraceOpenKitBuilder, times(1)).getDefaultServerID();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesTrustManager() {
        // given
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        when(dynatraceOpenKitBuilder.getTrustManager()).thenReturn(trustManager);

        // when
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getSSLTrustManager(), is(sameInstance(trustManager)));
        verify(dynatraceOpenKitBuilder, times(1)).getTrustManager();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesHttpRequestInterceptor() {
        // given
        HttpRequestInterceptor requestInterceptor = mock(HttpRequestInterceptor.class);
        when(dynatraceOpenKitBuilder.getHttpRequestInterceptor()).thenReturn(requestInterceptor);

        // when
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getHttpRequestInterceptor(), is(sameInstance(requestInterceptor)));
        verify(dynatraceOpenKitBuilder, times(1)).getHttpRequestInterceptor();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesHttpResponseInterceptor() {
        // given
        HttpResponseInterceptor responseInterceptor = mock(HttpResponseInterceptor.class);
        when(dynatraceOpenKitBuilder.getHttpResponseInterceptor()).thenReturn(responseInterceptor);

        // when
        OpenKitConfiguration target = OpenKitConfiguration.from(dynatraceOpenKitBuilder);

        // then
        assertThat(target.getHttpResponseInterceptor(), is(sameInstance(responseInterceptor)));
        verify(dynatraceOpenKitBuilder, times(1)).getHttpResponseInterceptor();
    }
}
