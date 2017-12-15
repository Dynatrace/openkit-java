package com.dynatrace.openkit.test.providers;

import com.dynatrace.openkit.providers.DefaultTimingProvider;

import java.util.concurrent.atomic.AtomicLong;

public class TestTimingProvider extends DefaultTimingProvider {
    private AtomicLong currentTimestamp = new AtomicLong(1000000);

    @Override
    public long provideTimestampInMilliseconds() {
        return currentTimestamp.addAndGet(1000);
    }
}
