package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.api.OpenKitConstants;

public class BeaconConfiguration {

    public static final DataCollectionLevel DEFAULT_DATA_COLLECTION_LEVEL = DataCollectionLevel.OFF;
    public static final CrashReportingLevel DEFAULT_CRASH_REPORTING_LEVEL = CrashReportingLevel.OFF;

    private DataCollectionLevel dataCollectionLevel = DEFAULT_DATA_COLLECTION_LEVEL;
    private CrashReportingLevel crashReportingLevel = DEFAULT_CRASH_REPORTING_LEVEL;

    /**
     * Constructor
     * @param dataCollectionLevel data collection level ( @see com.dynatrace.configuration.DataCollectionLevel )
     * @param crashReportingLevel crashReporting level ( @see com.dynatrace.configuration.CrashReportingLevel )
     */
    public BeaconConfiguration(DataCollectionLevel dataCollectionLevel, CrashReportingLevel crashReportingLevel) {
        this.dataCollectionLevel = dataCollectionLevel;
        this.crashReportingLevel = crashReportingLevel;
    }

    /**
     * Get the data collection level
     * @return data collection level
     */
    public DataCollectionLevel getDataCollectionLevel() {
        return this.dataCollectionLevel;
    }

    /**
     * Get the crash reporting level
     * @return crash reporting level
     */
    public CrashReportingLevel getCrashReportingLevel() {
        return this.crashReportingLevel;
    }
}
