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
 * Specifies the level at which crashes are reported to the server.
 */
public enum CrashReportingLevel implements SerializableBeaconValue {
    /**
     * Crashes are not sent to the server
     */
    OFF(0),
    /**
     * Crashes are not sent to the server
     */
    OPT_OUT_CRASHES(1),
    /**
     * Crashes are sent to the server
     */
    OPT_IN_CRASHES(2);

    private final int intValue;

    CrashReportingLevel(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    public String asBeaconValue() {
        return String.valueOf(intValue);
    }

    public static CrashReportingLevel defaultValue() {
        return OPT_IN_CRASHES;
    }
}
