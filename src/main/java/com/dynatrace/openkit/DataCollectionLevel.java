/**
 * Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit;

import com.dynatrace.openkit.protocol.SerializableBeaconValue;

/**
 * Specifies the granularity of which/how much data is collected/sent.
 */
public enum DataCollectionLevel implements SerializableBeaconValue {
    /**
     * No data will be collected at all
     */
    OFF(0),
    /**
     * Only performance related data will be collected
     */
    PERFORMANCE(1),
    /**
     * All available RUM (real user monitoring) data, including performance related data, is collected.
     */
    USER_BEHAVIOR(2);

    private final int intValue;

    DataCollectionLevel(int intValue){
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    public String asBeaconValue() {
        return String.valueOf(intValue);
    }

    public static DataCollectionLevel defaultValue() {
        return USER_BEHAVIOR;
    }
}

