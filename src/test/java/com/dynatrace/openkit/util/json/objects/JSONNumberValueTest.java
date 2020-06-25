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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class JSONNumberValueTest {

    @Test
    public void isNullReturnsFalse() {
        // then
        assertThat(JSONNumberValue.fromLong(0L).isNull(), is(false));
    }

    @Test
    public void isBooleanReturnsFalse() {
        // then
        assertThat(JSONNumberValue.fromLong(0L).isBoolean(), is(false));
    }

    @Test
    public void isNumberReturnsTrue() {
        // then
        assertThat(JSONNumberValue.fromLong(0L).isNumber(), is(true));
    }

    @Test
    public void isStringReturnsFalse() {
        // then
        assertThat(JSONNumberValue.fromLong(0L).isString(), is(false));
    }

    @Test
    public void isArrayReturnsFalse() {
        // then
        assertThat(JSONNumberValue.fromLong(0L).isArray(), is(false));
    }

    @Test
    public void isObjectReturnsFalse() {
        // then
        assertThat(JSONNumberValue.fromLong(0L).isObject(), is(false));
    }

    @Test
    public void constructingJSONNumberValueWithLongValueSetsIsIntegerFlagToTrue() {
        // given
        JSONNumberValue target = JSONNumberValue.fromLong(42L);

        // then
        assertThat(target.isInteger(), is(true));
    }

    @Test
    public void constructingJSONNumberValueWithDoubleValueSetsIsIntegerFlagToFalse() {
        // given
        JSONNumberValue target = JSONNumberValue.fromDouble(42.0);

        // then
        assertThat(target.isInteger(), is(false));
    }

    @Test
    public void isIntValueReturnsTrueIfValueIsAnIntegerAndInRangeOfJavaInt() {
        // when Integer.MIN_VALUE is used, then
        assertThat(JSONNumberValue.fromLong(Integer.MIN_VALUE).isIntValue(), is(true));

        // when Integer.MAX_VALUE is used, then
        assertThat(JSONNumberValue.fromLong(Integer.MAX_VALUE).isIntValue(), is(true));
    }

    @Test
    public void isIntValueReturnsFalseIfIntegerOutsideJavaIntIsUsed() {
        // when one less than Integer.MIN_VALUE is used, then
        assertThat(JSONNumberValue.fromLong(Integer.MIN_VALUE - 1L).isIntValue(), is(false));

        // when one greater than Integer.MAX_VALUE is used, then
        assertThat(JSONNumberValue.fromLong(Integer.MAX_VALUE + 1L).isIntValue(), is(false));
    }

    @Test
    public void isIntValueReturnsFalseIfValueIsAFloatingPointValue() {
        // when it actually fits into an integer, then
        assertThat(JSONNumberValue.fromDouble(42).isIntValue(), is(false));

        // when it's really a floating point value, then
        assertThat(JSONNumberValue.fromDouble(3.14159).isIntValue(), is(false));
    }

    @Test
    public void getIntValueReturns32BitIntValue() {
        // when constructed from a long that fits into the int range
        assertThat(JSONNumberValue.fromLong(Integer.MIN_VALUE).getIntValue(), is(Integer.MIN_VALUE));
    }

    @Test
    public void getIntValueReturnsCastedValueFromLong() {
        // when constructed from a long that does not fit into 32-bit, then
        assertThat(JSONNumberValue.fromLong(0x11111111deadbabeL).getIntValue(), is(0xdeadbabe));
    }

    @Test
    public void getIntValueReturnsCastedValueFromDouble() {
        // when constructed from a double with fractional part, then
        assertThat(JSONNumberValue.fromDouble(Math.PI).getIntValue(), is(3));

        // and when constructed from another double with fractional part, then
        assertThat(JSONNumberValue.fromDouble(Math.E).getIntValue(), is(2));
    }

    @Test
    public void getLongValueReturnsValueWhenConstructedFromLong() {
        // when, then
        assertThat(JSONNumberValue.fromLong(0xFEEDFACECAFEBEEFL).getLongValue(), is(-77129852519530769L));
    }

    @Test
    public void getLongValueReturnsTruncatedValueWhenConstructedFromDouble() {
        // when constructed from a double with fractional part, then
        assertThat(JSONNumberValue.fromDouble(Math.PI).getLongValue(), is(3L));

        // and when constructed from another double with fractional part, then
        assertThat(JSONNumberValue.fromDouble(Math.E).getLongValue(), is(2L));
    }

    @Test
    public void getFloatValueReturns32BitFloatingPointValue() {
        // when
        // then (expected value from wikipedia)
        assertThat(JSONNumberValue.fromDouble(Math.PI).getFloatValue(), is(3.14159274101F));
    }

    @Test
    public void getDoubleValueReturns64BitFloatingPointValue() {
        // when, then
        assertThat(JSONNumberValue.fromDouble(Math.PI).getDoubleValue(), is(Math.PI));

        // and when, then
        assertThat(JSONNumberValue.fromDouble(Math.E).getDoubleValue(), is(Math.E));
    }

    @Test
    public void fromNumberLiteralReturnsNullIfLiteralIsNull() {
        // when, then
        assertThat(JSONNumberValue.fromNumberLiteral(null), is(nullValue()));
    }

    @Test
    public void fromNumberLiteralReturnsNullIfLiteralIsInvalid() {
        // when constructed with empty string, then
        assertThat(JSONNumberValue.fromNumberLiteral(""), is(nullValue()));

        // and when constructed with arbitrary string, then
        assertThat(JSONNumberValue.fromNumberLiteral("foobar"), is(nullValue()));

        // and when constructed with alpha-numeric string, then
        assertThat(JSONNumberValue.fromNumberLiteral("1234foo"), is(nullValue()));
    }

    @Test
    public void fromNumberLiteralReturnsIntegerRepresentationForIntegerNumbers() {
        // when constructed from positive integer literal
        JSONNumberValue obtained = JSONNumberValue.fromNumberLiteral("1234567890");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isInteger(), is(true));
        assertThat(obtained.getLongValue(), is(1234567890L));
    }

    @Test
    public void fromNumberLiteralReturnsNullForUnparsableLong() {
        // when constructed from positive integer literal
        JSONNumberValue obtained = JSONNumberValue.fromNumberLiteral("9223372036854775808");

        // then
        assertThat(obtained, is(nullValue()));
    }

    @Test
    public void fromNumberLiteralReturnsDoubleIfLiteralContainsFractionPart() {
        // when constructed from positive floating point literal
        JSONNumberValue obtained = JSONNumberValue.fromNumberLiteral("1.25");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isInteger(), is(false));
        assertThat(obtained.getDoubleValue(), is(1.25));
    }

    @Test
    public void fromNumberLiteralReturnsDoubleIfLiteralContainsExponentPart() {
        // when constructed from positive floating point literal
        JSONNumberValue obtained = JSONNumberValue.fromNumberLiteral("15E-1");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isInteger(), is(false));
        assertThat(obtained.getDoubleValue(), is(1.5));
    }

    @Test
    public void fromNumberLiteralReturnsDoubleIfLiteralContainsFractionAndExponentPart() {
        // when constructed from positive integer literal
        JSONNumberValue obtained = JSONNumberValue.fromNumberLiteral("0.0625e+2");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained.isInteger(), is(false));
        assertThat(obtained.getDoubleValue(), is(6.25));
    }
}
