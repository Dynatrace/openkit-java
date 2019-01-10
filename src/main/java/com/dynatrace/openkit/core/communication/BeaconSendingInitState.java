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

import com.dynatrace.openkit.protocol.StatusResponse;

import java.util.concurrent.TimeUnit;

/**
 * Initial state for beacon sending.
 *
 * <p>
 * The initial state is used to retrieve the configuration from the server and update the configuration.
 * </p>
 *
 * <p>
 * Transition to:
 * <ul>
 * <li>{@link BeaconSendingTerminalState} upon shutdown request</li>
 * <li>{@link BeaconSendingTimeSyncState} if initial status request succeeded</li>
 * </ul>
 * </p>
 */
class BeaconSendingInitState extends AbstractBeaconSendingState {

    /**
     * Times to use as delay between consecutive re-executions of this state, when no state transition is performed.
     */
    static final long[] REINIT_DELAY_MILLISECONDS = {
        TimeUnit.MINUTES.toMillis(1),
        TimeUnit.MINUTES.toMillis(5),
        TimeUnit.MINUTES.toMillis(15),
        TimeUnit.HOURS.toMillis(1),
        TimeUnit.HOURS.toMillis(2),
    };

    /**
     * Maximum number of retries
     */
    static final int MAX_INITIAL_STATUS_REQUEST_RETRIES = 5;
    static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Index to re-initialize delays.
     */
    private int reinitializeDelayIndex = 0;

    BeaconSendingInitState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        StatusResponse statusResponse;
        while (true) {
            long currentTimestamp = context.getCurrentTimestamp();
            context.setLastOpenSessionBeaconSendTime(currentTimestamp);
            context.setLastStatusCheckTime(currentTimestamp);

            statusResponse = BeaconSendingRequestUtil.sendStatusRequest(context, MAX_INITIAL_STATUS_REQUEST_RETRIES, INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
            if (context.isShutdownRequested() || statusResponse != null) {
                // shutdown was requested or a status response was received
                break;
            }

            // status request needs to be sent again after some delay
            context.sleep(REINIT_DELAY_MILLISECONDS[reinitializeDelayIndex]);

            reinitializeDelayIndex = Math.min(reinitializeDelayIndex + 1, REINIT_DELAY_MILLISECONDS.length - 1); // ensure no out of bounds
        }

        if (context.isShutdownRequested()) {
            // shutdown was requested -> abort init with failure
            // transition to shutdown state is handled by base class
            context.initCompleted(false);
        } else if (statusResponse != null) {
            // success -> continue with time sync
            context.handleStatusResponse(statusResponse);
            context.setNextState(new BeaconSendingTimeSyncState(true));
        }
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingTerminalState();
    }

    @Override
    void onInterrupted(BeaconSendingContext context) {
        context.initCompleted(false);
    }

    @Override
    public String toString() {
        return "Initial";
    }
}