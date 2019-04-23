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

package com.dynatrace.openkit.util.json.objects;

import java.util.Iterator;
import java.util.List;

/**
 * JSON value class representing an array value.
 *
 * <p>
 *     A JSON array is a composite object that stores other {@link JSONValue JSON values}.
 * </p>
 */
public class JSONArrayValue extends JSONValue {

    /** They underlying array storing the values */
    private final List<JSONValue> jsonValues;

    /**
     * Constructor taking the underlying list of {@link JSONValue JSON values}.
     *
     * <p>
     *     Instead of using this constructor directly use {@link #fromList(List)} factory method.
     * </p>
     *
     * @param jsonValues The underlying list of values.
     */
    private JSONArrayValue(List<JSONValue> jsonValues) {
        this.jsonValues = jsonValues;
    }

    /**
     * Create a new JSONArrayValue for given List.
     *
     * @param jsonValues The list of JSON values.
     *
     * @return Newly created {@link JSONArrayValue} or {@code null} if argument is null.
     */
    public static JSONArrayValue fromList(List<JSONValue> jsonValues) {
        return jsonValues == null ? null : new JSONArrayValue(jsonValues);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    /**
     * Get the size of this JSON array.
     *
     * @return Size of this JSON array.
     */
    public int size() {
        return jsonValues.size();
    }

    /**
     * Returns the element at the specified position in this JSON array.
     *
     * @param index Index of the element to return
     *
     * @return The element at the specified position in this JSON array.
     *
     * @throws IndexOutOfBoundsException If given {@code index} is out of bounds.
     */
    public JSONValue get(int index) {
        return jsonValues.get(index);
    }

    /**
     * Returns an iterator over the elements in this JSON array in proper sequence.
     *
     * @return An iterator over the elements in this JSON array in proper sequence
     */
    public Iterator<JSONValue> iterator() {
        return jsonValues.iterator();
    }
}
