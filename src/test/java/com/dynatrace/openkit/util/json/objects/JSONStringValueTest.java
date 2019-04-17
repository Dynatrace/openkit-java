/**
 * Copyright 2018-2019 Dynatrace LLC
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

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JSONStringValueTest {

    @Test
    public void isNullReturnsFalse() {
        // then
        assertThat(JSONStringValue.fromString("").isNull(), is(false));
    }

    @Test
    public void isBooleanReturnsFalse() {
        // then
        assertThat(JSONStringValue.fromString("").isBoolean(), is(false));
    }

    @Test
    public void isNumberReturnsFalse() {
        // then
        assertThat(JSONStringValue.fromString("").isNumber(), is(false));
    }

    @Test
    public void isStringReturnsTrue() {
        // then
        assertThat(JSONStringValue.fromString("").isString(), is(true));
    }

    @Test
    public void isArrayReturnsFalse() {
        // then
        assertThat(JSONStringValue.fromString("").isArray(), is(false));
    }

    @Test
    public void isObjectReturnsFalse() {
        // then
        assertThat(JSONStringValue.fromString("").isObject(), is(false));
    }

    @Test
    public void fromStringReturnsNullIfArgumentIsNull() {
        // then
        assertThat(JSONStringValue.fromString(null), is(nullValue()));
    }

    @Test
    public void getValueGivesValueOfFactoryMethodArgument() {
        // then
        assertThat(JSONStringValue.fromString("").getValue(), is(equalTo("")));
        assertThat(JSONStringValue.fromString("a").getValue(), is(equalTo("a")));
        assertThat(JSONStringValue.fromString("foobar").getValue(), is(equalTo("foobar")));
    }
}
