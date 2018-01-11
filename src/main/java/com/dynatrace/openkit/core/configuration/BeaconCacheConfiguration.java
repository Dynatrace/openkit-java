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

/**
 * Configuration for beacon cache.
 */
public class BeaconCacheConfiguration {

    private long maxRecordAge;
    private int cacheSizeLowerBound;
    private int cacheSizeUpperBound;

    /**
     * Constructor
     *
     * @param maxRecordAge Maximum record age
     * @param cacheSizeLowerBound lower memory limit for cache
     * @param cacheSizeUpperBound upper memory limit for cache
     */
    public BeaconCacheConfiguration(long maxRecordAge, int cacheSizeLowerBound, int cacheSizeUpperBound) {

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
    public int getCacheSizeLowerBound() {
        return cacheSizeLowerBound;
    }

    /**
     * Get upper memory limit for cache.
     */
    public int getCacheSizeUpperBound() {
        return cacheSizeUpperBound;
    }
}
