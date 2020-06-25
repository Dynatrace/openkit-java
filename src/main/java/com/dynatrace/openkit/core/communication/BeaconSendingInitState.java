/**
 * Copyright 2018-2020 Dynatrace LLC
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
 * <li>{@link BeaconSendingCaptureOnState} if initial status request succeeded and capturing is enabled.</li>
 * <li>{@link BeaconSendingCaptureOffState} if initial status request succeeded and capturing is disabled.</li>
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
    private static final int MAX_INITIAL_STATUS_REQUEST_RETRIES = 5;
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

        // execute the status request until we get a response
        StatusResponse statusResponse = executeStatusRequest(context);

        if (context.isShutdownRequested()) {
            // shutdown was requested -> abort init with failure
            // transition to shutdown state is handled by base class
            context.initCompleted(false);
        } else if (BeaconSendingResponseUtil.isSuccessfulResponse(statusResponse)) {
            // success -> continue with capture on/off depending on context
            context.handleStatusResponse(statusResponse);
            context.setNextState(context.isCaptureOn()
                    ? new BeaconSendingCaptureOnState()
                    : new BeaconSendingCaptureOffState());
            context.initCompleted(true);
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

    /**
     * Execute status requests, until a successful response was received or shutdown was requested.
     *
     * @param context The state's context
     * @return The last received status response, which might be erroneous if shutdown has been requested.
     *
     * @throws InterruptedException Thrown if the current thread has been interrupted.
     */
    private StatusResponse executeStatusRequest(BeaconSendingContext context) throws InterruptedException {

        StatusResponse statusResponse;

        while (true) {
            long currentTimestamp = context.getCurrentTimestamp();
            context.setLastOpenSessionBeaconSendTime(currentTimestamp);
            context.setLastStatusCheckTime(currentTimestamp);

            statusResponse = BeaconSendingRequestUtil.sendStatusRequest(context, MAX_INITIAL_STATUS_REQUEST_RETRIES, INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
            if (context.isShutdownRequested() || BeaconSendingResponseUtil.isSuccessfulResponse(statusResponse)) {
                // shutdown was requested or a successful status response was received
                break;
            }

            long sleepTime = REINIT_DELAY_MILLISECONDS[reinitializeDelayIndex];
            if (BeaconSendingResponseUtil.isTooManyRequestsResponse(statusResponse)) {
                // in case of too many requests the server might send us a retry-after
                sleepTime = statusResponse.getRetryAfterInMilliseconds();

                // also temporarily disable capturing to avoid further server overloading
                context.disableCaptureAndClear();
            }

            // status request needs to be sent again after some delay
            context.sleep(sleepTime);

            reinitializeDelayIndex = Math.min(reinitializeDelayIndex + 1, REINIT_DELAY_MILLISECONDS.length - 1); // ensure no out of bounds
        }

        return statusResponse;
    }
}