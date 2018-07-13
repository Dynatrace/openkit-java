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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implements a time sync response which is sent for time sync requests.
 */
public class TimeSyncResponse extends Response {

    // time sync response constants
    public static final String RESPONSE_KEY_REQUEST_RECEIVE_TIME = "t1";
    public static final String RESPONSE_KEY_RESPONSE_SEND_TIME = "t2";

    // timestamps contained in time sync response
    private long requestReceiveTime = -1;
    private long responseSendTime = -1;

    // *** constructors ***

    public TimeSyncResponse(String response, int responseCode, Map<String, List<String>> headers) {
        super(responseCode, headers);
        parseResponse(response);
    }

    // *** private methods ***

    // parses time sync response
    private void parseResponse(String response) {

        if (response == null || response.isEmpty()) {
            return;
        }


        List<KeyValuePair> parsedResponse = Response.parseResponseKeyValuePair(response);
        for (KeyValuePair kv : parsedResponse) {

            if (RESPONSE_KEY_REQUEST_RECEIVE_TIME.equals(kv.key)) {
                requestReceiveTime = Long.parseLong(kv.value);
            } else if (RESPONSE_KEY_RESPONSE_SEND_TIME.equals(kv.key)) {
                responseSendTime = Long.parseLong(kv.value);
            }
        }
    }

    // *** getter methods ***

    public long getRequestReceiveTime() {
        return requestReceiveTime;
    }

    public long getResponseSendTime() {
        return responseSendTime;
    }

}
