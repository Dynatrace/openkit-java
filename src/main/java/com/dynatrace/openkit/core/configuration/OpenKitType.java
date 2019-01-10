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
 * This enum defines if an OpenKit instance should be used for AppMon or Dynatrace.
 */
public enum OpenKitType {

    APPMON(1),      // AppMon: default monitor URL name contains "dynaTraceMonitor" and default Server ID is 1
    DYNATRACE(1);   // Dynatrace: default monitor URL name contains "mbeacon" and default Server ID is 1

    private int defaultServerID;

    OpenKitType(int defaultServerID) {
        this.defaultServerID = defaultServerID;
    }

    public int getDefaultServerID() {
        return defaultServerID;
    }
}