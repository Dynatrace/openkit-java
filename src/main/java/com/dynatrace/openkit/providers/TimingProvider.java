package com.dynatrace.openkit.providers;

public interface TimingProvider {

    /**
     * Provide the current timestamp in milliseconds.
     */
    long provideTimestampInMilliseconds();

    /**
     * Sleep given amount of milliseconds.
     */
    void sleep(long milliseconds);
}
