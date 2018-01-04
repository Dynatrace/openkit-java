package com.dynatrace.openkit.providers;

/**
 * Interface providing timing related functionality.
 */
public interface TimingProvider {

    /**
     * Provide the current timestamp in milliseconds.
     */
    long provideTimestampInMilliseconds();

    /**
     * Sleep given amount of milliseconds.
     *
     * @throws InterruptedException When the sleep call gets interrupted.
     */
    void sleep(long milliseconds) throws InterruptedException;

    /**
     * Initialize timing provider with cluster time offset. If {@code false} is provided
     * for {@code isTimeSyncSupported}, the cluster offset is set to 0.
     *
     * @param clusterTimeOffset
     * @param isTimeSyncSupported
     */
    void initialize(long clusterTimeOffset, boolean isTimeSyncSupported);

    /**
     * Returns whether time sync is supported or not
     *
     * @return {@code true} if time sync is supported otherwise {@code false}
     */
    boolean isTimeSyncSupported();

    /**
     * Converts a local timestamp to cluster time.
     *
     * @param timestamp Timestamp in local time
     * @return Returns local time if not time synced or if not yet initialized
     */
    long convertToClusterTime(long timestamp);

    /**
     * Gets the last init time in cluster time, or 0 if not yet initialized
     *
     * @return
     */
    long getLastInitTimeInClusterTime();

    /**
     * Gets the time since last init in milliseconds, or since 1970-01-01 if not yet initialized
     *
     * @return
     */
    long getTimeSinceLastInitTime();

    /**
     * Gets the time between provided timestamp and last init in milliseconds, or between provided
     * timestamp and 1970-01-01 if not yet initialized
     *
     * @param timestamp
     * @return
     */
    long getTimeSinceLastInitTime(long timestamp);
}
