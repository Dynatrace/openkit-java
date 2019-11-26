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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Space based eviction strategy for the beacon cache.
 *
 * <p>
 * This strategy checks if the number of cached bytes is greater than {@link BeaconCacheConfiguration#getCacheSizeLowerBound()}
 * and in this case runs the strategy.
 * </p>
 */
class SpaceEvictionStrategy implements BeaconCacheEvictionStrategy {

    private final Logger logger;
    private final BeaconCache beaconCache;
    private final BeaconCacheConfiguration configuration;

    private boolean infoShown = false;

    /**
     * Constructor.
     *
     * @param logger Instance implementing the {@link Logger} interface for writing some useful debug messages.
     * @param beaconCache The beacon cache to evict if necessary.
     * @param configuration The configuration providing the boundary settings for this strategy.
     */
    SpaceEvictionStrategy(Logger logger, BeaconCache beaconCache, BeaconCacheConfiguration configuration) {
        this.logger = logger;
        this.beaconCache = beaconCache;
        this.configuration = configuration;
    }

    @Override
    public void execute() {

        if (isStrategyDisabled()) {
            // immediately return if this strategy is disabled
            if (!infoShown && logger.isInfoEnabled()) {
                logger.info(getClass().getSimpleName() + " execute() - strategy is disabled");
                // suppress any further log output
                infoShown = true;
            }
            return;
        }

        if (shouldRun()) {
            doExecute();
        }
    }

    /**
     * Checks if the strategy is disabled.
     *
     * <p>
     * The strategy might be disabled on purpose, if upper and lower boundary are less than or equal to 0
     * or accidentally, when either lower or upper boundary is less than or equal to zero or upper boundary is less
     * than lower boundary.
     * </p>
     *
     * @return {@code true} if strategy is disabled, {@code false} otherwise.
     */
    boolean isStrategyDisabled() {
        return configuration.getCacheSizeLowerBound() <= 0
            || configuration.getCacheSizeUpperBound() <= 0
            || configuration.getCacheSizeUpperBound() < configuration.getCacheSizeLowerBound();
    }

    /**
     * Checks if the strategy should run.
     *
     * <p>
     * The strategy should run, if the currently stored number of bytes in the Beacon cache exceeds the configured
     * upper limit.
     * </p>
     *
     * @return {@code true} if the strategy should run, {@code false} otherwise.
     */
    boolean shouldRun() {

        return beaconCache.getNumBytesInCache() > configuration.getCacheSizeUpperBound();
    }

    /**
     * Performs execution of strategy.
     */
    private void doExecute() {

        Map<BeaconKey, Integer> removedRecordsPerBeacon = new HashMap<BeaconKey, Integer>();

        while (!Thread.currentThread().isInterrupted()
            && beaconCache.getNumBytesInCache() > configuration.getCacheSizeLowerBound()) {

            Set<BeaconKey> beaconKeys = beaconCache.getBeaconKeys();

            Iterator<BeaconKey> iterator = beaconKeys.iterator();
            while (!Thread.currentThread().isInterrupted()
                && iterator.hasNext()
                && beaconCache.getNumBytesInCache() > configuration.getCacheSizeLowerBound()) {

                BeaconKey beaconKey = iterator.next();

                // remove 1 record from Beacon cache for given beaconKey
                // the result is the number of records removed, which might be in range [0, numRecords=1]
                int numRecordsRemoved = beaconCache.evictRecordsByNumber(beaconKey, 1);

                if (logger.isDebugEnabled()) {
                    if (!removedRecordsPerBeacon.containsKey(beaconKey)) {
                        removedRecordsPerBeacon.put(beaconKey, numRecordsRemoved);
                    } else {
                        removedRecordsPerBeacon.put(beaconKey, removedRecordsPerBeacon.get(beaconKey) + numRecordsRemoved);
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            for (Map.Entry<BeaconKey, Integer> entries : removedRecordsPerBeacon.entrySet()) {
                logger.debug(getClass().getSimpleName() + " doExecute()  - Removed " + entries.getValue() + " records from Beacon with key " + entries.getKey());
            }
        }
    }
}
