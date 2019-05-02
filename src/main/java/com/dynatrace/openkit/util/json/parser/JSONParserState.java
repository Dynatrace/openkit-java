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

package com.dynatrace.openkit.util.json.parser;

public enum JSONParserState {
    INIT,                   // initial state of the JSON parser
    IN_ARRAY_START,         // state when start of array was encountered
    IN_ARRAY_VALUE,         // state in array, after value has been parsed
    IN_ARRAY_DELIMITER,     // state in array, after delimiter has been parsed
    IN_OBJECT_START,        // state when start of object was encountered
    IN_OBJECT_KEY,          // state in object, after key has been parsed
    IN_OBJECT_COLON,        // state in object, after key value delimiter (":") has been parsed
    IN_OBJECT_VALUE,        // state in object, after value has been parsed
    IN_OBJECT_DELIMITER,    // state in object, after comma delimiter has been parsed
    END,                    // end state of the JSON parser
    ERROR                   // error state
}
