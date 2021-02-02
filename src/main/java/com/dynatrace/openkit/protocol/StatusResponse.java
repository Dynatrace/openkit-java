/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements a status response which is sent for the request types status check and beacon send.
 */
public class StatusResponse {

    /**
     * Response code sent by HTTP server to indicate success.
     */
    public static final int HTTP_OK = 200;

    /**
     * Bad request is error code 400 in the HTTP protocol.
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * Too many requests sent by client (rate limiting) error code.
     */
    public static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * Key in the HTTP response headers for Retry-After
     */
    static final String RESPONSE_KEY_RETRY_AFTER = "retry-after";

    /**
     * Default "Retry-After" is 10 minutes.
     */
    static final long DEFAULT_RETRY_AFTER_IN_MILLISECONDS = 10L * 60L * 1000L;

    /**
     * Value that is sent if response status indicates an error.
     *
     * <p>
     *     The status is part of the payload {@link ResponseAttributes}.
     * </p>
     */
    static final String RESPONSE_STATUS_ERROR = "ERROR";

    private final Logger logger;

    private final int responseCode;
    private final Map<String, List<String>> headers;

    private final ResponseAttributes responseAttributes;

    // *** constructors ***

    private StatusResponse(Logger logger, ResponseAttributes responseAttributes, int responseCode, Map<String, List<String>> headers) {
        this.logger = logger;
        this.responseAttributes = responseAttributes;
        this.responseCode = responseCode;
        this.headers = responseHeadersWithLowerCaseKeys(headers);
    }

    private static Map<String, List<String>> responseHeadersWithLowerCaseKeys(Map<String, List<String>> headers) {
        Map<String, List<String>> result = new HashMap<String, List<String>>(headers.size());
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    public static StatusResponse createSuccessResponse(
            Logger logger,
            ResponseAttributes responseAttributes,
            int responseCode,
            Map<String, List<String>> headers) {
        return new StatusResponse(logger, responseAttributes, responseCode, headers);
    }

    public static StatusResponse createErrorResponse(Logger logger, int responseCode) {
        return createErrorResponse(logger, responseCode, new HashMap<String, List<String>>());
    }

    public static StatusResponse createErrorResponse(Logger logger, int responseCode, Map<String, List<String>> headers) {
        ResponseAttributes responseAttributes = ResponseAttributesImpl.withUndefinedDefaults().build();
        return new StatusResponse(logger, responseAttributes, responseCode, headers);
    }

    public boolean isErroneousResponse() {
        return getResponseCode() >= HTTP_BAD_REQUEST || isStatusSetToError();
    }

    private boolean isStatusSetToError() {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.STATUS)) {
            // no status sent - therefore no error
            return false;
        }

        return RESPONSE_STATUS_ERROR.equals(responseAttributes.getStatus());
    }

    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Returns the attributes received as response from the server.
     */
    public ResponseAttributes getResponseAttributes() {
        return responseAttributes;
    }

    public long getRetryAfterInMilliseconds() {

        List<String> values = getHeaders().get(RESPONSE_KEY_RETRY_AFTER);
        if (values == null) {
            // the Retry-After response header is missing
            logger.warning(RESPONSE_KEY_RETRY_AFTER + " is not available - using default value " + DEFAULT_RETRY_AFTER_IN_MILLISECONDS);
            return DEFAULT_RETRY_AFTER_IN_MILLISECONDS;
        }

        if (values.size() != 1) {
            // the Retry-After response header has multiple values, but only one is expected
            logger.warning(RESPONSE_KEY_RETRY_AFTER + " has unexpected number of values - using default value " + DEFAULT_RETRY_AFTER_IN_MILLISECONDS);
            return DEFAULT_RETRY_AFTER_IN_MILLISECONDS;
        }

        // according to RFC 7231 Section 7.1.3 (https://tools.ietf.org/html/rfc7231#section-7.1.3)
        // Retry-After value can either be a delay seconds value, which is a non-negative decimal integer
        // or it is an HTTP date.
        // Our implementation assumes only delay seconds value here
        int delaySeconds;
        try {
            delaySeconds = Integer.parseInt(values.get(0));
        } catch (NumberFormatException e) {
            logger.error("Failed to parse " + RESPONSE_KEY_RETRY_AFTER + " value \"" + values.get(0)
                    + "\" - using default value " + DEFAULT_RETRY_AFTER_IN_MILLISECONDS);
            return DEFAULT_RETRY_AFTER_IN_MILLISECONDS;
        }

        // convert delay seconds to milliseconds
        return delaySeconds * 1000L;
    }

    Map<String, List<String>> getHeaders() {
        return headers;
    }
}
