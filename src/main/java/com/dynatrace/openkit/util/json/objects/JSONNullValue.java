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

/**
 * JSON value class representing a null value.
 */
public class JSONNullValue extends JSONValue {

    /**
     * The sole instance of this class.
     */
    public static final JSONNullValue NULL = new JSONNullValue();

    /**
     * Constructor.
     *
     * <p>
     *     To avoid object churn, use {@link #NULL}, which is the only instance of this class.
     * </p>
     */
    private JSONNullValue() {
    }

    @Override
    public boolean isNull() {
        return true;
    }
}
