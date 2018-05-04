package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.protocol.Beacon;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

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
        WebRequestTracerStringURL target = new WebRequestTracerStringURL(mock(Logger.class),
            mock(Beacon.class), mock(ActionImpl.class), "a1337://foo");

        // then
        assertThat(target.getURL(), is(equalTo("a1337://foo")));
    }

    @Test
    public void ifURLIsInvalidTheDefaultValueIsUsed() {
        // given
        WebRequestTracerStringURL target = new WebRequestTracerStringURL(mock(Logger.class),
            mock(Beacon.class), mock(ActionImpl.class), "foobar");

        // then
        assertThat(target.getURL(), is(equalTo("<unknown>")));
    }
}
