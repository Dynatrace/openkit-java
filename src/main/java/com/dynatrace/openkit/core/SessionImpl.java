/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;

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
    public void identifyUser(String userTag) {
        beacon.identifyUser(userTag);
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

        endTime = beacon.getCurrentTimestamp();

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

    /**
     * Clears data that has been captured so far.
     * <p>
     * <p>
     * This is called, when capturing is turned off to avoid having too much data.
     * </p>
     */
    public void clearCapturedData() {

        beacon.clearData();
    }
}
