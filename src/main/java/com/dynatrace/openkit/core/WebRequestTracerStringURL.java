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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.protocol.Beacon;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * Setting the Dynatrace tag to the {@link OpenKitConstants#WEBREQUEST_TAG_HEADER} HTTP header has to be done manually by the user.
 * Inherited class of {@link WebRequestTracerBaseImpl} which can be used for tracing and timing of a web request handled by any 3rd party HTTP Client.
 */
public class WebRequestTracerStringURL extends WebRequestTracerBaseImpl {

    private static final Pattern SCHEMA_VALIDATION_PATTERN = Pattern.compile("^[a-z][a-z0-9+\\-.]*://.+", Pattern.CASE_INSENSITIVE);

    // *** constructors ***

    // creates web request tracer with a simple string URL
    public WebRequestTracerStringURL(Logger logger, Beacon beacon, ActionImpl action, String url) {
        super(logger, beacon, action);

        // separate query string from URL
        if (isValidURLScheme(url)) {
            this.url = url.split("\\?")[0];
        }
    }

    static boolean isValidURLScheme(String url) {
        return url != null && SCHEMA_VALIDATION_PATTERN.matcher(url).matches();
    }
}
