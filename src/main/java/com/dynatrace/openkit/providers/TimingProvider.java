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
	 * @exception InterruptedException When the sleep call gets interrupted.
     */
    void sleep(long milliseconds) throws InterruptedException;

    /**
     * Initialize timing provider with cluster time offset. If {@code false} is provided
     * for {@code isTimeSyncSupported}, the cluster offset is set to 0.
     *
     * @param clusterTimeOffset
     */
    void initialize(long clusterTimeOffset, boolean isTimeSyncSupported);

    /**
     * Returns whether a time sync was performed or not
     *
     * @return {@code true} is time sync was performed otherwise {@code false}
     */
    boolean isTimeSyncSupported();

    /**
     * Converts a local timestamp to cluster time. Returns local time if not time synced or if not yet initialized
     *
     * @param timestamp
     * @return
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
