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

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

import java.util.concurrent.TimeUnit;

/**
 * Class containing all default values for all configurations.
 */
public class ConfigurationDefaults {

    /**
     * The default {@link BeaconCacheConfiguration} when user does not override it.
     *
     * Default settings allow beacons which are max 2 hours old and unbounded memory limits.
     */
    public static final long DEFAULT_MAX_RECORD_AGE_IN_MILLIS = TimeUnit.MINUTES.toMillis(105); // 1hour and 45 minutes
    /**
     * Defines the default upper memory boundary of the {@link com.dynatrace.openkit.core.caching.BeaconCache}.
     *
     * <p>
     *     The upper boundary is the size limit at which the {@link com.dynatrace.openkit.core.caching.BeaconCache}
     *     will start evicting records. The default uppper boundary is 100 MB
     * </p>
     */
    public static final long DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES = 100L * 1024L * 1024L;             // 100 MiB
    /**
     * Defines the lower memory boundary of the {@link com.dynatrace.openkit.core.caching.BeaconCache}
     *
     * <p>
     *     The lower boundary is the size until which the {@link com.dynatrace.openkit.core.caching.BeaconCache} will
     *     evict records once the upper boundary was exceeded. The default lower boundary is 80 MB
     * </p>
     */
    public static final long DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES = 80L * 1024L * 1024L;              // 80 MiB

    /** Default data collection level used, if no other value was specified */
    public static final DataCollectionLevel DEFAULT_DATA_COLLECTION_LEVEL = DataCollectionLevel.defaultValue();
    /** Default crash reporting level used, if no other value was specified */
    public static final CrashReportingLevel DEFAULT_CRASH_REPORTING_LEVEL = CrashReportingLevel.defaultValue();

    private ConfigurationDefaults() {
    }

}
