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

package com.dynatrace.openkit.util.json.constants;

import java.util.regex.Pattern;

/**
 * Class storing the JSON literals.
 */
public class JSONLiterals {

    /** boolean true literal */
    public static final String BOOLEAN_TRUE_LITERAL = "true";
    /** boolean false literal */
    public static final String BOOLEAN_FALSE_LITERAL = "false";
    /** null literal */
    public static final String NULL_LITERAL = "null";

    /** regex pattern for parsing number literals */
    public static final Pattern NUMBER_PATTERN = Pattern.compile("^-?(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?$");

    /**
     * Default constructor.
     *
     * <p>
     *     This ctor should be private, since this class contains only constants.
     * </p>
     */
    private JSONLiterals() {

    }
}
