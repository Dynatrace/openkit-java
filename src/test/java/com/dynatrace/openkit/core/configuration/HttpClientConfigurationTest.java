/**
 *   Copyright 2018-2021 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.api.http.HttpRequestInterceptor;
import com.dynatrace.openkit.api.http.HttpResponseInterceptor;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpClientConfigurationTest {
    @Test
    public void instanceFromOpenKitConfigTakesOverEndpointUrl() {
        // given
        String endpointUrl = "https://localhost:9999/1";
        OpenKitConfiguration openKitConfig = mock(OpenKitConfiguration.class);
        when(openKitConfig.getEndpointURL()).thenReturn(endpointUrl);

        HTTPClientConfiguration target = HTTPClientConfiguration.from(openKitConfig);

        // when
        String obtained = target.getBaseURL();

        // then
        verify(openKitConfig, times(1)).getEndpointURL();
        assertThat(obtained, is(equalTo(endpointUrl)));
    }

    @Test
    public void instanceFromOpenKitConfigTakesOverApplicationId() {
        // given
        String applicationId = "some cryptic appId";
        OpenKitConfiguration openKitConfig = mock(OpenKitConfiguration.class);
        when(openKitConfig.getApplicationID()).thenReturn(applicationId);

        HTTPClientConfiguration target = HTTPClientConfiguration.from(openKitConfig);

        // when
        String obtained = target.getApplicationID();

        // then
        verify(openKitConfig, times(1)).getApplicationID();
        assertThat(obtained, is(equalTo(applicationId)));
    }

    @Test
    public void instanceFromOpenKitConfigTakesOverTrustManager() {
        // given
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        OpenKitConfiguration openKitConfig = mock(OpenKitConfiguration.class);
        when(openKitConfig.getSSLTrustManager()).thenReturn(trustManager);

        HTTPClientConfiguration target = HTTPClientConfiguration.from(openKitConfig);

        // when
        SSLTrustManager obtained = target.getSSLTrustManager();

        // then
        verify(openKitConfig, times(1)).getSSLTrustManager();
        assertThat(obtained, sameInstance(trustManager));
    }

    @Test
    public void instanceFromOpenKitConfigTakesOverDefaultServerId() {
        // given
        int defaultServerId = 1;
        OpenKitConfiguration openKitConfig = mock(OpenKitConfiguration.class);
        when(openKitConfig.getDefaultServerID()).thenReturn(defaultServerId);

        HTTPClientConfiguration target = HTTPClientConfiguration.from(openKitConfig);

        // when
        int obtained = target.getServerID();

        // then
        verify(openKitConfig, times(1)).getDefaultServerID();
        assertThat(obtained, is(equalTo(defaultServerId)));
    }

    @Test
    public void instanceFromOpenKitConfigTakesOverHttpRequestInterceptor() {
        // given
        HttpRequestInterceptor httpRequestInterceptor = mock(HttpRequestInterceptor.class);
        OpenKitConfiguration openKitConfig = mock(OpenKitConfiguration.class);
        when(openKitConfig.getHttpRequestInterceptor()).thenReturn(httpRequestInterceptor);

        HTTPClientConfiguration target = HTTPClientConfiguration.from(openKitConfig);

        // when
        HttpRequestInterceptor obtained = target.getHttpRequestInterceptor();

        // then
        verify(openKitConfig, times(1)).getHttpRequestInterceptor();
        assertThat(obtained, is(equalTo(httpRequestInterceptor)));
    }

    @Test
    public void instanceFromOpenKitConfigTakesOverHttpResponseInterceptor() {
        // given
        HttpResponseInterceptor httpResponseInterceptor = mock(HttpResponseInterceptor.class);
        OpenKitConfiguration openKitConfig = mock(OpenKitConfiguration.class);
        when(openKitConfig.getHttpResponseInterceptor()).thenReturn(httpResponseInterceptor);

        HTTPClientConfiguration target = HTTPClientConfiguration.from(openKitConfig);

        // when
        HttpResponseInterceptor obtained = target.getHttpResponseInterceptor();

        // then
        verify(openKitConfig, times(1)).getHttpResponseInterceptor();
        assertThat(obtained, is(equalTo(httpResponseInterceptor)));
    }

    @Test
    public void builderFromHttpClientConfigTakesOverBaseUrl() {
        // given
        String baseUrl = "https://localhost:9999/1";
        HTTPClientConfiguration httpConfig = mock(HTTPClientConfiguration.class);
        when(httpConfig.getBaseURL()).thenReturn(baseUrl);

        // when
        HTTPClientConfiguration target = HTTPClientConfiguration.modifyWith(httpConfig).build();

        // then
        verify(httpConfig, times(1)).getBaseURL();
        assertThat(target.getBaseURL(), is(equalTo(baseUrl)));
    }

    @Test
    public void builderFromHttpClientConfigTakesOverApplicationId() {
        // given
        String applicationId = "some cryptic appId";
        HTTPClientConfiguration httpConfig = mock(HTTPClientConfiguration.class);
        when(httpConfig.getApplicationID()).thenReturn(applicationId);

        // when
        HTTPClientConfiguration target = HTTPClientConfiguration.modifyWith(httpConfig).build();

        // then
        verify(httpConfig, times(1)).getApplicationID();
        assertThat(target.getApplicationID(), is(equalTo(applicationId)));
    }

    @Test
    public void builderFromHttpClientConfigTakesOverTrustManager() {
        // given
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        HTTPClientConfiguration httpConfig = mock(HTTPClientConfiguration.class);
        when(httpConfig.getSSLTrustManager()).thenReturn(trustManager);

        // when
        HTTPClientConfiguration target = HTTPClientConfiguration.modifyWith(httpConfig).build();

        // then
        verify(httpConfig, times(1)).getSSLTrustManager();
        assertThat(target.getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void builderFromHttpClientConfigTakesOverServerId() {
        // given
        int serverId = 1;
        HTTPClientConfiguration httpConfig = mock(HTTPClientConfiguration.class);
        when(httpConfig.getServerID()).thenReturn(serverId);

        // when
        HTTPClientConfiguration target = HTTPClientConfiguration.modifyWith(httpConfig).build();

        // then
        verify(httpConfig, times(1)).getServerID();
        assertThat(target.getServerID(), is(equalTo(serverId)));
    }

    @Test
    public void builderFromHttpClientConfigTakesHttpRequestInterceptor() {
        // given
        HttpRequestInterceptor httpRequestInterceptor = mock(HttpRequestInterceptor.class);
        HTTPClientConfiguration httpConfig = mock(HTTPClientConfiguration.class);
        when(httpConfig.getHttpRequestInterceptor()).thenReturn(httpRequestInterceptor);

        // when
        HTTPClientConfiguration target = HTTPClientConfiguration.modifyWith(httpConfig).build();

        // then
        verify(httpConfig, times(1)).getHttpRequestInterceptor();
        assertThat(target.getHttpRequestInterceptor(), is(sameInstance(httpRequestInterceptor)));
    }

    @Test
    public void builderFromHttpClientConfigTakesHttpResponseInterceptor() {
        // given
        HttpResponseInterceptor httpResponseInterceptor = mock(HttpResponseInterceptor.class);
        HTTPClientConfiguration httpConfig = mock(HTTPClientConfiguration.class);
        when(httpConfig.getHttpResponseInterceptor()).thenReturn(httpResponseInterceptor);

        // when
        HTTPClientConfiguration target = HTTPClientConfiguration.modifyWith(httpConfig).build();

        // then
        verify(httpConfig, times(1)).getHttpResponseInterceptor();
        assertThat(target.getHttpResponseInterceptor(), is(sameInstance(httpResponseInterceptor)));
    }

    @Test
    public void emptyBuilderCreatesEmptyInstance() {
        // given
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder();

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getBaseURL(), is(nullValue()));
        assertThat(obtained.getApplicationID(), is(nullValue()));
        assertThat(obtained.getSSLTrustManager(), is(nullValue()));
        assertThat(obtained.getServerID(), is(equalTo(-1)));
    }

    @Test
    public void builderWithBaseUrlPropagatesToInstance() {
        // given
        String baseUrl = "https://localhost:9999/1";
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder().withBaseURL(baseUrl);

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getBaseURL(), is(equalTo(baseUrl)));
    }

    @Test
    public void builderWithApplicationIdPropagatesToInstance() {
        // given
        String applicationId = "some cryptic appId";
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder().withApplicationID(applicationId);

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getApplicationID(), is(equalTo(applicationId)));
    }

    @Test
    public void builderWithTrustManagerPropagatesToInstance() {
        // given
        SSLTrustManager trustManager = mock(SSLTrustManager.class);
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder().withSSLTrustManager(trustManager);

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void builderWithServerIdPropagatesToInstance() {
        // given
        int serverId = 1;
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder().withServerID(serverId);

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getServerID(), is(equalTo(serverId)));
    }

    @Test
    public void builderWithHttpRequestInterceptorPropagatesToInstance() {
        // given
        HttpRequestInterceptor httpRequestInterceptor = mock(HttpRequestInterceptor.class);
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder()
            .withHttpRequestInterceptor(httpRequestInterceptor);

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getHttpRequestInterceptor(), is(sameInstance(httpRequestInterceptor)));
    }

    @Test
    public void builderWithHttpResponseInterceptorPropagatesToInstance() {
        // given
        HttpResponseInterceptor httpResponseInterceptor = mock(HttpResponseInterceptor.class);
        HTTPClientConfiguration.Builder target = new HTTPClientConfiguration.Builder()
            .withHttpResponseInterceptor(httpResponseInterceptor);

        // when
        HTTPClientConfiguration obtained = target.build();

        // then
        assertThat(obtained.getHttpResponseInterceptor(), is(sameInstance(httpResponseInterceptor)));
    }
}
