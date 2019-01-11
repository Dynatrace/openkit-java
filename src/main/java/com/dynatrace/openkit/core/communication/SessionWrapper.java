/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper around the {@link SessionImpl} which holds additional data
 * required only in the communication package, therefore leave it package internal.
 */
class SessionWrapper {

    /**
     * The maximum number of "new session requests" to send per session.
     */
    private static final int MAX_NEW_SESSION_REQUESTS = 4;

    /**
     * The wrapped {@link SessionImpl}.
     */
    private final SessionImpl session;

    private int numNewSessionRequestsLeft = MAX_NEW_SESSION_REQUESTS;
    private final AtomicBoolean beaconConfigurationSet = new AtomicBoolean(false);
    private final AtomicBoolean sessionFinished = new AtomicBoolean(false);

    /**
     * Constructor taking the wrapped {@link SessionImpl}.
     * @param session The wrapped session.
     */
    SessionWrapper(SessionImpl session) {
        this.session = session;
    }

    /**
     * Updates the {@link BeaconConfiguration} in the wrapped session.
     *
     * <p>
     *     Besides updating the beacon configuration this also set the {@link #beaconConfigurationSet flag} to {@code true}.
     * </p>
     *
     * @param beaconConfiguration The new beacon configuration to set.
     */
    void updateBeaconConfiguration(BeaconConfiguration beaconConfiguration) {
        session.setBeaconConfiguration(beaconConfiguration);
        beaconConfigurationSet.set(true);
    }

    /**
     * Get a boolean flag indicating whether {@link #updateBeaconConfiguration(BeaconConfiguration)} has been
     * called before.
     *
     * @return
     */
    boolean isBeaconConfigurationSet() {
        return beaconConfigurationSet.get();
    }

    /**
     * Get Sessions beacon configuration.
     */
    BeaconConfiguration getBeaconConfiguration() {
        return session.getBeaconConfiguration();
    }

    /**
     * Finishes this session.
     *
     * <p>
     *     This only sets a finished flag to {@code true}, since this will be called by the
     *     session itself.
     * </p>
     */
    void finishSession() {
        sessionFinished.set(true);
    }

    /**
     * Get a  boolean flag indicating whether this session has been finished or not.
     * @return {@code true} if this session has been finished, {@code false} otherwise.
     */
    boolean isSessionFinished() {
        return sessionFinished.get();
    }

    /**
     * Will be called each time a new session request was made for a session.
     */
    void decreaseNumNewSessionRequests() {
        numNewSessionRequestsLeft -= 1;
    }

    /**
     * Get a flag indicating whether new session request can be sent, or not.
     * @return {@code true} new session request can be sent, {@code false} otherwise.
     */
    boolean canSendNewSessionRequest() {
        return numNewSessionRequestsLeft > 0;
    }

    /**
     * Clear captured data.
     */
    void clearCapturedData() {
        session.clearCapturedData();
    }

    /**
     * Send beacon forward call.
     */
    StatusResponse sendBeacon(HTTPClientProvider httpClientProvider) {
        return session.sendBeacon(httpClientProvider);
    }

    /**
     * Test if the Session is empty.
     *
     * @return {@code true} if Session is empty, {@code false} otherwise.
     */
    boolean isEmpty() {
        return session.isEmpty();
    }

    /**
     * Ends the session.
     */
    void end() {
        session.end();
    }

    /**
     * Get the wrapped {@link SessionImpl}.
     */
    SessionImpl getSession() {
        return session;
    }

    /**
     * Get a boolean value indicating whether data sending is allowed or not.
     * @return {@code true}
     */
    boolean isDataSendingAllowed() {
        return isBeaconConfigurationSet() && session.getBeaconConfiguration().getMultiplicity() > 0;
    }
}
