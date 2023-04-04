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

package com.dynatrace.openkit.core.util;

import com.dynatrace.openkit.core.objects.EventPayloadBuilder;
import com.dynatrace.openkit.util.json.objects.JSONArrayValue;
import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONObjectValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;

import java.util.Iterator;

public class EventPayloadBuilderUtil {

    private EventPayloadBuilderUtil() {
    }

    private static boolean isObjectContainingNonFiniteNumericValues(JSONObjectValue jsonObject) {
        for(String key: jsonObject.keySet()) {
            if(isItemContainingNonFiniteNumericValues(jsonObject.get(key))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isArrayContainingNonFiniteNumericValues(JSONArrayValue jsonArrayValue) {
        Iterator<JSONValue> it = jsonArrayValue.iterator();

        while(it.hasNext()) {
            if(isItemContainingNonFiniteNumericValues(it.next())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isItemContainingNonFiniteNumericValues(JSONValue jsonValue) {
        return (jsonValue.isObject() && isObjectContainingNonFiniteNumericValues((JSONObjectValue) jsonValue))
                || (jsonValue.isArray() && isArrayContainingNonFiniteNumericValues((JSONArrayValue) jsonValue))
                || (jsonValue.isNumber() && !((JSONNumberValue) jsonValue).isFinite());
    }

}
