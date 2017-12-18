package com.dynatrace.openkit.providers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultTimingProviderTest {

    private long clusterOffset = 1234L;
    private long now;
    private DefaultTimingProvider provider;

    @Before
    public void setUp() {
        // store now
        now = System.currentTimeMillis();
        provider = new TestDefaultTimingProvider(now);
    }

    @Test
    public void timeSyncIsSupportedByDefault() {
        // given
        TimingProvider provider = new DefaultTimingProvider();

        // then
        assertThat(provider.isTimeSyncSupported(), is(true));
    }

    @Test
    public void timeSyncIsSupportedIfInitCalledWithTrue() {
        // when
        provider.initialize(0L, true);

        // then
        assertThat(provider.isTimeSyncSupported(), is(true));
    }

    @Test
    public void timeSyncIsNotSupportedIfInitCalledWithFalse() {
        // when
        provider.initialize(0L, false);

        // then
        assertThat(provider.isTimeSyncSupported(), is(false));
    }

    @Test
    public void canConvertToClusterTime() {
        // given
        provider.initialize(clusterOffset, true);

        // when
        long target = provider.convertToClusterTime(now);

        // then
        assertThat(target, is(equalTo(clusterOffset + now)));
    }

    @Test
    public void lastInitTimeIsSetCorrectly() {
        // given
        provider.initialize(clusterOffset, true);

        // when
        long target = provider.getLastInitTimeInClusterTime();

        // then
        assertThat(target, is(equalTo(clusterOffset + now)));
    }

    @Test
    public void canGetTimeSinceLastInit() {
        // given
        provider.initialize(clusterOffset, true);

        // when
        long target = provider.getTimeSinceLastInitTime();

        // then
        assertThat(target, is(equalTo(0L)));
    }

    @Test
    public void canGetTimeSinceLastInitTimeWithTimestamp() {
        // given
        provider.initialize(clusterOffset, true);

        // when
        long target = provider.getTimeSinceLastInitTime(now + 1);

        // then
        assertThat(target, is(equalTo(1L)));
    }

    /**
     * DefaultTimingProvider that always returns the same value for provideTimestampInMilliseconds
     */
    private class TestDefaultTimingProvider extends DefaultTimingProvider {
        private final long now;

        TestDefaultTimingProvider(long now) {
            this.now = now;
        }

        @Override
        public long provideTimestampInMilliseconds() {
            return now;
        }
    }
}
