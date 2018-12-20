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

package com.dynatrace.openkit.api;

/**
 * Defines constant values used in OpenKit
 */
public class OpenKitConstants {


    /**
     * Explicit default constructor to hide implicit public one.
     */
    private OpenKitConstants() {
        throw new IllegalStateException("constants class");
    }

    /**
     * Name of Dynatrace HTTP header which is used for tracing web requests.
     */
    public static final String WEBREQUEST_TAG_HEADER = "X-dynaTrace";

    // default values used in configuration
    public static final String DEFAULT_APPLICATION_VERSION = "1.1.4";
    public static final String DEFAULT_OPERATING_SYSTEM = "OpenKit " + DEFAULT_APPLICATION_VERSION;
    public static final String DEFAULT_MANUFACTURER = "Dynatrace";
    public static final String DEFAULT_MODEL_ID = "OpenKitDevice";

}
