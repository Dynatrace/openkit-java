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

import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.protocol.StatusResponse;

import java.util.List;

/**
 * In this state open sessions are finished. After that all sessions are sent to the server.
 * <p>
 *     Transition to:
 *     <ul>
 *         <li>{@link BeaconSendingTerminalState}</li>
 *     </ul>
 * </p>
 */
class BeaconSendingFlushSessionsState extends AbstractBeaconSendingState {

    BeaconSendingFlushSessionsState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) {

        // first get all sessions that were not yet configured
        List<SessionImpl> notConfiguredSessions = context.getAllNotConfiguredSessions();
        for (SessionImpl newSession : notConfiguredSessions) {
            // just turn on the capturing and send all remaining data
            newSession.enableCapture();
        }

        // end open sessions -> will be flushed afterwards
        List<SessionImpl> openSessions = context.getAllOpenAndConfiguredSessions();
        for (SessionImpl openSession : openSessions) {
            openSession.end();
        }

        // flush already finished (and previously ended) sessions
        boolean tooManyRequestsReceived = false;
        List<SessionImpl> finishedSessions = context.getAllFinishedAndConfiguredSessions();
        for (SessionImpl finishedSession : finishedSessions) {
            if (!tooManyRequestsReceived && finishedSession.isDataSendingAllowed()) {
                StatusResponse response = finishedSession.sendBeacon(context.getHTTPClientProvider(), context);
                if (BeaconSendingResponseUtil.isTooManyRequestsResponse(response)) {
                    tooManyRequestsReceived = true;
                }
            }
            finishedSession.clearCapturedData();
            finishedSession.close(); // The session is already closed/ended at this point. This call avoids a static code warning.
            context.removeSession(finishedSession);
        }

        // make last state transition to terminal state
        context.setNextState(new BeaconSendingTerminalState());
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingTerminalState();
    }

    @Override
    public String toString() {
        return "FlushSessions";
    }
}
