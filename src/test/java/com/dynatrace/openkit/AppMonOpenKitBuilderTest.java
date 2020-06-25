/**
 * Copyright 2018-2020 Dynatrace LLC
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
import static org.junit.Assert.assertThat;

public class AppMonOpenKitBuilderTest {

    private static final String ENDPOINT_URL = "https://www.google.at";
    private static final String APPLICATION_NAME = "the-application-name";
    private static final long DEVICE_ID = 777;

    @Test
    public void constructorInitializesApplicationID() {
        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, DEVICE_ID);

        // then
        assertThat(target.getApplicationName(), is(equalTo(APPLICATION_NAME)));
    }

    @Test
    public void constructorInitializesDeviceIdString() {
        // given, when
        AbstractOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, String.valueOf(DEVICE_ID));

        // then
        assertThat(target.getDeviceID(), is(DEVICE_ID));
        assertThat(target.getOrigDeviceID(), is(String.valueOf(DEVICE_ID)));
    }

    @Test
    public void getApplicationIDGivesSameValueAsApplicationName() {
        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, DEVICE_ID);

        // then
        assertThat(target.getApplicationID(), is(equalTo(APPLICATION_NAME)));
    }

    @Test
    public void getOpenKitTypeGivesAppropriateValue() {
        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, DEVICE_ID);

        // then
        assertThat(target.getOpenKitType(), is(equalTo(AppMonOpenKitBuilder.OPENKIT_TYPE)));
    }

    @Test
    public void getDefaultServerIDGivesAppropriateValue() {
        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT_URL, APPLICATION_NAME, DEVICE_ID);

        // then
        assertThat(target.getDefaultServerID(), is(equalTo(AppMonOpenKitBuilder.DEFAULT_SERVER_ID)));
    }
}
