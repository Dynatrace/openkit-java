package com.dynatrace.openkit.providers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class DefaultSessionIDProviderTest {

    private DefaultSessionIDProvider provider;

    @Before
    public void setUp() {
        provider = new DefaultSessionIDProvider();
    }

    @Test
    public void defaultSessionIDProviderInitializedWithTimestampReturnsANonNegativeInteger() {
        // when
        int actual = provider.getNextSessionID();

        // then
        assertThat(actual, is(greaterThan(0)));
    }

    @Test
    public void defaultSessionIDProviderProvidesConsecutiveNumbers() {
        // when
        int firstSessionID = provider.getNextSessionID();
        int secondSessionID = provider.getNextSessionID();

        // then
        assertThat(secondSessionID, is(firstSessionID + 1 ));
    }
}
