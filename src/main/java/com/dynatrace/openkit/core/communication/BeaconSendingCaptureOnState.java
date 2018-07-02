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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.protocol.StatusResponse;

import java.util.List;

/**
 * The sending state, when init is completed and capturing is turned on.
 *
 * <p>
 * Transitions to:
 * <ul>
 * <li>{@link BeaconSendingTimeSyncState} if {@link BeaconSendingTimeSyncState#isTimeSyncRequired(BeaconSendingContext)} is {@code true}</li>
 * <li>{@link BeaconSendingCaptureOffState} if capturing is turned off</li>
 * <li>{@link BeaconSendingFlushSessionsState} on shutdown</li>
 * </ul>
 * </p>
 */
class BeaconSendingCaptureOnState extends AbstractBeaconSendingState {

    /**
     * store last received status response
     */
    private StatusResponse statusResponse = null;

    BeaconSendingCaptureOnState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        // check if time sync is required (from time to time a re-sync must be performed)
        if (BeaconSendingTimeSyncState.isTimeSyncRequired(context)) {
            // time re-sync required -> transition
            context.setNextState(new BeaconSendingTimeSyncState());
            return;
        }

        context.sleep();

        // send new session request for all sessions that are new
        sendNewSessionRequests(context);

        statusResponse = null;

        // send all finished sessions (this method may set this.statusResponse)
        sendFinishedSessions(context);

        // check if we need to send open sessions & do it if necessary (this method may set this.statusResponse)
        sendOpenSessions(context);

        // handle the last statusResponse received (or null if none was received) from the server
        handleStatusResponse(context, statusResponse);
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingFlushSessionsState();
    }

    private void sendNewSessionRequests(BeaconSendingContext context) {

        List<SessionWrapper> newSessions = context.getAllNewSessions();

        for (SessionWrapper session : newSessions) {
            if (!session.canSendNewSessionRequest()) {
                // already exceeded the maximum number of session requests, disable any further data collecting
                BeaconConfiguration currentConfiguration = session.getBeaconConfiguration();
                BeaconConfiguration newConfiguration = new BeaconConfiguration(0,
                    currentConfiguration.getDataCollectionLevel(), currentConfiguration.getCrashReportingLevel());
                session.updateBeaconConfiguration(newConfiguration);
                continue;
            }

            StatusResponse response = context.getHTTPClient().sendNewSessionRequest();
            if (response != null) {
                BeaconConfiguration currentConfiguration = session.getBeaconConfiguration();
                BeaconConfiguration newConfiguration = new BeaconConfiguration(response.getMultiplicity(),
                    currentConfiguration.getDataCollectionLevel(), currentConfiguration.getCrashReportingLevel());
                session.updateBeaconConfiguration(newConfiguration);
            } else {
                // did not retrieve any response from server, maybe the cluster is down?
                session.decreaseNumNewSessionRequests();
            }
        }
    }

    /**
     * Send all sessions which have been finished previously.
     *
     * @param context Context.
     */
    private void sendFinishedSessions(BeaconSendingContext context) {

        // check if there's finished Sessions to be sent -> immediately send beacon(s) of finished Sessions
        List<SessionWrapper> finishedSessions = context.getAllFinishedAndConfiguredSessions();

        for (SessionWrapper finishedSession : finishedSessions) {
            if (finishedSession.isDataSendingAllowed()) {
                statusResponse = finishedSession.sendBeacon(context.getHTTPClientProvider());
                if (statusResponse == null) {
                    // something went wrong,
                    if (!finishedSession.isEmpty()) {
                        break; //  sending did not work, break out for now and retry it later
                    }
                }
            }

            // session was sent/is not allowed to be sent - so remove it from beacon cache
            context.removeSession(finishedSession); // remove the finished session from the cache
            finishedSession.clearCapturedData();
            finishedSession.getSession().close(); // The session is already closed/ended at this point. This call avoids a static code warning.
        }
    }

    /**
     * Check if the send interval (configured by server) has expired and start to send open sessions if it has expired.
     *
     * @param context
     */
    private void sendOpenSessions(BeaconSendingContext context) {

        long currentTimestamp = context.getCurrentTimestamp();
        if (currentTimestamp <= context.getLastOpenSessionBeaconSendTime() + context.getSendInterval()) {
            return; // send interval to send open sessions has not expired yet
        }

        List<SessionWrapper> openSessions = context.getAllOpenAndConfiguredSessions();
        for (SessionWrapper session : openSessions) {
            if (session.isDataSendingAllowed()) {
                statusResponse = session.sendBeacon(context.getHTTPClientProvider());
            } else {
                session.clearCapturedData();
            }
        }

        context.setLastOpenSessionBeaconSendTime(currentTimestamp);
    }

    private static void handleStatusResponse(BeaconSendingContext context, StatusResponse statusResponse) {

        if (statusResponse == null) {
            return; // nothing to handle
        }

        context.handleStatusResponse(statusResponse);
        if (!context.isCaptureOn()) {
            // capturing is turned off -> make state transition
            context.setNextState(new BeaconSendingCaptureOffState());
        }
    }

    @Override
    public String toString() {
        return "CaptureOn";
    }
}
