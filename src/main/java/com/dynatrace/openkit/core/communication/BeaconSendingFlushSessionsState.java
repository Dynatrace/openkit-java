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

import com.dynatrace.openkit.core.SessionImpl;

/**
 * Un this state open sessions are finished. After that all sessions are sent tot the server.
 * <p>
 * <p>
 * Transition to:
 * <ul>
 * <li>{@link BeaconSendingTerminalState}</li>
 * </ul>
 * </p>
 */
class BeaconSendingFlushSessionsState extends AbstractBeaconSendingState {

    /**
     * Do not retry beacon sending on error.
     */
    static final int BEACON_SEND_RETRY_ATTEMPTS = 0;

    BeaconSendingFlushSessionsState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        // end open sessions -> will be flushed afterwards
        SessionImpl[] openSessions = context.getAllOpenSessions();
        for (SessionImpl openSession : openSessions) {
            openSession.end();
        }

        // flush already finished (and previously ended) sessions
        SessionImpl finishedSession = context.getNextFinishedSession();
        while (finishedSession != null) {
            finishedSession.sendBeacon(context.getHTTPClientProvider(), BEACON_SEND_RETRY_ATTEMPTS);
            finishedSession = context.getNextFinishedSession();
        }

        // make last state transition to terminal state
        context.setNextState(new BeaconSendingTerminalState());
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingTerminalState();
    }
}
