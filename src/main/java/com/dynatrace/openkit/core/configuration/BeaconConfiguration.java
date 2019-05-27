/**
 * Copyright 2018-2019 Dynatrace LLC
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

    public static final int DEFAULT_MULITPLICITY = 1;

    /**
     * Multiplicity as received from the server.
     */
    private final int multiplicity;

    /**
     * Default constructor using default values for data collection levels
     */
    public BeaconConfiguration() {
        this(DEFAULT_MULITPLICITY);
    }

    /**
     * Constructor
     * @param multiplicity multiplicity as returned by the server
     */
    public BeaconConfiguration(int multiplicity) {
        this.multiplicity = multiplicity;
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
