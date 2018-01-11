package com.dynatrace.openkit.core.configuration;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BeaconCacheConfigurationTest {

    @Test
    public void getMaxRecordAge() {

        // then
        assertThat(new BeaconCacheConfiguration(-100L, 1, 2).getMaxRecordAge(),
            is(-100L));
        assertThat(new BeaconCacheConfiguration(0L, 1, 2).getMaxRecordAge(),
            is(0L));
        assertThat(new BeaconCacheConfiguration(200L, 1, 2).getMaxRecordAge(),
            is(200L));
    }

    @Test
    public void getCacheSizeLowerBound() {

        // then
        assertThat(new BeaconCacheConfiguration(0L, -1, 2).getCacheSizeLowerBound(),
            is(-1));
        assertThat(new BeaconCacheConfiguration(-1L, 0, 2).getCacheSizeLowerBound(),
            is(0));
        assertThat(new BeaconCacheConfiguration(0L, 1, 2).getCacheSizeLowerBound(),
            is(1));
    }

    @Test
    public void getCacheSizeUpperBound() {

        // then
        assertThat(new BeaconCacheConfiguration(0L, -1, -2).getCacheSizeUpperBound(),
            is(-2));
        assertThat(new BeaconCacheConfiguration(-1L, 1, 0).getCacheSizeUpperBound(),
            is(0));
        assertThat(new BeaconCacheConfiguration(0L, 1, 2).getCacheSizeUpperBound(),
            is(2));
    }
}
