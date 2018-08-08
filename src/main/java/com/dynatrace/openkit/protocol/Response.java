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

package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Abstract base class for a response to one of the 3 request types (status check, beacon send, time sync).
 */
public abstract class Response {

    static class KeyValuePair {
        final String key;
        final String value;

        KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Bad request is error code 400 in the HTTP protocol.
     */
    private static final int HTTP_BAD_REQUEST = 400;

    /**
     * Key in the HTTP response headers for Retry-After
     */
    static final String RESPONSE_KEY_RETRY_AFTER = "retry-after";
    /**
     * Default "Retry-After" is 10 minutes.
     */
    static final long DEFAULT_RETRY_AFTER_IN_MILLISECONDS = 10L * 60L * 1000L;

    private final Logger logger;

    private final int responseCode;
    private final Map<String, List<String>> headers;

    // *** constructors ***

    Response(Logger logger, int responseCode, Map<String, List<String>> headers) {
        this.logger = logger;
        this.responseCode = responseCode;
        this.headers = headers;
    }

    // *** getter methods ***

    public boolean isErroneousResponse() {
        return getResponseCode() >= HTTP_BAD_REQUEST;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
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

    static List<KeyValuePair> parseResponseKeyValuePair(String response) {

        List<KeyValuePair> result = new ArrayList<KeyValuePair>();
        StringTokenizer tokenizer = new StringTokenizer(response, "&");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int keyValueSeparatorIndex = token.indexOf('=');
            if (keyValueSeparatorIndex == -1) {
                throw new IllegalArgumentException("Invalid response; even number of tokens expected.");
            }
            String key = token.substring(0, keyValueSeparatorIndex);
            String value = token.substring(keyValueSeparatorIndex + 1);

            result.add(new KeyValuePair(key, value));
        }

        return result;
    }
}
