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

import java.util.*;

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

    private final int responseCode;
    private final Map<String, List<String>> headers;

    // *** constructors ***

    Response(int responseCode, Map<String, List<String>> headers) {
        this.responseCode = responseCode;
        this.headers = headers;
    }

    // *** getter methods ***

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
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
