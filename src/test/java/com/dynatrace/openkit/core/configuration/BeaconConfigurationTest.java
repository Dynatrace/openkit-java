package com.dynatrace.openkit.core.configuration;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BeaconConfigurationTest {

    @Test
    public void getDataCollectionLevel() {

        // then
        assertThat(new BeaconConfiguration(DataCollectionLevel.OFF, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(DataCollectionLevel.OFF));
        assertThat(new BeaconConfiguration(DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(DataCollectionLevel.PERFORMANCE));
        assertThat(new BeaconConfiguration(DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(DataCollectionLevel.USER_BEHAVIOR));
    }

    @Test
    public void getCrashReportingLevel() {

        // then
        assertThat(new BeaconConfiguration(DataCollectionLevel.OFF, CrashReportingLevel.OFF).getCrashReportingLevel(),
            is(CrashReportingLevel.OFF));
        assertThat(new BeaconConfiguration(DataCollectionLevel.OFF, CrashReportingLevel.OPT_IN_CRASHES).getCrashReportingLevel(),
            is(CrashReportingLevel.OPT_IN_CRASHES));
    }
}
