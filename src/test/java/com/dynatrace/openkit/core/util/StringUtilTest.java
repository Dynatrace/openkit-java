/**
 *   Copyright 2018-2021 Dynatrace LLC
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

package com.dynatrace.openkit.core.util;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

public class StringUtilTest {

    @Test
    public void hashNullString() {
        // given, when
        long hash = StringUtil.to64BitHash(null);

        // then
        assertThat(hash, is(equalTo(0L)));
    }

    @Test
    public void hashEmptyString() {
        // given
        String emptString = "";

        // when
        long hash = StringUtil.to64BitHash(emptString);

        //then
        assertThat(hash, is(equalTo(0L)));
    }

    @Test
    public void differentStringsDifferentHash() {
        // given
        String firstString = "some string";
        String secondString = "some other string";

        // when
        long firstHash = StringUtil.to64BitHash(firstString);
        long secondHash = StringUtil.to64BitHash(secondString);

        //then
        assertThat(firstHash, not(equalTo(secondHash)));
    }

    @Test
    public void equalStringSameHash() {
        // given
        String firstString = "str";
        String secondString = String.valueOf(new char[]{'s', 't', 'r'});

        // when
        long firstHash = StringUtil.to64BitHash(firstString);
        long secondHash = StringUtil.to64BitHash(secondString);

        // then
        assertNotSame(firstString, secondString);
        assertThat(firstString, is(equalTo(secondString)));
        assertThat(firstHash, is(equalTo(secondHash)));
    }

    @Test
    public void caseSensitiveStringsDifferentHash() {
        // given
        String lowerCase = "value";
        String upperCase = "Value";

        // when
        long lowerCaseHash = StringUtil.to64BitHash(lowerCase);
        long upperCaseHash = StringUtil.to64BitHash(upperCase);

        //
        assertThat(lowerCaseHash, not(equalTo(upperCaseHash)));
    }
}
