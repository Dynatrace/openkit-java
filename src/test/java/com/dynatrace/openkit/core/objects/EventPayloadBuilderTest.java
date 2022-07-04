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
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EventPayloadBuilderTest {

    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = mock(Logger.class);
    }

    @Test
    public void createEmptyPayloadBuilder() {
        EventPayloadBuilder builder = new EventPayloadBuilder(mockLogger, new HashMap<String, JSONValue>());
        assertThat(builder.build(), is("{}"));
    }

    @Test
    public void removingReservedValuesAtInitializing() {
        HashMap<String, JSONValue> attributes = new HashMap<String, JSONValue>();
        attributes.put("dt", JSONStringValue.fromString("Removed"));
        attributes.put("dt.test", JSONStringValue.fromString("Removed"));
        attributes.put("event.kind", JSONStringValue.fromString("Override"));

        EventPayloadBuilder builder = new EventPayloadBuilder(mockLogger, attributes);
        assertTrue("Attribute \"event.kind\" was not found in the builder.", isStringAvailable(builder.build(), "\"event.kind\":\"Override\""));
        assertFalse("Attribute \"dt\" was wrongly found inside of the builder.", isStringAvailable(builder.build(), "\"dt\":\"Removed\""));
        assertFalse("Attribute \"dt.test\" was wrongly found inside of the builder.", isStringAvailable(builder.build(), "\"dt.test\":\"Removed\""));
    }

    @Test
    public void addNonOverridableAttributeWhichIsAlreadyAvailable() {
        HashMap<String, JSONValue> attributes = new HashMap<String, JSONValue>();
        attributes.put("dt.sid", JSONStringValue.fromString("SessionID"));

        EventPayloadBuilder builder = new EventPayloadBuilder(mockLogger, attributes);
        builder.addNonOverridableAttribute("dt.sid", JSONStringValue.fromString("NonOverridable"));
        assertTrue("Attribute \"dt.sid\" was not found in the builder.", isStringAvailable(builder.build(), "\"dt.sid\":\"NonOverridable\""));
    }

    @Test
    public void addNonOverridableAttributeWhichIsNotAvailable() {
        EventPayloadBuilder builder = new EventPayloadBuilder(mockLogger, new HashMap<String, JSONValue>());
        builder.addNonOverridableAttribute("NonOverridableKey", JSONStringValue.fromString("Value"));
        assertTrue("Attribute \"NonOverridableKey\" was not found in the builder.", isStringAvailable(builder.build(), "\"NonOverridableKey\":\"Value\""));
    }

    @Test
    public void addOverridableAttributeWhichIsAlreadyAvailable() {
        HashMap<String, JSONValue> attributes = new HashMap<String, JSONValue>();
        attributes.put("timestamp", JSONStringValue.fromString("NewValue"));

        EventPayloadBuilder builder = new EventPayloadBuilder(mockLogger, attributes);
        builder.addOverridableAttribute("timestamp", JSONStringValue.fromString("Overridable"));
        assertTrue("Attribute \"timestamp\" was not found in the builder.", isStringAvailable(builder.build(), "\"timestamp\":\"NewValue\""));
        assertTrue("Attribute \"dt.overridden_keys\" was not found in the builder.", isStringAvailable(builder.build(), "\"dt.overridden_keys\":[\"timestamp\"]"));
    }

    @Test
    public void addOverridableAttributeWhichIsNotAvailable() {
        EventPayloadBuilder builder = new EventPayloadBuilder(mockLogger, new HashMap<String, JSONValue>());

        builder.addOverridableAttribute("Overridable", JSONStringValue.fromString("Value"));
        assertTrue("Attribute \"Overridable\" was not found in the builder.", isStringAvailable(builder.build(), "\"Overridable\":\"Value\""));
    }

    private static boolean isStringAvailable(String compareStr, String value) {
        return compareStr.contains(value);
    }
}
