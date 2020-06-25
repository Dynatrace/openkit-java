/**
 * Copyright 2018-2020 Dynatrace LLC
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

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class PercentEncoderTest {

    /**
     * All unreserved characters based on RFC-3986
     */
    private static final String UNRESERVED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~";

    @Test
    public void rfc3986UnreservedCharactersAreNotEncoded() {
        // when
        String obtained = PercentEncoder.encode(UNRESERVED_CHARACTERS, "UTF-8");

        // then
        assertThat(obtained, is(equalTo(UNRESERVED_CHARACTERS)));
    }

    @Test
    public void reservedCharactersArePercentEncoded() {
        // when
        String obtained = PercentEncoder.encode("+()/\\&%$#@!`?<>[]{}", "UTF-8");

        // then
        String expected = "%2B%28%29%2F%5C%26%25%24%23%40%21%60%3F%3C%3E%5B%5D%7B%7D"; // precomputed using Python
        assertThat(obtained, is(equalTo(expected)));
    }

    @Test
    public void mixingReservedAndUnreservedCharactersWorks() {
        // when
        String obtained = PercentEncoder.encode("a+bc()~/\\&0_", "UTF-8");

        // then
        String expected = "a%2Bbc%28%29~%2F%5C%260_"; // precomputed using Python
        assertThat(obtained, is(equalTo(expected)));
    }

    @Test
    public void charactersOutsideOfAsciiRangeAreEncodedFirst() {
        // when
        String obtained = PercentEncoder.encode("aösÖ€dÁF", "UTF-8");

        // then
        String expected = "a%C3%B6s%C3%96%E2%82%ACd%C3%81F";
        assertThat(obtained, is(equalTo(expected)));
    }

    @Test
    public void itIsPossibleToMarkAdditionalCharactersAsReserved() {
        // when
        String additionalReservedCharacters = "€0_";
        String obtained = PercentEncoder.encode("0123456789-._~", "UTF-8", additionalReservedCharacters.toCharArray());

        // then
        String expected = "%30123456789-.%5F~";
        assertThat(obtained, is(equalTo(expected)));
    }

    @Test
    public void nullIsReturnedIfEncodingIsNecessaryButIsNotKnown() {
        // when
        String obtained = PercentEncoder.encode("a€b", "this-is-really-no-valid-encoding");

        // then
        assertThat(obtained, is(nullValue()));
    }
}
