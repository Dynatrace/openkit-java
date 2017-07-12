/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

/**
 * Abstract class for providing timestamps, time durations and cluster time conversions.
 */
public abstract class TimeProvider {

	private static TimeProvider timeProvider = new LocalTimeProvider();
	private static long lastInitTime = 0;
	private static long clusterTimeOffset = 0;
	private static boolean timeSynced = false;

	protected abstract long provideTimestamp();

	// initialize time provider with cluster time offset or 0 if not time synced
	public static void initialize(long clusterTimeOffset, boolean timeSynced) {
		// set init time in milliseconds since 1970-01-01
		lastInitTime = getTimestamp();
		TimeProvider.timeSynced = timeSynced;
		if (timeSynced) {
			TimeProvider.clusterTimeOffset = clusterTimeOffset;
		} else {
			TimeProvider.clusterTimeOffset = 0;
		}
	}

	public static boolean isTimeSynced() {
		return TimeProvider.timeSynced;
	}

	// convert a local timestamp to cluster time
	// return local time if not time synced or if not yet initialized
	public static long convertToClusterTime(long timestamp) {
		return timestamp + clusterTimeOffset;
	}

	// return timestamp in milliseconds since 1970-01-01 in local time
	public static long getTimestamp() {
		return timeProvider.provideTimestamp();
	}

	// return last init time in cluster time, or 0 if not yet initialized
	public static long getLastInitTimeInClusterTime() {
		return lastInitTime + clusterTimeOffset;
	}

	// return time since last init in milliseconds, or since 1970-01-01 if not yet initialized
	public static long getTimeSinceLastInitTime() {
		return timeProvider.provideTimestamp() - lastInitTime;
	}

	// return time between provided timestamp and last init in milliseconds, or between provided timestamp and 1970-01-01 if not yet initialized
	public static long getTimeSinceLastInitTime(long timestamp) {
		return timestamp - lastInitTime;
	}

	// FOR TESTS ONLY: set time provider
	public static void setTimeProvider(TimeProvider timeProvider) {
		TimeProvider.timeProvider = timeProvider;
	}

}
