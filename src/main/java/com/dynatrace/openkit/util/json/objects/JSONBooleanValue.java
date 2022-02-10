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

import com.dynatrace.openkit.util.json.constants.JSONLiterals;

/**
 * JSON value class representing a boolean value.
 */
public class JSONBooleanValue extends JSONValue {

    /** singleton instance representing true. */
    public static final JSONBooleanValue TRUE = new JSONBooleanValue(true);

    /** singleton instance representing false. */
    public static final JSONBooleanValue FALSE = new JSONBooleanValue(false);

    /** The boolean value represented by this instance. */
    private final boolean value;

    /**
     * Constructor taking the boolean value which is represented by this instance.
     *
     * <p>
     *     Use the factory methods {@link #fromValue(boolean)} or {@link #fromLiteral(String)} instead.
     * </p>
     *
     * @param value The boolean value represented by this instance.
     */
    private JSONBooleanValue(boolean value) {
        this.value = value;
    }

    /**
     * Factory method to create a {@link JSONBooleanValue} from a given boolean value.
     *
     * <p>
     *     The return value is one of the two singleton instances {@link #TRUE} or {@link #FALSE}, depending on {@code value}.
     * </p>
     *
     * @param value The boolean value.
     * @return Returns either {@link #FALSE} or {@link #TRUE}.
     */
    public static JSONBooleanValue fromValue(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Factory method to create a {@link JSONBooleanValue} from a given JSON literal.
     *
     * @param literal The string literal for which to create the value.
     *
     * @return {@link #TRUE} if literal is {@link JSONLiterals#BOOLEAN_TRUE_LITERAL}, {@link #FALSE} if literal
     *      is {@link JSONLiterals#BOOLEAN_FALSE_LITERAL} and {@code null} in every other case.
     */
    public static JSONBooleanValue fromLiteral(String literal) {
        if (literal == null) {
            return null;
        } else if (literal.equals(JSONLiterals.BOOLEAN_TRUE_LITERAL)) {
            return TRUE;
        } else if (literal.equals(JSONLiterals.BOOLEAN_FALSE_LITERAL)) {
            return FALSE;
        } else {
            return null;
        }
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    void writeJSONString(JSONValueWriter writer, JSONOutputConfig config) {
        writer.insertValue(getValue() ? JSONLiterals.BOOLEAN_TRUE_LITERAL : JSONLiterals.BOOLEAN_FALSE_LITERAL);
    }

    /**
     * Get value represented by this instance.
     *
     * @return Boolean value represented by this instance.
     */
    public boolean getValue() {
        return value;
    }
}
