/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimeProvider;

/**
 * Actual implementation of the {@link Session} interface.
 */
public class SessionImpl implements Session {

	// end time of this Session
	private long endTime = -1;

	// BeaconSender and Beacon reference
	private final BeaconSender beaconSender;
	private final Beacon beacon;

	// used for taking care to really leave all Actions at the end of this Session
	private SynchronizedQueue<Action> openRootActions = new SynchronizedQueue<Action>();

	// *** constructors ***

	SessionImpl(BeaconSender beaconSender, Beacon beacon) {
		this.beaconSender = beaconSender;
		this.beacon = beacon;

		beaconSender.startSession(this);
	}

	// *** Session interface methods ***

	@Override
	public RootAction enterAction(String actionName) {
		return new RootActionImpl(beacon, actionName, openRootActions);
	}

	@Override
	public void identifyUser(String userId) {
		beacon.identifyUser(userId);
	}

	@Override
	public void reportCrash(String errorName, String reason, String stacktrace) {
		beacon.reportCrash(errorName, reason, stacktrace);
	}

	@Override
	public void end() {
		// check if end() was already called before by looking at endTime
		if (endTime != -1) {
			return;
		}

		// leave all Root-Actions for sanity reasons
		while (!openRootActions.isEmpty()) {
			Action action = openRootActions.get();
			action.leaveAction();
		}

		endTime = TimeProvider.getTimestamp();

		// create end session data on beacon
		beacon.endSession(this);

		// finish session and stop managing it
		beaconSender.finishSession(this);
	}

	// *** public methods ***

	// sends the current Beacon state
	public StatusResponse sendBeacon(HTTPClientProvider clientProvider, int numRetries) throws InterruptedException {
		return beacon.send(clientProvider, numRetries);
	}

	// *** getter methods ***

	public long getEndTime() {
		return endTime;
	}

}
