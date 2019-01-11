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

package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SpaceEvictionStrategyTest {

    private Logger mockLogger;
    private BeaconCache mockBeaconCache;

    @Before
    public void setUp() {

        mockLogger = mock(Logger.class);
        mockBeaconCache = mock(BeaconCache.class);
    }

    @Test
    public void theStrategyIsDisabledIfCacheSizeLowerBoundIsLessThanZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, -1L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        // then
        assertThat(target.isStrategyDisabled(), is(true));
    }

    @Test
    public void theStrategyIsDisabledIfCacheSizeLowerBoundIsEqualToZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 0L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        // then
        assertThat(target.isStrategyDisabled(), is(true));
    }

    @Test
    public void theStrategyIsDisabledIfCacheSizeUpperBoundIsLessThanZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, -1L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        // then
        assertThat(target.isStrategyDisabled(), is(true));
    }

    @Test
    public void theStrategyIsDisabledIfCacheSizeUpperBoundIsEqualToZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 0L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        // then
        assertThat(target.isStrategyDisabled(), is(true));
    }

    @Test
    public void theStrategyIsDisabledIfCacheSizeUpperBoundIsLessThanLowerBound() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 999L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        // then
        assertThat(target.isStrategyDisabled(), is(true));
    }

    @Test
    public void shouldRunGivesTrueIfNumBytesInCacheIsGreaterThanUpperBoundLimit() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1);

        // then
        assertThat(target.shouldRun(), is(true));
    }

    @Test
    public void shouldRunGivesFalseIfNumBytesInCacheIsEqualToUpperBoundLimit() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound());

        // then
        assertThat(target.shouldRun(), is(false));
    }

    @Test
    public void shouldRunGivesFalseIfNumBytesInCacheIsLessThanUpperBoundLimit() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() - 1);

        // then
        assertThat(target.shouldRun(), is(false));
    }

    @Test
    public void executeEvictionLogsAMessageOnceAndReturnsIfStrategyIsDisabled() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, -1L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockLogger.isInfoEnabled()).thenReturn(true);

        // when executing the first time
        target.execute();

        // then
        verify(mockLogger, times(1)).isInfoEnabled();
        verify(mockLogger, times(1)).info(anyString());
        verifyNoMoreInteractions(mockLogger);

        // and when executing a second time
        target.execute();

        // then
        verify(mockLogger, times(1)).isInfoEnabled();
        verify(mockLogger, times(1)).info(anyString());
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void executeEvictionDoesNotLogIfStrategyIsDisabledAndInfoIsDisabledInLogger() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, -1L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockLogger.isInfoEnabled()).thenReturn(false);

        // when executing the first time
        target.execute();

        // then
        verify(mockLogger, times(1)).isInfoEnabled();
        verifyNoMoreInteractions(mockLogger);

        // and when executing a second time
        target.execute();

        // then
        verify(mockLogger, times(2)).isInfoEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void executeEvictionCallsCacheMethodForEachBeacon() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            0L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(42, 1)));


        // when executing the first time
        target.execute();

        // then
        verify(mockBeaconCache, times(5)).getNumBytesInCache();
        verify(mockBeaconCache, times(1)).evictRecordsByNumber(1, 1);
        verify(mockBeaconCache, times(1)).evictRecordsByNumber(42, 1);
    }

    @Test
    public void executeEvictionLogsEvictionResultIfDebugIsEnabled() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            0L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(42, 1)));
        when(mockBeaconCache.evictRecordsByNumber(eq(1), anyInt())).thenReturn(5);
        when(mockBeaconCache.evictRecordsByNumber(eq(42), anyInt())).thenReturn(1);

        when(mockLogger.isDebugEnabled()).thenReturn(true);

        // when executing the first time
        target.execute();

        // then
        verify(mockLogger, times(3)).isDebugEnabled();
        verify(mockLogger, times(1)).debug("SpaceEvictionStrategy doExecute()  - Removed 5 records from Beacon with ID 1");
        verify(mockLogger, times(1)).debug("SpaceEvictionStrategy doExecute()  - Removed 1 records from Beacon with ID 42");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void executeEvictionDoesNotLogEvictionResultIfDebugIsDisabled() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            configuration.getCacheSizeUpperBound() + 1,
            0L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(42, 1)));
        when(mockBeaconCache.evictRecordsByNumber(eq(1), anyInt())).thenReturn(5);
        when(mockBeaconCache.evictRecordsByNumber(eq(42), anyInt())).thenReturn(1);

        when(mockLogger.isDebugEnabled()).thenReturn(false);

        // when executing the first time
        target.execute();

        // then
        verify(mockLogger, times(3)).isDebugEnabled();
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void executeEvictionRunsUntilTheCacheSizeIsLessThanOrEqualToLowerBound() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1, // shouldRun method
            configuration.getCacheSizeUpperBound(), // first iteration
            configuration.getCacheSizeUpperBound(), // first iteration
            configuration.getCacheSizeUpperBound(), // first iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            configuration.getCacheSizeLowerBound(), // stops already
            0L); // just for safety
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(42, 1)));


        // when executing the first time
        target.execute();

        // then
        verify(mockBeaconCache, times(8)).getNumBytesInCache();
        verify(mockBeaconCache, times(2)).evictRecordsByNumber(1, 1);
        verify(mockBeaconCache, times(2)).evictRecordsByNumber(42, 1);
    }

    @Test
    public void executeEvictionStopsIfThreadGetsInterruptedBetweenTwoBeacons() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1, // shouldRun method
            configuration.getCacheSizeUpperBound(), // first iteration
            configuration.getCacheSizeUpperBound(), // first iteration
            configuration.getCacheSizeUpperBound(), // first iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            configuration.getCacheSizeLowerBound(), // stops already
            0L); // just for safety
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(42, 1)));
        when(mockBeaconCache.evictRecordsByNumber(anyInt(), eq(1))).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                Thread.currentThread().interrupt(); // interrupt current thread - just to test, if it stopped
                return 5;
            }
        });

        // when executing the first time
        target.execute();

        // then
        verify(mockBeaconCache, times(3)).getNumBytesInCache();
        verify(mockBeaconCache, times(1)).evictRecordsByNumber(anyInt(), eq(1));

        // and verify that the thread interrupted flag is still set
        assertThat(Thread.interrupted(), is(true)); // will also clear the interrupted flag, which we definitely want
    }

    @Test
    public void executeEvictionStopsIfNumBytesInCacheFallsBelowLowerBoundBetweenTwoBeacons() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        SpaceEvictionStrategy target = new SpaceEvictionStrategy(mockLogger, mockBeaconCache, configuration);

        when(mockBeaconCache.getNumBytesInCache()).thenReturn(configuration.getCacheSizeUpperBound() + 1, // shouldRun method
            configuration.getCacheSizeUpperBound(), // first iteration
            configuration.getCacheSizeUpperBound(), // first iteration
            configuration.getCacheSizeUpperBound(), // first iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            (configuration.getCacheSizeUpperBound() + configuration.getCacheSizeLowerBound()) / 2, // second iteration
            configuration.getCacheSizeLowerBound(), // second iteration
            0L); // just for safety
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(42, 1)));

        // when executing the first time
        target.execute();

        // then
        verify(mockBeaconCache, times(8)).getNumBytesInCache();
        verify(mockBeaconCache, times(3)).evictRecordsByNumber(anyInt(), eq(1));
    }
}
