/**
 * Copyright 2018-2021 Dynatrace LLC
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

import com.dynatrace.openkit.AbstractOpenKitBuilder;

/**
 * Configuration for beacon cache.
 */
public class BeaconCacheConfiguration {

    private final long maxRecordAge;
    private final long cacheSizeLowerBound;
    private final long cacheSizeUpperBound;

    /**
     * Constructor
     *
     * @param builder OpenKit builder storing all necessary configuration information.
     */
    private BeaconCacheConfiguration(AbstractOpenKitBuilder builder) {
        this.maxRecordAge = builder.getBeaconCacheMaxRecordAge();
        this.cacheSizeLowerBound = builder.getBeaconCacheLowerMemoryBoundary();
        this.cacheSizeUpperBound = builder.getBeaconCacheUpperMemoryBoundary();
    }

    /**
     * Create a {@link BeaconCacheConfiguration} from given {@link AbstractOpenKitBuilder}.
     *
     * @param builder The OpenKit builder for which to create a {@link BeaconCacheConfiguration}.
     * @return Newly created {@link BeaconCacheConfiguration} or {@code null} if given argument is {@code null}
     */
    public static BeaconCacheConfiguration from(AbstractOpenKitBuilder builder) {
        if (builder == null) {
            return null;
        }
        return new BeaconCacheConfiguration(builder);
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
