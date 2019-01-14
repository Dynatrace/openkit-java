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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("resource")
public class WebRequestTracerStringURLTest {

    @Test
    public void nullIsNotAValidURLScheme() {

        // then
        assertThat(WebRequestTracerStringURL.isValidURLScheme(null), is(false));
    }

    @Test
    public void aValidSchemeStartsWithALetter() {

        // when starting with lower case letter, then
        assertThat(WebRequestTracerStringURL.isValidURLScheme("a://some.host"), is(true));
    }

    @Test
    public void aValidSchemeOnlyContainsLettersDigitsPlusPeriodOrHyphen() {

        // when the url scheme contains all allowed characters
        assertThat(WebRequestTracerStringURL.isValidURLScheme("b1+Z6.-://some.host"), is(true));
    }

    @Test
    public void aValidSchemeAllowsUpperCaseLettersToo() {

        // when the url scheme contains all allowed characters
        assertThat(WebRequestTracerStringURL.isValidURLScheme("Obp1e+nZK6i.t-://some.host"), is(true));
    }

    @Test
    public void aValidSchemeDoesNotStartWithADigit() {

        assertThat(WebRequestTracerStringURL.isValidURLScheme("1a://some.host"), is(false));
    }

    @Test
    public void aSchemeIsInvalidIfInvalidCharactersAreEncountered() {

        assertThat(WebRequestTracerStringURL.isValidURLScheme("a()[]{}@://some.host"), is(false));
    }

    @Test
    public void anURLIsOnlySetInConstructorIfItIsValid() {

        // given
        WebRequestTracerStringURL target = new WebRequestTracerStringURL(mock(Logger.class), mock(Beacon.class), 0, "a1337://foo");

        // then
        assertThat(target.getURL(), is(equalTo("a1337://foo")));
    }

    @Test
    public void ifURLIsInvalidTheDefaultValueIsUsed() {
        // given
        WebRequestTracerStringURL target = new WebRequestTracerStringURL(mock(Logger.class), mock(Beacon.class), 0, "foobar");

        // then
        assertThat(target.getURL(), is(equalTo("<unknown>")));
    }

    @Test
    public void urlStoredDoesNotContainRequestParameters() {
        // given
        WebRequestTracerStringURL target = new WebRequestTracerStringURL(mock(Logger.class), mock(Beacon.class), 0, "https://www.google.com/foo/bar?foo=bar&asdf=jklo");

        // then
        assertThat(target.getURL(), is(equalTo("https://www.google.com/foo/bar")));
    }
}
