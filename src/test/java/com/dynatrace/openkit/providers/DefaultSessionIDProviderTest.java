/**
 * Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit.providers;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DefaultSessionIDProviderTest {

    @Test
    public void defaultSessionIDProviderInitializedWithTimestampReturnsANonNegativeInteger() {
        // given
        // default constructor uses a random number generated from the current system timestamp
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider();

        //when
        int actual = provider.getNextSessionID();

        // then
        assertThat(actual, is(greaterThan(0)));
    }

    @Test
    public void defaultSessionIDProviderProvidesConsecutiveNumbers() {
        //given
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider(Integer.MAX_VALUE / 2);

        // when
        int firstSessionID = provider.getNextSessionID();
        int secondSessionID = provider.getNextSessionID();

        // then
        assertThat(secondSessionID, is(firstSessionID + 1));
    }

    @Test
    public void aProviderInitializedWithMaxIntValueProvidesMinSessionIdValueAtNextCall() {
        //given
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider(Integer.MAX_VALUE);

        //when
        int actual = provider.getNextSessionID();

        //then
        assertThat(actual, is(equalTo(1)));
    }

    @Test
    public void aProviderInitializedWithZeroProvidesMinSessionIdValueAtNextCall() {
        //given
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider(0);

        //when
        int actual = provider.getNextSessionID();

        //then
        assertThat(actual, is(equalTo(1)));
    }
}
