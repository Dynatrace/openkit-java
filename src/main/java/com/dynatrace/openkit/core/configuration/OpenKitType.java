package com.dynatrace.openkit.core.configuration;

/**
 * This enum defines if an OpenKit instance should be used for AppMon or Dynatrace.
 */
public enum OpenKitType {

	APPMON("dynaTraceMonitor", 1),			// AppMon: default monitor URL name contains "dynaTraceMonitor" and default Server ID is 1
	DYNATRACE("mbeacon", -1);				// Dynatrace: default monitor URL name contains "mbeacon" and default Server ID is -1

	private String defaultMonitorName;
	private int defaultServerID;

	OpenKitType(String defaultMonitorName, int defaultServerID) {
		this.defaultMonitorName = defaultMonitorName;
		this.defaultServerID = defaultServerID;
	}

	public String getDefaultMonitorName() {
		return defaultMonitorName;
	}

	public int getDefaultServerID() {
		return defaultServerID;
	}
}