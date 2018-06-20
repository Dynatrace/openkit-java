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
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Actual implementation of the {@link Session} interface.
 */
public class SessionImpl implements Session {

    private static final RootAction NULL_ROOT_ACTION = new NullRootAction();

    // end time of this Session
    private final AtomicLong endTime = new AtomicLong(-1);

    // BeaconSender and Beacon reference
    private final BeaconSender beaconSender;
    private final Beacon beacon;

    // used for taking care to really leave all Actions at the end of this Session
    private final SynchronizedQueue<Action> openRootActions = new SynchronizedQueue<Action>();

    private final Logger logger;

    // *** constructors ***

    SessionImpl(Logger logger, BeaconSender beaconSender, Beacon beacon) {
        this.logger = logger;
        this.beaconSender = beaconSender;
        this.beacon = beacon;

        beaconSender.startSession(this);
    }

    // *** Session interface methods ***


    @Override
    public void close() {
        end();
    }

    @Override
    public RootAction enterAction(String actionName) {
        if (actionName == null || actionName.isEmpty()) {
            logger.warning(this + "enterAction: actionName must not be null or empty");
            return NULL_ROOT_ACTION;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "enterAction(" + actionName + ")");
        }
        if (isSessionEnded()) {
            return NULL_ROOT_ACTION;
        }
        return new RootActionImpl(logger, beacon, actionName, openRootActions);
    }

    @Override
    public void identifyUser(String userTag) {
        if (userTag == null || userTag.isEmpty()) {
            logger.warning(this + "identifyUser: userTag must not be null or empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "identifyUser(" + userTag + ")");
        }
        if (!isSessionEnded()) {
            beacon.identifyUser(userTag);
        }
    }

    @Override
    public void reportCrash(String errorName, String reason, String stacktrace) {
        if (errorName == null || errorName.isEmpty()) {
            logger.warning(this + "reportCrash: errorName must not be null or empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportCrash(" + errorName + ", " + reason + ", " + stacktrace + ")");
        }
        if (!isSessionEnded()) {
            beacon.reportCrash(errorName, reason, stacktrace);
        }
    }

    @Override
    public void end() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "end()");
        }

        // check if end() was already called before by looking at endTime
        if (!endTime.compareAndSet(-1L, beacon.getCurrentTimestamp())) {
            return;
        }

        // leave all Root-Actions for sanity reasons
        while (!openRootActions.isEmpty()) {
            Action action = openRootActions.get();
            action.leaveAction();
        }

        // create end session data on beacon
        beacon.endSession(this);

        // finish session and stop managing it
        beaconSender.finishSession(this);
    }

    // *** public methods ***

    // sends the current Beacon state
    public StatusResponse sendBeacon(HTTPClientProvider clientProvider) {
        return beacon.send(clientProvider);
    }

    // *** getter methods ***

    public long getEndTime() {
        return endTime.get();
    }

    /**
     * Clears data that has been captured so far.
     *
     * <p>
     * This is called, when capturing is turned off to avoid having too much data.
     * </p>
     */
    public void clearCapturedData() {
        beacon.clearData();
    }

    /**
     * Test if this Session is empty or not.
     *
     * <p>
     * A session is considered to be empty, if it does not contain any action or event data.
     * </p>
     *
     * @return {@code true} if the session is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return beacon.isEmpty();
    }

    /**
     * Test if the session has already been ended.
     *
     * <p>
     * A session is considered as ended, if the endTime is set to something other than minus 1.
     * </p>
     *
     * @return {@code true} if the session has been ended already, {@code false} if the session is not ended yet.
     */
    boolean isSessionEnded() {
        return getEndTime() != -1L;
    }

    /**
     * Set the {@link BeaconConfiguration}
     */
    public void setBeaconConfiguration(BeaconConfiguration beaconConfiguration) {
        beacon.setBeaconConfiguration(beaconConfiguration);
    }

    /**
     * Get the {@link BeaconConfiguration}
     */
    public BeaconConfiguration getBeaconConfiguration() {
        return beacon.getBeaconConfiguration();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + "] ";
    }
}
