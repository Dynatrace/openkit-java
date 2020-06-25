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

package com.dynatrace.openkit.providers;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DefaultThreadIDProviderTest {
    @Test
    public void currentThreadIDIsReturned() {
        // given
        ThreadIDProvider provider = new DefaultThreadIDProvider();

        long threadID64 = Thread.currentThread().getId();
        int threadHash = (int)((threadID64 ^ (threadID64 >>> 32)) & 0x7fffffff );
        // then
        assertThat(provider.getThreadID(), is(equalTo(threadHash)));
    }

    @Test
    public void convertNativeThreadIDToPositiveIntegerVerifyXorBitPatterns() {
        // given
        long testValue = 0x0000000600000005L; // bytes 0101 and 0110 -> xor resulting in 0011

        // when
        int result = DefaultThreadIDProvider.convertNativeThreadIDToPositiveInteger(testValue);
        // then
        assertThat(result, is(equalTo(3)));
    }

    @Test
    public void convertNativeThreadIDToPositiveIntegerVerifyMaskMSBFirst() {
        // given
        long testValue = 1L << 31;

        // when
        int result = DefaultThreadIDProvider.convertNativeThreadIDToPositiveInteger(testValue);
        // then
        assertThat(result, is(equalTo(0)));
    }

    @Test
    public void convertNativeThreadIDToPositiveIntegerVerifyMaskMSBSecond() {
        // given
        long testValue = 1L << 63;

        // when
        int result = DefaultThreadIDProvider.convertNativeThreadIDToPositiveInteger(testValue);
        // then
        assertThat(result, is(equalTo(0)));
    }
}
