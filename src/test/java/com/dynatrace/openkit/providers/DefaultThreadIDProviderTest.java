package com.dynatrace.openkit.providers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultThreadIDProviderTest {
    @Test
    public void currentThreadIDIsReturned() {
        // given
        ThreadIDProvider provider = new DefaultThreadIDProvider();

        // then
        assertThat(provider.getThreadID(), is(equalTo(Thread.currentThread().getId())));
    }
}
