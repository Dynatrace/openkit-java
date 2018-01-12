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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconCacheEvictorTest {

    private Logger mockLogger;
    private BeaconCache mockBeaconCache;
    private BeaconCacheEvictionStrategy mockStrategyOne;
    private BeaconCacheEvictionStrategy mockStrategyTwo;

    private BeaconCacheEvictor evictor;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        when(mockLogger.isErrorEnabled()).thenReturn(true);

        mockBeaconCache = mock(BeaconCache.class);
        mockStrategyOne = mock(BeaconCacheEvictionStrategy.class);
        mockStrategyTwo = mock(BeaconCacheEvictionStrategy.class);
        evictor = null;
    }

    @After
    public void tearDown() {
        if (evictor != null) {
            evictor.stop(TimeUnit.MINUTES.toMillis(1));
        }
    }

    @Test
    public void aDefaultConstructedBeaconCacheEvictorIsNotAlive() {

        // given
        evictor = new BeaconCacheEvictor(mockLogger, mockBeaconCache);

        // then
        assertThat(evictor.isAlive(), is(false));
    }

    @Test
    public void afterStartingABeaconCacheEvictorItIsAlive() {

        // given
        evictor = new BeaconCacheEvictor(mockLogger, mockBeaconCache);

        // when
        boolean obtained = evictor.start();

        // then
        assertThat(obtained, is(true));
        assertThat(evictor.isAlive(), is(true));
    }

    @Test
    public void startingAnAlreadyAliveBeaconCacheEvictorDoesNothing() {

        // given
        evictor = new BeaconCacheEvictor(mockLogger, mockBeaconCache);
        evictor.start();

        // when trying to start the evictor again
        boolean obtained = evictor.start();

        // then
        assertThat(obtained, is(false));
        assertThat(evictor.isAlive(), is(true));
    }

    @Test
    public void stoppingABeaconCacheEvictorWhichIsNotAliveDoesNothing() {

        // given
        evictor = new BeaconCacheEvictor(mockLogger, mockBeaconCache);

        // when
        boolean obtained = evictor.stop();

        // then
        assertThat(obtained, is(false));
        assertThat(evictor.isAlive(), is(false));
    }

    @Test
    public void stoppingAnAliveBeaconCacheEvictor() {

        // given
        evictor = new BeaconCacheEvictor(mockLogger, mockBeaconCache);
        evictor.start();

        // when
        boolean obtained = evictor.stop();

        // then
        assertThat(obtained, is(true));
        assertThat(evictor.isAlive(), is(false));
    }

    @Test
    public void triggeringEvictionStrategiesInThread() throws Exception {

        // given
        final Observer[] observers  = new Observer[]{null};
        final CountDownLatch addObserverLath = new CountDownLatch(1);
        final CyclicBarrier strategyInvokedBarrier = new CyclicBarrier(2);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                observers[0] = (Observer)invocation.getArguments()[0];
                addObserverLath.countDown();

                return null;
            }
        }).when(mockBeaconCache).addObserver(org.mockito.Matchers.any(Observer.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                strategyInvokedBarrier.await();

                return null;
            }
        }).when(mockStrategyTwo).executeEviction();

        // first step start the eviction thread
        evictor = new BeaconCacheEvictor(mockLogger, mockBeaconCache, mockStrategyOne, mockStrategyTwo);
        evictor.start();

        // wait until the eviction thread registered itself as observer
        addObserverLath.await();

        // verify the observer was set
        assertThat(observers[0], is(notNullValue()));

        // do some updates
        for (int i = 0; i < 10; i++) {
            observers[0].update(mock(Observable.class), null);
            strategyInvokedBarrier.await();
            strategyInvokedBarrier.reset();
        }

        // stop teh stuff and ensure it's invoked
        boolean stopped = evictor.stop();

        assertThat(stopped, is(true));
        assertThat(evictor.isAlive(), is(false));

        verify(mockStrategyOne, times(10)).executeEviction();
        verify(mockStrategyTwo, times(10)).executeEviction();
    }
}
