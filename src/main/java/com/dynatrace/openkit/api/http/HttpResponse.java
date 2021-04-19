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

package com.dynatrace.openkit.api.http;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Interface representing an HTTP response.
 */
public interface HttpResponse {

    /**
     * Gets the request {@link URL} associated with this response.
     */
    URL getRequestUrl();

    /**
     * Gets the request method associated with this response.
     */
    String getRequestMethod();

    /**
     * Gets the HTTP response code.
     *
     * <p>
     *     If reading the HTTP status line fails, {@link Integer#MIN_VALUE} is returned.
     * </p>
     *
     * @return HTTP response code returned by server, or a negative value in case of an error.
     */
    int getResponseCode();

    /**
     * Gets the HTTP response message.
     *
     * <p>
     *     If reading the HTTP status line fails, {@code null} is returned.
     * </p>
     *
     * @return HTTP response message returned by server or {@code null}.
     */
    String getResponseMessage();

    /**
     * Gets the HTTP response headers and their values.
     *
     * @return An immutable map of HTTP response headers mapping to their values.
     */
    Map<String, List<String>> getHeaders();

    /**
     * Gets the value of an HTTP response header.
     *
     * <p>
     *     If the header occurs multiple times, it depends on the underlying implementation
     *     which value is returned.
	 *
	 *     In this case prefer {@link #getHeaders()} instead.
     * </p>
     *
     * @return Value associated with HTTP response header {@code name} or {@code null}.
     */
    String getHeader(String name);
}
