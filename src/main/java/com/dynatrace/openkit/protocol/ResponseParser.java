/**
 * Copyright 2018-2019 Dynatrace LLC
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

import com.dynatrace.openkit.util.json.parser.ParserException;

/**
 * Evaluates a response string and transforms it into a
 */
public class ResponseParser {

    private static final String KEY_VALUE_RESPONSE_TYPE_MOBILE = "type=m";
    private static final String KEY_VALUE_RESPONSE_TYPE_MOBILE_WITH_SEPARATOR = "type=m&";

    private ResponseParser() {
    }

    public static Response parseResponse(String responseString) throws ParserException {
        if (isKeyValuePairResponse(responseString)) {
            return KeyValueResponseParser.parse(responseString);
        }

        return JsonResponseParser.parse(responseString);
    }

    private static boolean isKeyValuePairResponse(String responseString) {
        return responseString.equals(KEY_VALUE_RESPONSE_TYPE_MOBILE)
                || responseString.startsWith(KEY_VALUE_RESPONSE_TYPE_MOBILE_WITH_SEPARATOR);
    }
}
