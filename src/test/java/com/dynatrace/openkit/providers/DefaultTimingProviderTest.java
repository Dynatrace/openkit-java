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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class DefaultTimingProviderTest {

    @Test
    public void provideTimeStampInMillisecondsReturnsCurrentTime() {
        // given
        DefaultTimingProvider target = new DefaultTimingProvider();

        // when
        long timeBefore = System.currentTimeMillis();
        long obtained = target.provideTimestampInMilliseconds();
        long timeAfter = System.currentTimeMillis();

        // then
        assertThat(obtained, greaterThanOrEqualTo(timeBefore));
        assertThat(obtained, lessThanOrEqualTo(timeAfter));
    }

    @Test
    public void sleepSuspendsForSpecifiedTime() throws Exception {
        // given
        long sleepTimeInMillis = 2;
        DefaultTimingProvider target = new DefaultTimingProvider();

        // when
        long timeInNanosBefore = System.nanoTime();
        target.sleep(sleepTimeInMillis);
        long sleptTimeInNanos = System.nanoTime() - timeInNanosBefore;

        // then
        assertThat(sleptTimeInNanos, greaterThanOrEqualTo(sleepTimeInMillis * DefaultTimingProvider.MILLIS_TO_NANOS_FACTOR));
    }
}
