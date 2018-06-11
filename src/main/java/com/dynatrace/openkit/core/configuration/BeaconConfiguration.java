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

package com.dynatrace.openkit.core.configuration;

/**
 * Configuration for a Beacon.
 *
 * <p>
 *     Note: This class shall be immutable.
 *     It is perfectly valid to exchange the configuration over time.
 * </p>
 */
public class BeaconConfiguration {

    public static final DataCollectionLevel DEFAULT_DATA_COLLECTION_LEVEL = DataCollectionLevel.OFF;
    public static final CrashReportingLevel DEFAULT_CRASH_REPORTING_LEVEL = CrashReportingLevel.OFF;

    /**
     * Multiplicity as received from the server.
     */
    private final int multiplicity;

    /**
     *  Data collection level
     */
    private final DataCollectionLevel dataCollectionLevel;

    /**
     * Crash reporting level
     */
    private final CrashReportingLevel crashReportingLevel;

    /**
     * Default constructor using default values for data collection levels
     */
    public BeaconConfiguration() {
        this(1, DEFAULT_DATA_COLLECTION_LEVEL, DEFAULT_CRASH_REPORTING_LEVEL);
    }

    /**
     * Constructor
     * @param multiplicity multiplicity as returned by the server
     * @param dataCollectionLevel data collection level ( @see com.dynatrace.configuration.DataCollectionLevel )
     * @param crashReportingLevel crashReporting level ( @see com.dynatrace.configuration.CrashReportingLevel )
     */
    public BeaconConfiguration(int multiplicity, DataCollectionLevel dataCollectionLevel, CrashReportingLevel crashReportingLevel) {
        this.multiplicity = multiplicity;
        this.dataCollectionLevel = dataCollectionLevel;
        this.crashReportingLevel = crashReportingLevel;
    }

    /**
     * Get the data collection level
     * @return data collection level
     */
    public DataCollectionLevel getDataCollectionLevel() {
        return this.dataCollectionLevel;
    }

    /**
     * Get the crash reporting level
     * @return crash reporting level
     */
    public CrashReportingLevel getCrashReportingLevel(){
        return this.crashReportingLevel;
    }


    /**
     * Get the multiplicity
     * @return
     */
    public int getMultiplicity() {
        return multiplicity;
    }

    /**
     * Get a flag if capturing is allowed based on the value of mulitplicity
     * @return {@code true} if capturing is allowed {@code false} if not
     */
    public boolean isCapturingAllowed() {
        return multiplicity > 0;
    }
}
