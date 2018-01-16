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

        while (!Thread.currentThread().isInterrupted()
            && beaconCache.getNumBytesInCache() > configuration.getCacheSizeLowerBound()) {

            Set<Integer> beaconIDs = beaconCache.getBeaconIDs();

            Iterator<Integer> iterator = beaconIDs.iterator();
            while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {

                Integer beaconID = iterator.next();

                int numRecordsRemoved = beaconCache.evictRecordsByNumber(beaconID, 1);
                if (logger.isDebugEnabled()) {
                    logger.debug("Removed " + numRecordsRemoved + " records from Beacon with ID " + beaconID);
                }
            }
        }
    }
}
