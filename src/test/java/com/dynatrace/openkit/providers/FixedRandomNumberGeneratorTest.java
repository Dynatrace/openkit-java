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
package com.dynatrace.openkit.providers;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FixedRandomNumberGeneratorTest {

    @Test
    public void nextPositiveLongReturnsAlwaysTheSameNumber() {
        // given
        long randomNumber = 1234567890;
        RandomNumberGenerator mockRng = mock(RandomNumberGenerator.class);
        when(mockRng.nextPositiveLong()).thenReturn(randomNumber, 1L, 2L, 4L, 5L);

        FixedRandomNumberGenerator target = new FixedRandomNumberGenerator(mockRng);

        for (int i = 0; i < 100; i++) {
            // when
            long obtained = target.nextPositiveLong();

            // then
            assertThat(obtained, is(randomNumber));
        }
    }

    @Test
    public void nextPercentageValueReturnsAlwaysTheSameNumber() {
        // given
        int randomPercentage = 42;
        RandomNumberGenerator mockRng = mock(RandomNumberGenerator.class);
        when(mockRng.nextPercentageValue()).thenReturn(randomPercentage, 1, 2, 4, 5);

        FixedRandomNumberGenerator target = new FixedRandomNumberGenerator(mockRng);

        for (int i = 0; i < 100; i++) {
            // when
            int obtained = target.nextPercentageValue();

            // then
            assertThat(obtained, is(randomPercentage));
        }
    }
}
