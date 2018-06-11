package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.OpenKitConstants;

public class BeaconConfiguration {

    private DataCollectionLevel dataCollectionLevel = OpenKitConstants.DEFAULT_DATA_COLLECTION_LEVEL;
    private CrashReportingLevel crashReportingLevel = OpenKitConstants.DEFAULT_CRASH_REPORTING_LEVEL;

    public BeaconConfiguration(DataCollectionLevel dataCollectionLevel, CrashReportingLevel crashReportingLevel) {
        this.dataCollectionLevel = dataCollectionLevel;
        this.crashReportingLevel = crashReportingLevel;
    }

    public DataCollectionLevel getDataCollectionLevel() {
        return this.dataCollectionLevel;
    }

    public CrashReportingLevel getCrashReportingLevel() {
        return this.crashReportingLevel;
    }
}
