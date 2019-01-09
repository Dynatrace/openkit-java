/**
 * Copyright 2018-2019 Dynatrace LLC
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

/**
 * Event types used in the beacon protocol.
 */
public enum EventType {
    ACTION,                    // Action
    VALUE_STRING,            // captured string
    VALUE_INT,                // captured int
    VALUE_DOUBLE,            // captured double
    NAMED_EVENT,            // named event
    SESSION_END,            // session end
    WEBREQUEST,                // tagged web request
    ERROR,                    // error
    CRASH,                    // crash
    IDENTIFY_USER;            // identify user

    public short protocolValue() {
        switch (this) {
            case ACTION:
                return 1;
            case VALUE_STRING:
                return 11;
            case VALUE_INT:
                return 12;
            case VALUE_DOUBLE:
                return 13;
            case NAMED_EVENT:
                return 10;
            case SESSION_END:
                return 19;
            case WEBREQUEST:
                return 30;
            case ERROR:
                return 40;
            case CRASH:
                return 50;
            case IDENTIFY_USER:
                return 60;
            default:
                return -1;
        }
    }

}
