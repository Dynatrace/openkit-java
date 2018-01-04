package com.dynatrace.openkit.providers;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DefaultSessionIDProviderTest {

    @Test
    public void defaultSessionIDProviderInitializedWithTimestampReturnsANonNegativeInteger() {
        // given
        // default constructor uses a random number generated from the current system timestamp
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider();

        //when
        int actual = provider.getNextSessionID();

        // then
        assertThat(actual, is(greaterThan(0)));
    }

    @Test
    public void defaultSessionIDProviderProvidesConsecutiveNumbers() {
        //given
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider(Integer.MAX_VALUE / 2);

        // when
        int firstSessionID = provider.getNextSessionID();
        int secondSessionID = provider.getNextSessionID();

        // then
        assertThat(secondSessionID, is(firstSessionID + 1));
    }

    @Test
    public void aProviderInitializedWithMaxIntValueProvidesMinSessionIdValueAtNextCall() {
        //given
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider(Integer.MAX_VALUE);

        //when
        int actual = provider.getNextSessionID();

        //then
        assertThat(actual, is(equalTo(1)));
    }

    @Test
    public void aProviderInitializedWithZeroProvidesMinSessionIdValueAtNextCall() {
        //given
        DefaultSessionIDProvider provider = new DefaultSessionIDProvider(0);

        //when
        int actual = provider.getNextSessionID();

        //then
        assertThat(actual, is(equalTo(1)));
    }
}
