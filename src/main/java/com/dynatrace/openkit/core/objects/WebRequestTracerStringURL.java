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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.protocol.Beacon;

import java.util.regex.Pattern;

/**
 * Inherited class of {@link WebRequestTracerBaseImpl} which can be used for tracing and timing of a web request handled by any 3rd party HTTP Client.
 *
 * <p>
 *     Setting the Dynatrace tag to the {@link OpenKitConstants#WEBREQUEST_TAG_HEADER} HTTP header has to be done manually by the user.
 * </p>
 */
public class WebRequestTracerStringURL extends WebRequestTracerBaseImpl {

    private static final Pattern SCHEMA_VALIDATION_PATTERN = Pattern.compile("^[a-z][a-z0-9+\\-.]*://.+", Pattern.CASE_INSENSITIVE);

    /**
     * Creates web request tracer with a simple string URL
     *
     * @param logger The logger used to log information
     * @param parent The parent object, to which this web request tracer belongs to
     * @param beacon {@link Beacon} for data sending and tag creation
     * @param url The URL to trace
     */
    public WebRequestTracerStringURL(Logger logger,
                                     OpenKitComposite parent,
                                     Beacon beacon,
                                     String url) {
        super(logger, parent, extractURLParts(url), beacon);
    }

    /**
     * Extract URL parts of interest from given url.
     *
     * <p>
     *     The URL parts of interest are
     *     <ul>
     *         <li>scheme</li>
     *         <li>host</li>
     *         <li>port</li>
     *         <li>path</li>
     *     </ul>
     * </p>
     *
     * @param url The URL to trace, including all possible components
     * @return The {@code url} substring containing scheme, host, port, path
     */
    private static String extractURLParts(String url) {
        // separate query string from URL
        if (isValidURLScheme(url)) {
            return url.split("\\?", 2)[0];
        }

        return WebRequestTracerBaseImpl.UNKNOWN_URL;
    }

    /**
     * Tests if given {@code url} has a valid URL scheme.
     *
     * @param url The URL to test
     * @return {@code true} if the URL scheme is valid, {@code false} otherwise.
     */
    static boolean isValidURLScheme(String url) {
        return url != null && SCHEMA_VALIDATION_PATTERN.matcher(url).matches();
    }
}
