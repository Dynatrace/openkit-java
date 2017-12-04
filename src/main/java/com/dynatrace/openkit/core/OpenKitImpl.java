/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Device;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.*;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

	// BeaconSender reference
	private final BeaconSender beaconSender;

	// AbstractConfiguration reference
	private AbstractConfiguration configuration;
	private final ThreadIDProvider threadIDProvider;

	// dummy Session implementation, used if capture is set to off
	private static DummySession dummySessionInstance = new DummySession();

	// *** constructors ***

	public OpenKitImpl(AbstractConfiguration config) {
		this(config, new DefaultHTTPClientProvider(), new DefaultTimingProvider(), new DefaultThreadIDProvider());
	}

	protected OpenKitImpl(AbstractConfiguration config, HTTPClientProvider httpClientProvider, TimingProvider timingProvider, ThreadIDProvider threadIDProvider) {
		configuration = config;
		this.threadIDProvider = threadIDProvider;
		beaconSender = new BeaconSender(configuration, httpClientProvider, timingProvider);
	}

	/**
	 * Initialize this OpenKit instance.
	 *
	 * <p>
	 *     This method starts the {@link BeaconSender} and is called directly after
     *     the instance has been created in {@link com.dynatrace.openkit.OpenKitFactory}.
	 * </p>
	 */
	public void initialize() {
		beaconSender.initialize();
	}

	// *** OpenKit interface methods ***

	@Override
	public boolean waitForInitCompletion() {
		return beaconSender.waitForInit();
	}

	@Override
	public boolean waitForInitCompletion(long timeoutMillis) {
		return beaconSender.waitForInit(timeoutMillis);
	}

	@Override
	public boolean isInitialized() {
		return beaconSender.isInitialized();
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
		if (isInitialized() && configuration.isCapture()) {
			return new SessionImpl(configuration, clientIPAddress, beaconSender, threadIDProvider);
		} else {
			return dummySessionInstance;
		}
	}

	@Override
	public void shutdown() {
		beaconSender.shutdown();
	}

}
