/**
 * Copyright 2018-2020 Dynatrace LLC
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

package com.dynatrace.openkit.util.json.objects;

/**
 * JSON value class representing a string value.
 */
public class JSONStringValue extends JSONValue {

    /** The underlying string value */
    private final String stringValue;

    /**
     * Constructor taking the underlying string value.
     *
     * <p>
     *     To create an instance of {@link JSONStringValue} use the factory method {@link #fromString(String)}.
     * </p>
     *
     * @param stringValue The string value of this JSON string.
     */
    private JSONStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Factory method to create a {@link JSONStringValue} and initialize it with given string.
     *
     * @param stringValue The string value used for initializing this instance.
     *
     * @return Newly created {@link JSONStringValue} or {@code null} if argument is {@code null}.
     */
    public static JSONStringValue fromString(String stringValue) {
        return stringValue == null ? null : new JSONStringValue(stringValue);
    }

    @Override
    public boolean isString() {
        return true;
    }

    /**
     * Get the underlying string.
     *
     * @return String value.
     */
    public String getValue() {
        return stringValue;
    }
}
