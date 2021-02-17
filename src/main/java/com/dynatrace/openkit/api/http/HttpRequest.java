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
 * Interface representing an HTTP request.
 */
public interface HttpRequest {

    /**
     * Gets the HTTP request {@link URL}.
     */
    URL getUrl();

    /**
     * Gets the HTTP request method.
     */
    String getMethod();

    /**
     * Gets an immutable map containing the request headers and their values.
     */
    Map<String, List<String>> getHeaders();

    /**
     * Gets the header's value.
     *
     * @param name Header name for which to retrieve the value.
     *
     * @return The header's value, which might also be {@code null} if not set.
     */
    String getHeader(String name);

    /**
     * Sets an HTTP header or overwrites an existing HTTP header with new value.
     * <p>
     *     Trying to set an HTTP header with null name will return immediately.
     *     Trying to set one of the following restricted headers will also return immediately.
     * </p>
     * <ul>
     *     <li>{@code Access-Control-Request-Headers}</li>
     *     <li>{@code Access-Control-Request-Method}</li>
     *     <li>{@code Connection}</li>
     *     <li>{@code Content-Length}</li>
     *     <li>{@code Content-Transfer-Encoding}</li>
     *     <li>{@code Host}</li>
     *     <li>{@code Keep-Alive}</li>
     *     <li>{@code Origin}</li>
     *     <li>{@code Trailer}</li>
     *     <li>{@code Transfer-Encoding}</li>
     *     <li>{@code Upgrade}</li>
     *     <li>{@code Via}</li>
     * </ul>
     *
     * @param name The header's name, which must not be {@code null} or any of the restricted headers.
     * @param value The header's value
     */
    void setHeader(String name, String value);
}
