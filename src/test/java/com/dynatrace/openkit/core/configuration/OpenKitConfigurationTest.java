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

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenKitConfigurationTest {

    private static final String OPENKIT_TYPE = "Dynatrace NextGen";
    private static final String APPLICATION_ID = "Application-ID";
    private static final String APPLICATION_NAME = "Application Name";
    private static final String APPLICATION_VERSION = "1.2.3.4-b4321";
    private static final String OPERATING_SYSTEM = "Linux #253-Microsoft Mon Dec 31 17:49:00 PST 2018 x86_64 GNU/Linux";
    private static final String MANUFACTURER = "Dynatrace";
    private static final String MODEL_ID = "Least Model";
    private static final int DEFAULT_SERVER_ID = 777;

    private AbstractOpenKitBuilder abstractOpenKitBuilder;

    @Before
    public void setUp() {
        abstractOpenKitBuilder = mock(AbstractOpenKitBuilder.class);
        when(abstractOpenKitBuilder.getOpenKitType()).thenReturn(OPENKIT_TYPE);
        when(abstractOpenKitBuilder.getApplicationID()).thenReturn(APPLICATION_ID);
        when(abstractOpenKitBuilder.getApplicationName()).thenReturn(APPLICATION_NAME);
        when(abstractOpenKitBuilder.getApplicationVersion()).thenReturn(APPLICATION_VERSION);
        when(abstractOpenKitBuilder.getOperatingSystem()).thenReturn(OPERATING_SYSTEM);
        when(abstractOpenKitBuilder.getManufacturer()).thenReturn(MANUFACTURER);
        when(abstractOpenKitBuilder.getModelID()).thenReturn(MODEL_ID);
        when(abstractOpenKitBuilder.getDefaultServerID()).thenReturn(DEFAULT_SERVER_ID);
    }

    @Test
    public void creatingOpenKitConfigurationFromNullBuilderGivesNull() {
        // then
        assertThat(OpenKitConfiguration.from(null), is(nullValue()));
    }

    @Test
    public void creatingOpenKitFromNonNullBuilderGivesNonNullConfiguration() {
        // when
        OpenKitConfiguration obtained = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(obtained, is(notNullValue()));
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesType() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getOpenKitType(), is(OPENKIT_TYPE));
        verify(abstractOpenKitBuilder, times(1)).getOpenKitType();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesApplicationID() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getApplicationID(), is(APPLICATION_ID));
        verify(abstractOpenKitBuilder, times(1)).getApplicationID();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesApplicationName() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getApplicationName(), is(APPLICATION_NAME));
        verify(abstractOpenKitBuilder, times(1)).getApplicationName();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesApplicationVersion() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getApplicationVersion(), is(APPLICATION_VERSION));
        verify(abstractOpenKitBuilder, times(1)).getApplicationVersion();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesOperatingSystem() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getOperatingSystem(), is(OPERATING_SYSTEM));
        verify(abstractOpenKitBuilder, times(1)).getOperatingSystem();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesManufacturer() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getManufacturer(), is(MANUFACTURER));
        verify(abstractOpenKitBuilder, times(1)).getManufacturer();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesModelID() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getModelID(), is(MODEL_ID));
        verify(abstractOpenKitBuilder, times(1)).getModelID();
    }

    @Test
    public void creatingAnOpenKitConfigurationFromBuilderCopiesDefaultServerID() {
        // given
        OpenKitConfiguration target = OpenKitConfiguration.from(abstractOpenKitBuilder);

        // then
        assertThat(target.getDefaultServerID(), is(DEFAULT_SERVER_ID));
        verify(abstractOpenKitBuilder, times(1)).getDefaultServerID();
    }
}
