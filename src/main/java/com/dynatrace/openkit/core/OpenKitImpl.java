/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.openkit.api.Device;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

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

	// only set to true after initialized() was called and calls to the OpenKit are allowed
	private final AtomicBoolean initialized;

	// Configuration reference
	private Configuration configuration;

	// dummy Session implementation, used if capture is set to off
	private static DummySession dummySessionInstance = new DummySession();

	// *** constructors ***

	public OpenKitImpl(OpenKitType type, String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
		configuration = new Configuration(applicationName, applicationID, visitorID, endpointURL, type, verbose);
		initialized = new AtomicBoolean(false);
	}

	// *** OpenKit interface methods ***

	@Override
	public void initialize() {
		configuration.initialize();
		initialized.set(true);
	}

	@Override
	public Device getDevice() {
		return configuration.getDevice();
	}

	@Override
	public void setApplicationVersion(String applicationVersion) {
		configuration.setApplicationVersion(applicationVersion);
	}

	@Override
	public Session createSession(String clientIPAddress) {
		if (initialized.get() && configuration.isCapture()) {
			return new SessionImpl(configuration, clientIPAddress);
		} else {
			return dummySessionInstance;
		}
	}

	@Override
	public void shutdown() {
		configuration.shutdown();
	}

}
