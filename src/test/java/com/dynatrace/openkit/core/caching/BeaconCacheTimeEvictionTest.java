/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class BeaconCacheTimeEvictionTest {

    private Logger mockLogger;
    private BeaconCache mockBeaconCache;
    private TimingProvider mockTimingProvider;

    @Before
    public void setUp() {

        mockLogger = mock(Logger.class);
        mockBeaconCache = mock(BeaconCache.class);
        mockTimingProvider = mock(TimingProvider.class);
    }

    @Test
    public void theInitialLastRunTimestampIsMinusOne() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(-1L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.getLastRunTimestamp(), is(-1L));
    }

    @Test
    public void theStrategyIsDisabledIfBeaconMaxAgeIsSetToLessThanZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(-1L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.isStrategyDisabled(), is(true));

        // and no interactions were made
        verifyZeroInteractions(mockLogger, mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void theStrategyIsDisabledIfBeaconMaxAgeIsSetToZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(0L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.isStrategyDisabled(), is(true));

        // and no interactions were made
        verifyZeroInteractions(mockLogger, mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void theStrategyIsNotDisabledIFMaxRecordAgeIsGreaterThanZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.isStrategyDisabled(), is(false));

        // and no interactions were made
        verifyZeroInteractions(mockLogger, mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void shouldRunGivesFalseIfLastRunIsLessThanMaxAgeMillisecondsAgo() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        target.setLastRunTimestamp(1000);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(target.getLastRunTimestamp() + configuration.getMaxRecordAge() - 1);

        // then
        assertThat(target.shouldRun(), is(false));
    }

    @Test
    public void shouldRunGivesTrueIfLastRunIsExactlyMaxAgeMillisecondsAgo() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        target.setLastRunTimestamp(1000);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(target.getLastRunTimestamp() + configuration.getMaxRecordAge());

        // then
        assertThat(target.shouldRun(), is(true));
    }

    @Test
    public void shouldRunGivesTrueIfLastRunIsMoreThanMaxAgeMillisecondsAgo() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        BeaconCacheTimeEviction target = new BeaconCacheTimeEviction(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        target.setLastRunTimestamp(1000);
        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(target.getLastRunTimestamp() + configuration.getMaxRecordAge() + 1);

        // then
        assertThat(target.shouldRun(), is(true));
    }
}
