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

package com.dynatrace.openkit.providers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultTimingProviderTest {

    private long clusterOffset = 1234L;
    private long now;
    private DefaultTimingProvider provider;

    @Before
    public void setUp() {
        // store now
        now = System.currentTimeMillis();
        provider = new TestDefaultTimingProvider(now);
    }

    @Test
    public void timeSyncIsSupportedByDefault() {
        // given
        TimingProvider provider = new DefaultTimingProvider();

        // then
        assertThat(provider.isTimeSyncSupported(), is(true));
    }

    @Test
    public void timeSyncIsSupportedIfInitCalledWithTrue() {
        // when
        provider.initialize(0L, true);

        // then
        assertThat(provider.isTimeSyncSupported(), is(true));
    }

    @Test
    public void timeSyncIsNotSupportedIfInitCalledWithFalse() {
        // when
        provider.initialize(0L, false);

        // then
        assertThat(provider.isTimeSyncSupported(), is(false));
    }

    @Test
    public void canConvertToClusterTime() {
        // given
        provider.initialize(clusterOffset, true);

        // when
        long target = provider.convertToClusterTime(now);

        // then
        assertThat(target, is(equalTo(clusterOffset + now)));
    }

    /**
     * DefaultTimingProvider that always returns the same value for provideTimestampInMilliseconds
     */
    private class TestDefaultTimingProvider extends DefaultTimingProvider {
        private final long now;

        TestDefaultTimingProvider(long now) {
            this.now = now;
        }

        @Override
        public long provideTimestampInMilliseconds() {
            return now;
        }
    }
}
