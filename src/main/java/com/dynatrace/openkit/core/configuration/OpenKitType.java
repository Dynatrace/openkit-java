package com.dynatrace.openkit.core.configuration;

/**
 * This enum defines if an OpenKit instance should be used for AppMon or Dynatrace.
 */
public enum OpenKitType {

    APPMON(1),      // AppMon: default monitor URL name contains "dynaTraceMonitor" and default Server ID is 1
    DYNATRACE(1);   // Dynatrace: default monitor URL name contains "mbeacon" and default Server ID is 1

    private int defaultServerID;

    OpenKitType(int defaultServerID) {
        this.defaultServerID = defaultServerID;
    }

    public int getDefaultServerID() {
        return defaultServerID;
    }
}