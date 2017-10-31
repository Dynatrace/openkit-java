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
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

	// only set to true after initialized() was called and calls to the OpenKit are allowed
	private final AtomicBoolean initialized;

	// AbstractConfiguration reference
	private AbstractConfiguration configuration;

	// dummy Session implementation, used if capture is set to off
	private static DummySession dummySessionInstance = new DummySession();

	// *** constructors ***

	public OpenKitImpl(AbstractConfiguration config) {
		configuration = config;
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
