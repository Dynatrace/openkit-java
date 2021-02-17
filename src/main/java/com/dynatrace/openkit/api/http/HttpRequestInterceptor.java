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

/**
 * An interface allowing to intercept an HTTP request, before it is sent to the backend system.
 *
 * <p>
 *     This interceptor is only applied to HTTP requests which are sent to Dynatrace backends.
 * </p>
 */
public interface HttpRequestInterceptor {

    /**
     * Intercept the HTTP request and manipulate it.
     *
     * <p>
     *     Currently it's only possible to set custom HTTP headers.
     * </p>
     *
     * @param httpRequest The HTTP request to Dynatrace backend.
     */
    void intercept(HttpRequest httpRequest);
}
