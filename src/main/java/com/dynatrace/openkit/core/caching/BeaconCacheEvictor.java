package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class BeaconCacheEvictor {

    private static final String THREAD_NAME = BeaconCacheEvictor.class.getSimpleName();
    private static final long EVICTION_THREAD_JOIN_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    private final Logger logger;
    private final Thread evictionThread;

    public BeaconCacheEvictor(Logger logger,
                              BeaconCacheImpl beaconCache,
                              BeaconCacheConfiguration configuration,
                              TimingProvider timingProvider) {
        this.logger = logger;
        BeaconCacheEvictionStrategy timeEviction = new BeaconCacheTimeEviction(logger, beaconCache, configuration, timingProvider);
        BeaconCacheEvictionStrategy spaceEviction = new BeaconCacheSpaceEviction(logger, beaconCache, configuration);

        evictionThread = new Thread(new CacheEvictionRunnable(logger, beaconCache, timeEviction, spaceEviction), THREAD_NAME);
    }

    public synchronized void start() {
        if (!evictionThread.isAlive()) {
            evictionThread.start();
            if (logger.isDebugEnabled()) {
                logger.debug("BeaconCacheEviction thread started.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not starting BeaconCacheEviction thread, since it's already running");
            }
        }
    }

    public void stop() {
        stop(EVICTION_THREAD_JOIN_TIMEOUT);
    }

    public synchronized void stop(long timeout) {

        if (evictionThread.isAlive()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping BeaconCacheEviction thread.");
            }
            evictionThread.interrupt();
            try {
                evictionThread.join(timeout);
            } catch (InterruptedException e) {
                logger.warning("Stopping BeaconCacheEviction thread was interrupted.");
                Thread.currentThread().interrupt(); // re-interrupt the current thread
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not stopping BeaconCacheEviction thread, since it's not alive");
            }
        }
    }

    /**
     * Beacon cache eviction thread runnable.
     */
    private static final class CacheEvictionRunnable implements Runnable, Observer {

        private final Logger logger;
        private final Object lockObject = new Object();
        private boolean recordAdded = false;
        private final BeaconCacheImpl beaconCache;
        private final BeaconCacheEvictionStrategy[] strategies;

        CacheEvictionRunnable(Logger logger, BeaconCacheImpl beaconCache, BeaconCacheEvictionStrategy... strategies) {
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
                        while(!recordAdded) {
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
                    strategy.executeEviction();
                }
            }
        }

        @Override
        public void update(Observable o, Object arg) {

            if (!o.equals(beaconCache)) {
                return;
            }

            synchronized (lockObject) {
                recordAdded = true;
                lockObject.notifyAll();
            }
        }
    }

}
