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
}
