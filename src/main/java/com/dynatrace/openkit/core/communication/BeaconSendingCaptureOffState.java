/**
 * Copyright 2018-2021 Dynatrace LLC
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
 * State where no data is captured. Periodically issues a status request to check if capturing shall be re-enabled.
 * The check interval is defined in {@link BeaconSendingCaptureOffState#INITIAL_RETRY_SLEEP_TIME_MILLISECONDS}.
 *
 * <p>
 * Transition to:
 * <ul>
 * <li>{@link BeaconSendingCaptureOnState} if capturing is re-enabled</li>
 * <li>{@link BeaconSendingFlushSessionsState} on shutdown</li>
 * </ul>
 * </p>
 */
class BeaconSendingCaptureOffState extends AbstractBeaconSendingState {

    /**
     * number of retries for the status request
     */
    private static final int STATUS_REQUEST_RETRIES = 5;
    private static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    /**
     * maximum time to wait till next status check
     */
    private static final long STATUS_CHECK_INTERVAL = TimeUnit.HOURS.toMillis(2);

    /**
     * Sleep time in milliseconds.
     */
    final long sleepTimeInMilliseconds;

    /**
     * Create CaptureOff state with default sleep behavior.
     */
    BeaconSendingCaptureOffState() {
        this(-1L);
    }

    /**
     * Create CaptureOff state with explicitly set sleep time.
     *
     * @param sleepTimeInMilliseconds The number of milliseconds to sleep.
     */
    BeaconSendingCaptureOffState(long sleepTimeInMilliseconds) {
        super(false);
        this.sleepTimeInMilliseconds = sleepTimeInMilliseconds;
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        // disable capturing - avoid collecting further data
        context.disableCaptureAndClear();

        long currentTime = context.getCurrentTimestamp();

        long delta = sleepTimeInMilliseconds > 0
            ? sleepTimeInMilliseconds
            : STATUS_CHECK_INTERVAL - (currentTime - context.getLastStatusCheckTime());
        if (delta > 0 && !context.isShutdownRequested()) {
            context.sleep(delta);
        }
        StatusResponse statusResponse = BeaconSendingRequestUtil.sendStatusRequest(context, STATUS_REQUEST_RETRIES, INITIAL_RETRY_SLEEP_TIME_MILLISECONDS);
        handleStatusResponse(context, statusResponse);

        // update the last status check time in any case
        context.setLastStatusCheckTime(currentTime);
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingFlushSessionsState();
    }

    private static void handleStatusResponse(BeaconSendingContext context, StatusResponse statusResponse) {

        if (statusResponse != null) {
            // handle status response, even if it's erroneous
            // if it's an erroneous response capturing is disabled
            context.handleStatusResponse(statusResponse);
        }

        if (BeaconSendingResponseUtil.isTooManyRequestsResponse(statusResponse)) {
            // received "too many requests" response
            // in this case stay in capture off state and use the retry-after delay for sleeping
            context.setNextState(new BeaconSendingCaptureOffState(statusResponse.getRetryAfterInMilliseconds()));
        } else if (BeaconSendingResponseUtil.isSuccessfulResponse(statusResponse) && context.isCaptureOn()) {
            // capturing is re-enabled again, but only if we received a response from the server
            context.setNextState(new BeaconSendingCaptureOnState());
        }
    }

    @Override
    public String toString() {
        return "CaptureOff";
    }
}

