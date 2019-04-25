/**
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
import com.dynatrace.openkit.providers.TimingProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TimeEvictionStrategyTest {

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
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.getLastRunTimestamp(), is(-1L));
    }

    @Test
    public void theStrategyIsDisabledIfBeaconMaxAgeIsSetToLessThanZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(-1L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.isStrategyDisabled(), is(true));

        // and no interactions were made
        verifyZeroInteractions(mockLogger, mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void theStrategyIsDisabledIfBeaconMaxAgeIsSetToZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(0L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.isStrategyDisabled(), is(true));

        // and no interactions were made
        verifyZeroInteractions(mockLogger, mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void theStrategyIsNotDisabledIFMaxRecordAgeIsGreaterThanZero() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        // then
        assertThat(target.isStrategyDisabled(), is(false));

        // and no interactions were made
        verifyZeroInteractions(mockLogger, mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void shouldRunGivesFalseIfLastRunIsLessThanMaxAgeMillisecondsAgo() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        target.setLastRunTimestamp(1000);
        when(mockTimingProvider.provideTimestampInMilliseconds())
            .thenReturn(target.getLastRunTimestamp() + configuration.getMaxRecordAge() - 1);

        // then
        assertThat(target.shouldRun(), is(false));
    }

    @Test
    public void shouldRunGivesTrueIfLastRunIsExactlyMaxAgeMillisecondsAgo() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        target.setLastRunTimestamp(1000);
        when(mockTimingProvider.provideTimestampInMilliseconds())
            .thenReturn(target.getLastRunTimestamp() + configuration.getMaxRecordAge());

        // then
        assertThat(target.shouldRun(), is(true));
    }

    @Test
    public void shouldRunGivesTrueIfLastRunIsMoreThanMaxAgeMillisecondsAgo() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        target.setLastRunTimestamp(1000);
        when(mockTimingProvider.provideTimestampInMilliseconds())
            .thenReturn(target.getLastRunTimestamp() + configuration.getMaxRecordAge() + 1);

        // then
        assertThat(target.shouldRun(), is(true));
    }

    @Test
    public void executeEvictionLogsAMessageOnceAndReturnsIfStrategyIsDisabled() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(0L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockLogger.isInfoEnabled()).thenReturn(true);

        // when executing the first time
        target.execute();

        // then
        verify(mockLogger, times(1)).isInfoEnabled();
        verify(mockLogger, times(1)).info(anyString());
        verifyNoMoreInteractions(mockLogger);
        verifyZeroInteractions(mockBeaconCache, mockTimingProvider);

        // and when executing a second time
        target.execute();

        // then
        verify(mockLogger, times(1)).isInfoEnabled();
        verify(mockLogger, times(1)).info(anyString());
        verifyNoMoreInteractions(mockLogger);
        verifyZeroInteractions(mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void executeEvictionDoesNotLogIfStrategyIsDisabledAndInfoIsDisabledInLogger() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(0L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockLogger.isInfoEnabled()).thenReturn(false);

        // when executing the first time
        target.execute();

        // then
        verify(mockLogger, times(1)).isInfoEnabled();
        verifyNoMoreInteractions(mockLogger);
        verifyZeroInteractions(mockBeaconCache, mockTimingProvider);

        // and when executing a second time
        target.execute();

        // then
        verify(mockLogger, times(2)).isInfoEnabled();
        verifyNoMoreInteractions(mockLogger);
        verifyZeroInteractions(mockBeaconCache, mockTimingProvider);
    }

    @Test
    public void lastRuntimeStampIsAdjustedDuringFirstExecution() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(1000L, 1001L);

        // when executing the first time
        target.execute();

        // then
        assertThat(target.getLastRunTimestamp(), is(1000L));
        verify(mockTimingProvider, times(2)).provideTimestampInMilliseconds();

        // when executing the second time
        target.execute();

        // then
        assertThat(target.getLastRunTimestamp(), is(1000L));
        verify(mockTimingProvider, times(3)).provideTimestampInMilliseconds();
    }

    @Test
    public void executeEvictionStopsIfNoBeaconIdsAreAvailableInCache() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(1000L, 2000L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(Collections.<Integer>emptySet());

        // when
        target.execute();

        // then verify interactions
        verify(mockBeaconCache, times(1)).getBeaconIDs();
        verify(mockTimingProvider, times(3)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(mockBeaconCache, mockTimingProvider);

        // also ensure that the last run timestamp was updated
        assertThat(target.getLastRunTimestamp(), is(2000L));
    }

    @Test
    public void executeEvictionCallsEvictionForEachBeaconSeparately() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(1000L, 2099L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(1, 42)));

        // when
        target.execute();

        // then verify interactions
        verify(mockBeaconCache, times(1)).getBeaconIDs();
        verify(mockBeaconCache, times(1)).evictRecordsByAge(1, 2099L - configuration.getMaxRecordAge());
        verify(mockBeaconCache, times(1)).evictRecordsByAge(42, 2099L - configuration.getMaxRecordAge());
        verify(mockTimingProvider, times(3)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(mockBeaconCache, mockTimingProvider);

        // also ensure that the last run timestamp was updated
        assertThat(target.getLastRunTimestamp(), is(2099L));
    }

    @Test
    public void executeEvictionLogsTheNumberOfRecordsRemoved() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(1000L, 2099L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(1, 42)));
        when(mockBeaconCache.evictRecordsByAge(eq(1), anyLong())).thenReturn(2);
        when(mockBeaconCache.evictRecordsByAge(eq(42), anyLong())).thenReturn(5);

        when(mockLogger.isDebugEnabled()).thenReturn(true);

        // when
        target.execute();

        // then verify that the logger was invoked
        verify(mockLogger, times(2)).isDebugEnabled();
        verify(mockLogger, times(1)).debug("TimeEvictionStrategy doExecute() - Removed 2 records from Beacon with ID 1");
        verify(mockLogger, times(1)).debug("TimeEvictionStrategy doExecute() - Removed 5 records from Beacon with ID 42");
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void executeEvictionIsStoppedIfThreadGetsInterrupted() {

        // given
        BeaconCacheConfiguration configuration = new BeaconCacheConfiguration(1000L, 1000L, 2000L);
        TimeEvictionStrategy target = new TimeEvictionStrategy(mockLogger, mockBeaconCache, configuration, mockTimingProvider);

        when(mockTimingProvider.provideTimestampInMilliseconds()).thenReturn(1000L, 2099L);
        when(mockBeaconCache.getBeaconIDs()).thenReturn(new HashSet<Integer>(Arrays.asList(1, 42)));
        when(mockBeaconCache.evictRecordsByAge(anyInt(), anyLong())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                Thread.currentThread().interrupt();
                return 2;
            }
        });

        // when
        target.execute();

        // then verify interactions
        verify(mockBeaconCache, times(1)).getBeaconIDs();
        verify(mockBeaconCache, times(1)).evictRecordsByAge(anyInt(), eq(2099L - configuration.getMaxRecordAge()));
        verify(mockTimingProvider, times(3)).provideTimestampInMilliseconds();
        verifyNoMoreInteractions(mockBeaconCache, mockTimingProvider);

        // verify that the interrupted flag is still set & clear it, since the thread is actually not really interrupted
        assertThat(Thread.interrupted(), is(true));
    }
}
