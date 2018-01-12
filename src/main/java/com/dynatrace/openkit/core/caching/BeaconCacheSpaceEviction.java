package com.dynatrace.openkit.core.caching;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;

import java.util.Iterator;
import java.util.Set;

/**
 * Space based eviction strategy for the beacon cache.
 */
class BeaconCacheSpaceEviction implements BeaconCacheEvictionStrategy {

    private final Logger logger;
    private final BeaconCache beaconCache;
    private final BeaconCacheConfiguration configuration;

    private boolean disabledInfoShown = false;

    BeaconCacheSpaceEviction(Logger logger, BeaconCache beaconCache, BeaconCacheConfiguration configuration) {
        this.logger = logger;
        this.beaconCache = beaconCache;
        this.configuration = configuration;
    }

    @Override
    public void executeEviction() {

        if (isStrategyDisabled()) {
            // immediately return if this strategy is disabled
            if (!disabledInfoShown && logger.isInfoEnabled()) {
                logger.info("BeaconCacheSpaceEviction is disabled");
                // suppress any further log output
                disabledInfoShown = true;
            }
            return;
        }

        if (shouldRun()) {
            doExecute();
        }

    }

    boolean isStrategyDisabled() {
        return configuration.getCacheSizeLowerBound() <= 0
            || configuration.getCacheSizeUpperBound() <= 0
            || configuration.getCacheSizeUpperBound() < configuration.getCacheSizeLowerBound();
    }

    boolean shouldRun() {

        return beaconCache.getNumBytesInCache() > configuration.getCacheSizeUpperBound();
    }

    private void doExecute() {


        while (!Thread.currentThread().isInterrupted() && beaconCache.getNumBytesInCache() > configuration.getCacheSizeLowerBound()) {

            if (logger.isDebugEnabled()) {
                logger.debug("BeaconCacheSpaceEviction: Current cache");
            }

            Set<Integer> beaconIDs = beaconCache.getBeaconIDs();

            Iterator<Integer> iterator = beaconIDs.iterator();
            while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {

                Integer beaconID = iterator.next();

                beaconCache.evictRecordsByNumber(beaconID, 1);
            }
        }
    }
}
