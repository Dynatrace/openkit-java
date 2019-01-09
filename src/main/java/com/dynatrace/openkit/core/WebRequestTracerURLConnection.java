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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.protocol.Beacon;

import java.net.URL;
import java.net.URLConnection;

/**
 * Inherited class of {@link WebRequestTracerBaseImpl} which can be used for tracing and timing of a web request provided as a URLConnection.
 */
public class WebRequestTracerURLConnection extends WebRequestTracerBaseImpl {

    // *** constructors ***

    // creates web request tag with a URLConnection
    public WebRequestTracerURLConnection(Beacon beacon, ActionImpl action, URLConnection connection) {
        super(beacon, action);

        // only set tag header and URL if connection is not null
        if (connection != null) {
            setTagOnConnection(connection);

            // separate query string from URL
            URL connectionURL = connection.getURL();
            if (connectionURL != null) {
                this.url = connectionURL.toString().split("\\?")[0];
            }
        }
    }

    // *** private methods ***

    // set the Dynatrace tag on the provided URLConnection
    private void setTagOnConnection(URLConnection connection) {
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
