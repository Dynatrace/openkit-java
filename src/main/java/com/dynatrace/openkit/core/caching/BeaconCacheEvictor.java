/**
 * Copyright 2018 Dynatrace LLC
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

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

/**
 * Class responsible for handling an eviction thread, to ensure BeaconCache stays in configured boundaries.
 */
public class BeaconCacheEvictor {

    private static final String THREAD_NAME = BeaconCacheEvictor.class.getSimpleName();
    private static final long EVICTION_THREAD_JOIN_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    private final Logger logger;
    private final Thread evictionThread;

    /**
     * Public constructor, initializing the eviction thread with the default
     * {@link TimeEvictionStrategy} and {@link SpaceEvictionStrategy} strategies.
     *
     * @param logger         Logger to write some debug output
     * @param beaconCache    The Beacon cache to check if entries need to be evicted
     * @param configuration  Beacon cache configuration
     * @param timingProvider Timing provider required for time retrieval
     */
    public BeaconCacheEvictor(Logger logger, BeaconCache beaconCache, BeaconCacheConfiguration configuration, TimingProvider timingProvider) {

        this(logger, beaconCache, new TimeEvictionStrategy(logger, beaconCache, configuration, timingProvider), new SpaceEvictionStrategy(logger, beaconCache, configuration));
    }

    /**
     * Internal testing constructor.
     *
     * @param logger      Logger to write some debug output
     * @param beaconCache The Beacon cache to check if entries need to be evicted
     * @param strategies  Strategies passed to the actual Runnable.
     */
    BeaconCacheEvictor(Logger logger, BeaconCache beaconCache, BeaconCacheEvictionStrategy... strategies) {

        this.logger = logger;
        evictionThread = new Thread(new CacheEvictionRunnable(logger, beaconCache, strategies), THREAD_NAME);
    }

    /**
     * Starts the eviction thread.
     *
     * @return {@code true} if the eviction thread was started, {@code false} if the thread was already running.
     */
    public synchronized boolean start() {
        boolean result = false;

        if (!isAlive()) {
            evictionThread.start();
            if (logger.isDebugEnabled()) {
                logger.debug("BeaconCacheEviction thread started.");
            }
            result = true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not starting BeaconCacheEviction thread, since it's already running");
            }
        }

        return result;
    }

    /**
     * Stops the eviction thread and joins with {@link #EVICTION_THREAD_JOIN_TIMEOUT}.
     *
     * <p>
     * See also {@link #stop(long)}.
     * </p>
     */
    public boolean stop() {
        return stop(EVICTION_THREAD_JOIN_TIMEOUT);
    }

    /**
     * Stops the eviction thread via {@link Thread#interrupt()}, if it's alive and joins the eviction thread with given {@code timeout}.
     *
     * @param timeout The number of milliseconds to join the thread.
     *
     * @return {@code true} if stopping was successful, {@code false} if eviction thread is not running
     * or could not be stopped in time.
     */
    public synchronized boolean stop(long timeout) {
        boolean result = false;

        if (isAlive()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping BeaconCacheEviction thread.");
            }
            evictionThread.interrupt();
            try {
                evictionThread.join(timeout);
                result = !isAlive();
            } catch (InterruptedException e) {
                logger.warn("Stopping BeaconCacheEviction thread was interrupted.");
                Thread.currentThread().interrupt(); // re-interrupt the current thread
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not stopping BeaconCacheEviction thread, since it's not alive");
            }
        }

        return result;
    }

    public boolean isAlive() {
        return evictionThread.isAlive();
    }

    /**
     * Beacon cache eviction thread runnable.
     */
    private static final class CacheEvictionRunnable implements Runnable, Observer {

        private final Logger logger;
        private final Object lockObject = new Object();
        private boolean recordAdded = false;
        private final BeaconCache beaconCache;
        private final BeaconCacheEvictionStrategy[] strategies;

        CacheEvictionRunnable(Logger logger, BeaconCache beaconCache, BeaconCacheEvictionStrategy... strategies) {
            this.logger = logger;
            this.beaconCache = beaconCache;
            this.strategies = strategies;
        }

        @Override
        public void run() {

            // first register ourselves
            beaconCache.addObserver(this);

            // run
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (lockObject) {
                    try {
                        while (!recordAdded) {
                            lockObject.wait();
                        }
                    } catch (InterruptedException e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Caught InterruptedException while waiting for new cache records ... Exiting");
                        }
                        Thread.currentThread().interrupt();
                        break;
                    }

                    // reset the added flag
                    recordAdded = false;
                }

                // a new record has been added to the cache
                // run all eviction strategies, to perform cache cleanup
                for (BeaconCacheEvictionStrategy strategy : strategies) {
                    strategy.execute();
                }
            }
        }

        @Override
        public void update(Observable o, Object arg) {
            synchronized (lockObject) {
                recordAdded = true;
                lockObject.notifyAll();
            }
        }
    }
}
