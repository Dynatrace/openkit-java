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

import java.util.StringTokenizer;

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

    public TimeSyncResponse(String response, int responseCode) {
        super(responseCode);
        parseResponse(response);
    }

    // *** private methods ***

    // parses time sync response
    private void parseResponse(String response) {

        if (response == null || response.isEmpty()) {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(response, "&");
        while (tokenizer.hasMoreTokens()) {

            String token = tokenizer.nextToken();
            int keyValueSeparatorIndex = token.indexOf('=');
            if (keyValueSeparatorIndex == -1) {
                throw new IllegalArgumentException("Invalid response; even number of tokens expected.");
            }
            String key = token.substring(0, keyValueSeparatorIndex);
            String value = token.substring(keyValueSeparatorIndex + 1);

            if (RESPONSE_KEY_REQUEST_RECEIVE_TIME.equals(key)) {
                requestReceiveTime = Long.parseLong(value);
            } else if (RESPONSE_KEY_RESPONSE_SEND_TIME.equals(key)) {
                responseSendTime = Long.parseLong(value);
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
