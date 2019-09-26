/**
 *   Copyright 2018-2019 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.protocol;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class EventTypeTest {

    @Parameter
    public int expectedValue;

    @Parameter(1)
    public EventType eventType;

    @Parameters(name = "expected: {0}, EventType: {1}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {1,     EventType.ACTION},
                {10,    EventType.NAMED_EVENT},
                {11,    EventType.VALUE_STRING},
                {12,    EventType.VALUE_INT},
                {13,    EventType.VALUE_DOUBLE},
                {18,    EventType.SESSION_START},
                {19,    EventType.SESSION_END},
                {30,    EventType.WEB_REQUEST},
                {40,    EventType.ERROR},
                {50,    EventType.CRASH},
                {60,    EventType.IDENTIFY_USER}
        });
    }

    @Test
    public void eventTypeValue() {
        assertThat(eventType.protocolValue(), is(expectedValue));
        assertThat(eventType.asBeaconValue(), is(String.valueOf(expectedValue)));
    }
}
