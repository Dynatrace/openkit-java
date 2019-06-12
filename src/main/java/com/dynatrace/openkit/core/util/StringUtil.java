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

package com.dynatrace.openkit.core.util;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * Generates a 64 bit hash from the given string.
     *
     * @param stringValue the value to be hashed
     * @return the 64 bit hash of the given string ({@code 0} in case the given string is {@code null}) or empty.
     */
    public static long to64BitHash(String stringValue) {
        if(stringValue == null) {
            return 0;
        }

        long hash = 0;

        for (int i = 0; i < stringValue.length(); i++) {
            hash = 31 * hash + stringValue.charAt(i);
        }
        return hash;
    }
}
