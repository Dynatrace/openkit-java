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

import java.util.Iterator;
import java.util.Set;

/**
 * Time based eviction strategy for the beacon cache.
 */
class BeaconCacheTimeEviction implements BeaconCacheEvictionStrategy {

    private final Logger logger;
    private final BeaconCache beaconCache;
    private final BeaconCacheConfiguration configuration;
    private final TimingProvider timingProvider;

    private long lastRunTimestamp = -1;
    private boolean disabledInfoShown = false;

    BeaconCacheTimeEviction(Logger logger, BeaconCache beaconCache, BeaconCacheConfiguration configuration, TimingProvider timingProvider) {
        this.logger = logger;
        this.beaconCache = beaconCache;
        this.configuration = configuration;
        this.timingProvider = timingProvider;
    }

    @Override
    public void executeEviction() {

        if (isStrategyDisabled()) {
            // immediately return if this strategy is disabled
            if (!disabledInfoShown && logger.isInfoEnabled()) {
                logger.info("BeaconCacheTimeEviction is disabled");
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
        return configuration.getMaxRecordAge() <= 0;
    }

    boolean shouldRun() {
        // if delta since we last ran is >= the maximum age, we should run, otherwise this run can be skipped
        long currentTimestamp = timingProvider.provideTimestampInMilliseconds();
        return (currentTimestamp - lastRunTimestamp) >= configuration.getMaxRecordAge();
    }

    long getLastRunTimestamp() {
        return lastRunTimestamp;
    }

    void setLastRunTimestamp(long lastRunTimestamp) {
        this.lastRunTimestamp = lastRunTimestamp;
    }

    private void doExecute() {

        // first get a snapshot of all inserted beacons
        Set<Integer> beaconIDs = beaconCache.getBeaconIDs();
        if (beaconIDs.isEmpty()) {
            return;
        }

        // retrieve the timestamp when we start with execution
        long currentTimestamp = timingProvider.provideTimestampInMilliseconds();
        long smallestAllowedBeaconTimestamp = currentTimestamp - configuration.getMaxRecordAge();

        // iterate over the previously obtained set and evict for each beacon
        Iterator<Integer> beaconIDIterator = beaconIDs.iterator();
        while (!Thread.currentThread().isInterrupted() && beaconIDIterator.hasNext()) {
            Integer beaconID = beaconIDIterator.next();
            int numRecordsRemoved = beaconCache.evictRecordsByAge(beaconID, smallestAllowedBeaconTimestamp);
            if (logger.isDebugEnabled()) {
                logger.debug("Removed " + numRecordsRemoved + " records from Beacon with ID " + beaconID);
            }
        }

        // last but not least update the last runtime
        setLastRunTimestamp(currentTimestamp);
    }
}
