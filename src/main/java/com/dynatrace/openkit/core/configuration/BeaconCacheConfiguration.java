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

package com.dynatrace.openkit.core.configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for beacon cache.
 */
public class BeaconCacheConfiguration {

    /**
     * The default {@link BeaconCacheConfiguration} when user does not override it.
     *
     * Default settings allow beacons which are max 2 hours old and unbounded memory limits.
     */
    public static final long DEFAULT_MAX_RECORD_AGE_IN_MILLIS = TimeUnit.MINUTES.toMillis(105); // 1hour and 45 minutes
    public static final long DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES = 100 * 1024 * 1024;                // 100 MiB
    public static final long DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES = 80 * 1024 * 1024;                 // 80 MiB

    private final long maxRecordAge;
    private final long cacheSizeLowerBound;
    private final long cacheSizeUpperBound;

    /**
     * Constructor
     *
     * @param maxRecordAge Maximum record age
     * @param cacheSizeLowerBound lower memory limit for cache
     * @param cacheSizeUpperBound upper memory limit for cache
     */
    public BeaconCacheConfiguration(long maxRecordAge, long cacheSizeLowerBound, long cacheSizeUpperBound) {

        this.maxRecordAge = maxRecordAge;
        this.cacheSizeLowerBound = cacheSizeLowerBound;
        this.cacheSizeUpperBound = cacheSizeUpperBound;
    }

    /**
     * Get maximum record age.
     */
    public long getMaxRecordAge() {
        return maxRecordAge;
    }

    /**
     * Get lower memory limit for cache.
     */
    public long getCacheSizeLowerBound() {
        return cacheSizeLowerBound;
    }

    /**
     * Get upper memory limit for cache.
     */
    public long getCacheSizeUpperBound() {
        return cacheSizeUpperBound;
    }
}
