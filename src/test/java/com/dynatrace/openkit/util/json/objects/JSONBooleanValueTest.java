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

package com.dynatrace.openkit.util.json.objects;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class JSONBooleanValueTest {

    @Test
    public void isNullReturnsFalse() {
        // then
        assertThat(JSONBooleanValue.fromValue(true).isNull(), is(false));
    }

    @Test
    public void isBooleanReturnsTrue() {
        // then
        assertThat(JSONBooleanValue.fromValue(true).isBoolean(), is(true));
    }

    @Test
    public void isNumberReturnsFalse() {
        // then
        assertThat(JSONBooleanValue.fromValue(true).isNumber(), is(false));
    }

    @Test
    public void isStringReturnsFalse() {
        // then
        assertThat(JSONBooleanValue.fromValue(true).isString(), is(false));
    }

    @Test
    public void isArrayReturnsFalse() {
        // then
        assertThat(JSONBooleanValue.fromValue(true).isArray(), is(false));
    }

    @Test
    public void isObjectReturnsFalse() {
        // then
        assertThat(JSONBooleanValue.fromValue(true).isObject(), is(false));
    }

    @Test
    public void getValueReturnsAppropriateBooleanValue() {
        // then
        assertThat(JSONBooleanValue.TRUE.getValue(), is(true));
        assertThat(JSONBooleanValue.FALSE.getValue(), is(false));
    }

    @Test
    public void fromValueReturnsTrueSingletonValue() {
        // when
        JSONBooleanValue obtained = JSONBooleanValue.fromValue(true);

        // then the singleton TRUE instance is returned
        assertThat(obtained, is(sameInstance(JSONBooleanValue.TRUE)));
    }

    @Test
    public void fromValueReturnsFalseSingletonValue() {
        // when
        JSONBooleanValue obtained = JSONBooleanValue.fromValue(false);

        // then the singleton FALSE instance is returned
        assertThat(obtained, is(sameInstance(JSONBooleanValue.FALSE)));
    }

    @Test
    public void fromLiteralGivesFalseIfLiteralIsNull() {
        // then
        assertThat(JSONBooleanValue.fromLiteral(null), is(nullValue()));
    }

    @Test
    public void fromLiteralReturnsTrueSingletonValueForTrueLiteral() {
        // when
        JSONBooleanValue obtained = JSONBooleanValue.fromLiteral("true");

        // then the singleton FALSE instance is returned
        assertThat(obtained, is(sameInstance(JSONBooleanValue.TRUE)));
    }

    @Test
    public void fromLiteralReturnsFalseSingletonValueForFalseLiteral() {
        // when
        JSONBooleanValue obtained = JSONBooleanValue.fromLiteral("false");

        // then the singleton FALSE instance is returned
        assertThat(obtained, is(sameInstance(JSONBooleanValue.FALSE)));
    }

    @Test
    public void fromLiteralReturnsNullForNonBooleanLiterals() {
        // when wrong casing is used, then
        assertThat(JSONBooleanValue.fromLiteral("TRUE"), is(nullValue()));
        assertThat(JSONBooleanValue.fromLiteral("FALSE"), is(nullValue()));

        // and when it's actually a number, then
        assertThat(JSONBooleanValue.fromLiteral("1234"), is(nullValue()));

        // and when passed an empty string, then
        assertThat(JSONBooleanValue.fromLiteral(""), is(nullValue()));
    }
}
