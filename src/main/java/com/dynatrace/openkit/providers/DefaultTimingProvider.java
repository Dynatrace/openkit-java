package com.dynatrace.openkit.providers;

public class DefaultTimingProvider implements TimingProvider {

    private long lastInitTime = 0;
    private long clusterTimeOffset = 0;
    private boolean isTimeSyncSupported = true;


    @Override
    public long provideTimestampInMilliseconds() {
        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long milliseconds) throws InterruptedException {

        Thread.sleep(milliseconds);
    }

    @Override
    public synchronized void initialize(long clusterTimeOffset, boolean isTimeSyncSupported) {
        // set init time in milliseconds since 1970-01-01
        lastInitTime = provideTimestampInMilliseconds();
        this.isTimeSyncSupported = isTimeSyncSupported;
        if (isTimeSyncSupported) {
            this.clusterTimeOffset = clusterTimeOffset;
        } else {
            this.clusterTimeOffset = 0;
        }
    }

    @Override
    public synchronized boolean isTimeSyncSupported() {
        return isTimeSyncSupported;
    }

    @Override
    public synchronized long convertToClusterTime(long timestamp) {
        return timestamp + clusterTimeOffset;
    }

    @Override
    public synchronized long getLastInitTimeInClusterTime() {
        return lastInitTime + clusterTimeOffset;
    }

    @Override
    public synchronized long getTimeSinceLastInitTime() {
        return provideTimestampInMilliseconds() - lastInitTime;
    }

    @Override
    public synchronized long getTimeSinceLastInitTime(long timestamp) {
        return timestamp - lastInitTime;
    }
}
