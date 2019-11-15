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

import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.protocol.StatusResponse;

import java.util.List;

/**
 * The sending state, when init is completed and capturing is turned on.
 *
 * <p>
 * Transitions to:
 * <ul>
 * <li>{@link BeaconSendingCaptureOffState} if capturing is turned off</li>
 * <li>{@link BeaconSendingFlushSessionsState} on shutdown</li>
 * </ul>
 * </p>
 */
class BeaconSendingCaptureOnState extends AbstractBeaconSendingState {

    BeaconSendingCaptureOnState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        context.sleep();

        // send new session request for all sessions that are new
        StatusResponse newSessionsResponse = sendNewSessionRequests(context);
        if (BeaconSendingResponseUtil.isTooManyRequestsResponse(newSessionsResponse)) {
            // server is currently overloaded, temporarily switch to capture off
            context.setNextState(new BeaconSendingCaptureOffState(newSessionsResponse.getRetryAfterInMilliseconds()));
            return;
        }

        // send all finished sessions
        StatusResponse finishedSessionsResponse = sendFinishedSessions(context);
        if (BeaconSendingResponseUtil.isTooManyRequestsResponse(finishedSessionsResponse)) {
            // server is currently overloaded, temporarily switch to capture off
            context.setNextState(new BeaconSendingCaptureOffState(finishedSessionsResponse.getRetryAfterInMilliseconds()));
            return;
        }

        // check if we need to send open sessions & do it if necessary
        StatusResponse openSessionsResponse = sendOpenSessions(context);
        if (BeaconSendingResponseUtil.isTooManyRequestsResponse(openSessionsResponse)) {
            // server is currently overloaded, temporarily switch to capture off
            context.setNextState(new BeaconSendingCaptureOffState(openSessionsResponse.getRetryAfterInMilliseconds()));
            return;
        }

        // collect the last status response
        StatusResponse lastStatusResponse = newSessionsResponse;
        if (openSessionsResponse != null) {
            lastStatusResponse = openSessionsResponse;
        } else if (finishedSessionsResponse != null) {
            lastStatusResponse = finishedSessionsResponse;
        }

        // handle the last statusResponse received (or null if none was received) from the server
        handleStatusResponse(context, lastStatusResponse);
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingFlushSessionsState();
    }

    /**
     * Send new session requests for all sessions where we currently don't have a multiplicity configuration.
     *
     * @param context The state context.
     * @return The last status response received.
     */
    private StatusResponse sendNewSessionRequests(BeaconSendingContext context) {

        StatusResponse statusResponse = null;
        List<SessionImpl> notConfiguredSessions = context.getAllNotConfiguredSessions();

        for (SessionImpl session : notConfiguredSessions) {
            if (!session.canSendNewSessionRequest()) {
                // already exceeded the maximum number of session requests, disable any further data collecting
                session.disableCapture();
                continue;
            }

            statusResponse = context.getHTTPClient().sendNewSessionRequest();
            if (BeaconSendingResponseUtil.isSuccessfulResponse(statusResponse)) {
                StatusResponse mergedStatusResponse = context.getLastStatusResponse().merge(statusResponse);
                ServerConfiguration newServerConfig = ServerConfiguration.from(mergedStatusResponse);
                session.updateServerConfiguration(newServerConfig);
            } else if (BeaconSendingResponseUtil.isTooManyRequestsResponse(statusResponse)) {
                // server is currently overloaded, return immediately
                break;
            } else {
                // any other unsuccessful response
                session.decreaseNumRemainingSessionRequests();
            }
        }

        return statusResponse;
    }

    /**
     * Send all sessions which have been finished previously.
     *
     * @param context The state's context
     * @return The last status response received.
     */
    private StatusResponse sendFinishedSessions(BeaconSendingContext context) {

        StatusResponse statusResponse = null;
        // check if there's finished Sessions to be sent -> immediately send beacon(s) of finished Sessions
        List<SessionImpl> finishedSessions = context.getAllFinishedAndConfiguredSessions();

        for (SessionImpl finishedSession : finishedSessions) {
            if (finishedSession.isDataSendingAllowed()) {
                statusResponse = finishedSession.sendBeacon(context.getHTTPClientProvider());
                if (!BeaconSendingResponseUtil.isSuccessfulResponse(statusResponse)) {
                    // something went wrong,
                    if (BeaconSendingResponseUtil.isTooManyRequestsResponse(statusResponse) || !finishedSession.isEmpty()) {
                        break; //  sending did not work, break out for now and retry it later
                    }
                }
            }

            // session was sent/is not allowed to be sent - so remove it from beacon cache
            context.removeSession(finishedSession); // remove the finished session from the cache
            finishedSession.clearCapturedData();
            finishedSession.close(); // The session is already closed/ended at this point. This call avoids a static code warning.
        }

        return statusResponse;
    }

    /**
     * Check if the send interval (configured by server) has expired and start to send open sessions if it has expired.
     *
     * @param context The state's context
     * @return The last status response received.
     */
    private StatusResponse sendOpenSessions(BeaconSendingContext context) {

        StatusResponse statusResponse = null;

        long currentTimestamp = context.getCurrentTimestamp();
        if (currentTimestamp <= context.getLastOpenSessionBeaconSendTime() + context.getSendInterval()) {
            return null;
        }

        List<SessionImpl> openSessions = context.getAllOpenAndConfiguredSessions();
        for (SessionImpl session : openSessions) {
            if (session.isDataSendingAllowed()) {
                statusResponse = session.sendBeacon(context.getHTTPClientProvider());
                if (BeaconSendingResponseUtil.isTooManyRequestsResponse(statusResponse)) {
                    // server is currently overloaded, return immediately
                    break;
                }
            } else {
                session.clearCapturedData();
            }
        }

        context.setLastOpenSessionBeaconSendTime(currentTimestamp);

        return statusResponse;
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
