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

package com.dynatrace.openkit;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DynatraceOpenKitBuilderTest {

    private static final String ENDPOINT_URL = "https://www.google.at";
    private static final String APPLICATION_ID = "the-application-identifier";
    private static final String APPLICATION_NAME = "the-application-name";
    private static final long DEVICE_ID = 777;

    @Test
    public void constructorInitializesApplicationID() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getApplicationID(), is(equalTo(APPLICATION_ID)));
    }

    @Test
    public void constructorInitializesDeviceIdString() {
        // given, when
        AbstractOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, String.valueOf(DEVICE_ID));

        // then
        assertThat(target.getDeviceID(), is(DEVICE_ID));
        assertThat(target.getOrigDeviceID(), is(String.valueOf(DEVICE_ID)));
    }

    @Test
    public void getOpenKitTypeGivesAppropriateValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getOpenKitType(), is(equalTo(DynatraceOpenKitBuilder.OPENKIT_TYPE)));
    }

    @Test
    public void getDefaultServerIDGivesAppropriateValue() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getDefaultServerID(), is(equalTo(DynatraceOpenKitBuilder.DEFAULT_SERVER_ID)));
    }

    @Test
    public void defaultApplicationNameIsNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);

        // then
        assertThat(target.getApplicationName(), is(nullValue()));
    }

    @Test
    public void getApplicationNameGivesPreviouslySetApplicationName() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);
        target.withApplicationName(APPLICATION_NAME);

        // then
        assertThat(target.getApplicationName(), is(equalTo(APPLICATION_NAME)));
    }

    @Test
    public void withApplicationNameAllowsOverwritingWithNull() {
        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT_URL, APPLICATION_ID, DEVICE_ID);
        target.withApplicationName(APPLICATION_NAME); // initialize with non-null value

        // when
        target.withApplicationName(null);

        // then
        assertThat(target.getApplicationName(), is(nullValue()));
    }
}
