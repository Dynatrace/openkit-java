package com.dynatrace.openkit.providers;

public class DefaultTimingProvider implements TimingProvider {

    @Override
    public long provideTimestampInMilliseconds() {

        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long milliseconds) throws InterruptedException {

        Thread.sleep(milliseconds);
    }
}
