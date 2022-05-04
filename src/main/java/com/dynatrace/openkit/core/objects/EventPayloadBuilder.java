/**
 * Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.util.json.objects.JSONArrayValue;
import com.dynatrace.openkit.util.json.objects.JSONObjectValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;

import java.util.*;

public class EventPayloadBuilder {

    /** {@link Logger} for tracing log message */
    private final Logger logger;

    /** Map containing attributes for sendEvent API */
    private final Map<String, JSONValue> attributes = new HashMap<String, JSONValue>();

    /** List containing all keys which have been overridden by the customer */
    private final List<JSONValue> overriddenKeys;

    public EventPayloadBuilder(Logger logger, Map<String, JSONValue> attributes) {
        this.logger = logger;
        overriddenKeys = new LinkedList<JSONValue>();

        initializeInternalAttributes(attributes);
    }

    public EventPayloadBuilder addOverridableAttribute(String key, JSONValue value) {
        if (value != null) {
            if (attributes.containsKey(key)) {
                overriddenKeys.add(JSONStringValue.fromString(key));
            } else {
                attributes.put(key, value);
            }
        }

        return this;
    }

    public EventPayloadBuilder addNonOverridableAttribute(String key, JSONValue value) {
        if (value != null) {
            if (attributes.containsKey(key)) {
                logger.warning("EventPayloadBuilder addNonOverrideableAttribute: " + key + " is reserved for internal values!");
            }
            attributes.put(key, value);
        }

        return this;
    }

    public String build() {
        if (overriddenKeys.size() > 0) {
            addNonOverridableAttribute("dt.overridden_keys", JSONArrayValue.fromList(overriddenKeys));
        }

        return JSONObjectValue.fromMap(attributes).toString();
    }

    /**
     * Initialize the internal attribute Map and filter out the reserved internal keys already
     *
     * @param extAttributes External attributes coming from the API
     */
    public void initializeInternalAttributes(Map<String, JSONValue> extAttributes) {
        if(extAttributes != null){
            for (Map.Entry<String, JSONValue> entry : extAttributes.entrySet()) {
                if (isReservedForInternalAttributes(entry.getKey())) {
                    logger.warning("EventPayloadBuilder initializeInternalAttributes: " + entry.getKey() + " is reserved for internal values!");
                } else {
                    this.attributes.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static boolean isReservedForInternalAttributes(String key) {
        return (key == "dt" ||
            (key.startsWith("dt.") && !key.startsWith("dt.agent.") && key != EventPayloadAttributes.DT_TYPE));
    }
}