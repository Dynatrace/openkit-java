package com.dynatrace.openkit.providers;


import org.junit.Before;
import org.junit.Test;

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
        assertTrue(actual >= 0);
    }

    @Test
    public void defaultSessionIDProviderProvidesConsecutiveNumbers() {
        // when
        int firstSessionID = provider.getNextSessionID();
        int secondSessionID = provider.getNextSessionID();

        // then
        assertTrue(firstSessionID + 1 == secondSessionID);
    }
}
