package com.dynatrace.openkit.core.configuration;

/**
 * Configuration for beacon cache.
 */
public class BeaconCacheConfiguration {

    private long maxRecordAge;
    private int cacheSizeLowerBound;
    private int cacheSizeUpperBound;

    /**
     * Constructor
     *
     * @param maxRecordAge Maximum record age
     * @param cacheSizeLowerBound lower memory limit for cache
     * @param cacheSizeUpperBound upper memory limit for cache
     */
    public BeaconCacheConfiguration(long maxRecordAge, int cacheSizeLowerBound, int cacheSizeUpperBound) {

        this.maxRecordAge = maxRecordAge;
        this.cacheSizeLowerBound = cacheSizeLowerBound;
        this.cacheSizeUpperBound = cacheSizeUpperBound;
    }

    /**
     * Get maximum record age.
     */
    public long getMaxRecordAge() {
        return maxRecordAge;
    }

    /**
     * Get lower memory limit for cache.
     */
    public int getCacheSizeLowerBound() {
        return cacheSizeLowerBound;
    }

    /**
     * Get upper memory limit for cache.
     */
    public int getCacheSizeUpperBound() {
        return cacheSizeUpperBound;
    }
}
