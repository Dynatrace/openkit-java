/**
 * Copyright 2018-2020 Dynatrace LLC
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
public enum EventType implements SerializableBeaconValue {
    /**
     * Action event
     */
    ACTION(1),
    /**
     * Report string value event
     */
    VALUE_STRING(11),
    /**
     * Report integer value event
     */
    VALUE_INT(12),
    /**
     * Report double value event
     */
    VALUE_DOUBLE(13),
    /**
     * Named event
     */
    NAMED_EVENT(10),
    /**
     * Session start event
     */
    SESSION_START(18),
    /**
     * Session end event
     */
    SESSION_END(19),
    /**
     * Tagged web request event
     */
    WEB_REQUEST(30),
    /**
     * Report error code event
     */
    ERROR(40),
    /**
     * Report exception event
     */
    EXCEPTION(42),
    /**
     * Report crash event
     */
    CRASH(50),
    /**
     * Identify user event
     */
    IDENTIFY_USER(60);

    private final int value;

    EventType(int value) {
        this.value = value;
    }

    public int protocolValue() {
        return value;
    }

    @Override
    public String asBeaconValue() {
        return String.valueOf(value);
    }
}
