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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.Response;

/**
 * Utility class for responses.
 */
class BeaconSendingResponseUtil {

    /**
     * Default constructor.
     *
     * <p>
     * This constructor is private since the class is handled as static class.
     * </p>
     */
    private BeaconSendingResponseUtil() {
    }

    /**
     * Test if the given {@link Response} is a successful response.
     *
     * @param response The given response to check whether it is successful or not.
     * @return {@code true} if response is successful, {@code false} otherwise.
     */
    static boolean isSuccessfulResponse(Response response) {

        return response != null && !response.isErroneousResponse();
    }

    /**
     * Test if the given {@link Response} is a "too many requests" response.
     *
     * <p>
     * A "too many requests" response is an HTTP response with response code 429.
     * </p>
     *
     * @param response The given response to check whether it is a "too many requests" response or not.
     * @return {@code true} if response indicates too many requests, {@code false} otherwise.
     */
    static boolean isTooManyRequestsResponse(Response response) {

        return response != null && response.getResponseCode() == Response.HTTP_TOO_MANY_REQUESTS;
    }
}
