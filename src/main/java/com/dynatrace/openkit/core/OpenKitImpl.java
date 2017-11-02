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
import com.dynatrace.openkit.providers.DefaultHTTPClientProvider;
import com.dynatrace.openkit.providers.HTTPClientProvider;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

	// only set to true after initialized() was called and calls to the OpenKit are allowed
	private final AtomicBoolean initialized;

	// BeaconSender reference
	private final BeaconSender beaconSender;

	// AbstractConfiguration reference
	private AbstractConfiguration configuration;

	// dummy Session implementation, used if capture is set to off
	private static DummySession dummySessionInstance = new DummySession();

	// *** constructors ***

	public OpenKitImpl(AbstractConfiguration config) {
		this(config, new DefaultHTTPClientProvider());
	}

	protected OpenKitImpl(AbstractConfiguration config, HTTPClientProvider httpClientProvider) {
		configuration = config;
		beaconSender = new BeaconSender(configuration, httpClientProvider);
		initialized = new AtomicBoolean(false);
	}

	// *** OpenKit interface methods ***

	@Override
	public void initialize() {
		beaconSender.initialize();
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
			return new SessionImpl(configuration, clientIPAddress, beaconSender);
		} else {
			return dummySessionInstance;
		}
	}

	@Override
	public void shutdown() {
		beaconSender.shutdown();
	}

}
