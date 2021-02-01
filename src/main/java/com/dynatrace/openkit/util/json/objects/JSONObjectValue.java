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

package com.dynatrace.openkit.util.json.objects;

import java.util.Map;
import java.util.Set;

/**
 * JSON value class representing an object value.
 */
public class JSONObjectValue extends JSONValue {

    /** Underlying map for this JSON object. */
    private final Map<String, JSONValue> jsonObjectMap;

    /**
     * Constructor.
     *
     * <p>
     *     A new instance can be created via the factory method {@link #fromMap(Map)}.
     * </p>
     *
     * @param jsonObjectMap The underlying map representing this JSON object.
     */
    private JSONObjectValue(Map<String, JSONValue> jsonObjectMap) {
        this.jsonObjectMap = jsonObjectMap;
    }

    /**
     * Factory method for creating a new {@link JSONObjectValue}.
     *
     * @param jsonObjectMap The map storing the keys and values of the JSON object.
     * @return Newly created {@link JSONObjectValue} or {@code null} if argument is null.
     */
    public static JSONObjectValue fromMap(Map<String, JSONValue> jsonObjectMap) {
        return jsonObjectMap == null ? null : new JSONObjectValue(jsonObjectMap);
    }

    @Override
    public boolean isObject() {
        return true;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this JSON object.
     *
     * @return  A set view of the keys contained in this map
     */
    public Set<String> keySet() {
        return jsonObjectMap.keySet();
    }

    /**
     * Get the size of this JSON array.
     *
     * @return Size of this JSON array.
     */
    public int size() {
        return jsonObjectMap.size();
    }

    /**
     * Returns {@code true} if and only if the given key is present in this JSON object.
     *
     * @param key The key to test whether it's present or not in this JSON object.
     * @return {@code true} if the key is present in this JSON object, {@code false} otherwise.
     */
    public boolean containsKey(String key) {
        return jsonObjectMap.containsKey(key);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key whose associated JSON value is to be returned
     * @return The JSON value this key is associated with or {@code null} if no such key exists.
     */
    public JSONValue get(String key) {
        return jsonObjectMap.get(key);
    }
}
