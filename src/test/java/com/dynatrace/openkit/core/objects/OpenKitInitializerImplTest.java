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

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import com.dynatrace.openkit.api.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenKitInitializerImplTest {

    private final String APP_ID = "appId";
    private final String APP_NAME = "appName";
    private final String APP_VERSION = "1.2.3";
    private AbstractOpenKitBuilder mockBuilder;
    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);

        mockBuilder = mock(AbstractOpenKitBuilder.class);
        when(mockBuilder.getLogger()).thenReturn(mockLogger);
        when(mockBuilder.getApplicationID()).thenReturn(APP_ID);
        when(mockBuilder.getApplicationName()).thenReturn(APP_NAME);
        when(mockBuilder.getApplicationVersion()).thenReturn(APP_VERSION);
    }

    @Test
    public void constructorTakesOverLogger() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getLogger(), is(equalTo(mockLogger)));
    }

    @Test
    public void constructorInitializesPrivacyConfiguration() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getPrivacyConfiguration(), notNullValue());
    }

    @Test
    public void constructorInitializesOpenKitConfiguration() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getOpenKitConfiguration(), notNullValue());
    }

    @Test
    public void constructorInitializesTimingProvider() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getTimingProvider(), notNullValue());
    }

    @Test
    public void constructorInitializesThreadIdProvider() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getThreadIdProvider(), notNullValue());
    }

    @Test
    public void constructorInitializesSessionIdProvider() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getSessionIdProvider(), notNullValue());
    }

    @Test
    public void constructorInitializesBeaconCache() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getBeaconCache(), notNullValue());
    }

    @Test
    public void constructorInitializesBeaconCacheEvictor() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getBeaconCacheEvictor(), notNullValue());

    }

    @Test
    public void constructorInitializesBeaconSender() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getBeaconSender(), notNullValue());
    }

    @Test
    public void constructorInitializesSessionWatchdog() {
        // given, when
        OpenKitInitializerImpl target = createOpenKitInitializer();

        // then
        assertThat(target.getSessionWatchdog(), notNullValue());
    }

    private OpenKitInitializerImpl createOpenKitInitializer() {
        return new OpenKitInitializerImpl(mockBuilder);
    }
}
