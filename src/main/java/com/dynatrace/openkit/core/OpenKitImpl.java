/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.*;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

	// BeaconSender reference
	private final BeaconSender beaconSender;

	// AbstractConfiguration reference
	private final Configuration configuration;
	private final ThreadIDProvider threadIDProvider;
	private final TimingProvider timingProvider;

	// *** constructors ***

	public OpenKitImpl(Configuration config) {
		this(config, new DefaultHTTPClientProvider(), new DefaultTimingProvider(), new DefaultThreadIDProvider());
	}

	protected OpenKitImpl(Configuration config, HTTPClientProvider httpClientProvider, TimingProvider timingProvider, ThreadIDProvider threadIDProvider) {
		configuration = config;
		this.threadIDProvider = threadIDProvider;
		this.timingProvider = timingProvider;
		beaconSender = new BeaconSender(configuration, httpClientProvider, timingProvider);
	}

	/**
	 * Initialize this OpenKit instance.
	 *
	 * <p>
	 *     This method starts the {@link BeaconSender} and is called directly after
     *     the instance has been created in {@link com.dynatrace.openkit.AbstractOpenKitBuilder}.
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

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public Session createSession(String clientIPAddress) {
		// create beacon for session
		Beacon beacon = new Beacon(configuration, clientIPAddress, threadIDProvider, timingProvider);
		// create session
		return new SessionImpl(beaconSender, beacon);
	}

	@Override
	public void shutdown() {
		beaconSender.shutdown();
	}
}
