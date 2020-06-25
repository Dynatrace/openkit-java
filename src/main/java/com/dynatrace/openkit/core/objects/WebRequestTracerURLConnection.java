/**
 * Copyright 2018-2020 Dynatrace LLC
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

import java.net.URLConnection;

/**
 * Inherited class of {@link WebRequestTracerBaseImpl} which can be used for tracing and timing of a web request provided as a URLConnection.
 */
public class WebRequestTracerURLConnection extends WebRequestTracerBaseImpl {

    /**
     * Creates web request tag with a URLConnection
     *
     * <p>
     *     The required Dynatrace tag is applied automatically, if not done yet.
     * </p>
     *
     * @param logger The logger used to log information
     * @param parent The parent object, to which this web request tracer belongs to
     * @param beacon {@link Beacon} for data sending and tag creation
     * @param connection The URL connection to trace
     */
    public WebRequestTracerURLConnection(Logger logger,
                                         OpenKitComposite parent,
                                         Beacon beacon,
                                         URLConnection connection) {
        super(logger, parent, extractURLParts(connection), beacon);
        setTagOnConnection(connection);
    }

    /**
     * Extract URL parts of interest from given url connection.
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
     * @param connection The url connection from which to get the URL to trace
     * @return The {@code url} substring containing scheme, host, port, path
     */
    private static String extractURLParts(URLConnection connection) {
        if (connection == null || connection.getURL() == null) {
            return WebRequestTracerBaseImpl.UNKNOWN_URL;
        }

        return connection.getURL().toString().split("\\?", 2)[0];
    }

    /**
     * Set the Dynatrace tag on the provided URLConnection
     *
     * @param connection The URL connection.
     */
    private void setTagOnConnection(URLConnection connection) {
        if (connection == null) {
            return;
        }

        // check if header is already set
        String existingTag = connection.getRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER);
        if (existingTag == null) {
            // if not yet set -> set it now
            try {
                connection.setRequestProperty(OpenKitConstants.WEBREQUEST_TAG_HEADER, getTag());
            } catch (Exception e) {
                // if it does not work -> simply ignore
            }
        }
    }
}
