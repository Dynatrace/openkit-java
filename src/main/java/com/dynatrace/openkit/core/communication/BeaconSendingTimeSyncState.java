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

import com.dynatrace.openkit.protocol.Response;
import com.dynatrace.openkit.protocol.TimeSyncResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The state responsible for the time sync
 *
 * <p>
 * In this state a time sync is performed.
 * </p>
 *
 * <p>
 * Transition to:
 * <ul>
 * <li>{@link BeaconSendingCaptureOnState} if capturing is enabled ({@link BeaconSendingContext#isCaptureOn()} == {@code true})</li>
 * <li>{@link BeaconSendingCaptureOffState} if capturing is disabled ({@link BeaconSendingContext#isCaptureOn()} == {@code false}) or time sync failed</li>
 * <li>{@link BeaconSendingFlushSessionsState} on shutdown if not initial time sync</li>
 * <li>{@link BeaconSendingTerminalState} on shutdown if initial time sync</li>
 * </ul>
 * </p>
 */
class BeaconSendingTimeSyncState extends AbstractBeaconSendingState {

    static final int TIME_SYNC_REQUESTS = 5;
    private static final int TIME_SYNC_RETRY_COUNT = 5;
    static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);
    static final long TIME_SYNC_INTERVAL_IN_MILLIS = TimeUnit.HOURS.toMillis(2);

    private final boolean initialTimeSync;

    BeaconSendingTimeSyncState() {
        this(false);
    }

    BeaconSendingTimeSyncState(boolean initialTimeSync) {
        super(false);
        this.initialTimeSync = initialTimeSync;
    }

    /**
     * Helper method to check if a time sync is required.
     *
     * @return {@code true} if time sync is required, {@code false} otherwise.
     */
    static boolean isTimeSyncRequired(BeaconSendingContext context) {

        if (!context.isTimeSyncSupported()) {
            return false; // time sync not supported by server, therefore not required
        }

        return ((context.getLastTimeSyncTime() < 0)
            || (context.getCurrentTimestamp() - context.getLastTimeSyncTime() > TIME_SYNC_INTERVAL_IN_MILLIS));
    }

    private static void setNextState(BeaconSendingContext context) {

        // advance to next state
        if (context.isCaptureOn()) {
            context.setNextState(new BeaconSendingCaptureOnState());
        } else {
            context.setNextState(new BeaconSendingCaptureOffState());
        }
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        if (!isTimeSyncRequired(context)) {
            // make state transition based on configuration - since time sync is not supported or not required
            setNextState(context);
            return;
        }

        // execute time sync requests - note during initial sync it might be possible
        // that the time sync capability is disabled.
        TimeSyncRequestsResponse response = executeTimeSyncRequests(context);

        handleTimeSyncResponses(context, response);

        // mark init being completed if it's the initial time sync
        if (initialTimeSync) {
            context.initCompleted(true);
        }
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return initialTimeSync ? new BeaconSendingTerminalState() : new BeaconSendingFlushSessionsState();
    }

    /**
     * Execute the time synchronisation requests (HTTP requests).
     *
     * @param context State's context
     * @return Special container class
     */
    private TimeSyncRequestsResponse executeTimeSyncRequests(BeaconSendingContext context) throws InterruptedException {

        TimeSyncRequestsResponse response = new TimeSyncRequestsResponse();

        int retry = 0;
        long sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;

        // no check for shutdown here, time sync has to be completed
        while (response.timeSyncOffsets.size() < TIME_SYNC_REQUESTS && !context.isShutdownRequested()) {
            // doExecute time-sync request and take timestamps
            long requestSendTime = context.getCurrentTimestamp();
            TimeSyncResponse timeSyncResponse = context.getHTTPClient().sendTimeSyncRequest();
            long responseReceiveTime = context.getCurrentTimestamp();

            if (BeaconSendingResponseUtil.isSuccessfulResponse(timeSyncResponse)) {
                long requestReceiveTime = timeSyncResponse.getRequestReceiveTime();
                long responseSendTime = timeSyncResponse.getResponseSendTime();

                // check both timestamps for being > 0
                if ((requestReceiveTime > 0) && (responseSendTime > 0)) {
                    // if yes -> continue time-sync
                    long offset = ((requestReceiveTime - requestSendTime) + (responseSendTime - responseReceiveTime)) / 2;
                    response.timeSyncOffsets.add(offset);
                    retry = 0; // on successful response reset the retry count & initial sleep time
                    sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;
                } else {
                    // if no -> stop time sync, it's not supported
                    context.disableTimeSyncSupport();
                    break;
                }
            } else if (retry >= TIME_SYNC_RETRY_COUNT) {
                // retry limits exceeded
                break;
            } else if (BeaconSendingResponseUtil.isTooManyRequestsResponse(timeSyncResponse)) {
                // special handling for too many requests
                // clear all time sync offsets captured so far and store the response, which is handled later
                response.timeSyncOffsets.clear();
                response.response = timeSyncResponse;
                break;
            } else {
                context.sleep(sleepTimeInMillis);
                sleepTimeInMillis *= 2;
                retry++;
            }
        }

        return response;
    }

    @Override
    void onInterrupted(BeaconSendingContext context) {

        if (initialTimeSync) {
            context.initCompleted(false);
        }
    }

    private void handleTimeSyncResponses(BeaconSendingContext context, TimeSyncRequestsResponse response) {

        // time sync requests were *not* successful
        // either because of networking issues
        // -OR-
        // the server does not support time sync at all (e.g. AppMon).
        //
        // -> handle this case
        if (response.timeSyncOffsets.size() < TIME_SYNC_REQUESTS) {
            handleErroneousTimeSyncRequest(response.response, context);
            return;
        }

        //sanity check to catch case with div/0
        long calculatedOffset = computeClusterTimeOffset(response.timeSyncOffsets);

        // initialize time provider with cluster time offset
        context.initializeTimeSync(calculatedOffset, true);

        // also update the time when last time sync was performed to now
        context.setLastTimeSyncTime(context.getCurrentTimestamp());

        // set the next state
        setNextState(context);
    }

    private long computeClusterTimeOffset(List<Long> timeSyncOffsets) {
        // time sync requests were successful -> calculate cluster time offset
        Collections.sort(timeSyncOffsets);

        // take median value from sorted offset list
        long median = timeSyncOffsets.get(TIME_SYNC_REQUESTS / 2);

        // calculate variance from median
        long medianVariance = 0;
        for (int i = 0; i < TIME_SYNC_REQUESTS; i++) {
            long diff = timeSyncOffsets.get(i) - median;
            medianVariance += diff * diff;
        }
        medianVariance = medianVariance / TIME_SYNC_REQUESTS;

        // calculate cluster time offset as arithmetic mean of all offsets that are in range of 1x standard deviation
        long sum = 0;
        long count = 0;
        for (int i = 0; i < TIME_SYNC_REQUESTS; i++) {
            long diff = timeSyncOffsets.get(i) - median;
            if (diff * diff <= medianVariance) {
                sum += timeSyncOffsets.get(i);
                count++;
            }
        }

        if (count == 0) { // shouldn't come here under normal circumstances
            return 0; // prevents div/0
        }

        return Math.round(sum / (double) count);
    }

    private void handleErroneousTimeSyncRequest(TimeSyncResponse response, BeaconSendingContext context) {

        // if this is the initial sync try, we have to initialize the time provider
        // in every other case we keep the previous setting
        if (initialTimeSync) {
            context.initializeTimeSync(0, context.isTimeSyncSupported());
        }

        if (response != null && response.getResponseCode() == Response.HTTP_TOO_MANY_REQUESTS) {
            // server is currently overloaded, change to CaptureOff state temporarily
            context.setNextState(new BeaconSendingCaptureOffState(response.getRetryAfterInMilliseconds()));
        }
        else if (context.isTimeSyncSupported()) {
            // server supports time sync
            context.setNextState(new BeaconSendingCaptureOffState());
        } else {
            // otherwise set the next state based on the configuration
            setNextState(context);
        }
    }

    @Override
    public String toString() {
        return "TimeSync";
    }

    /**
     * Container class storing data for processing requests.
     */
    private static final class TimeSyncRequestsResponse {

        /**
         * List storing time sync offsets.
         */
        private final List<Long> timeSyncOffsets = new ArrayList<Long>(TIME_SYNC_REQUESTS);

        /**
         * TimeSync response which is only stored in case of "too many requests" response, otherwise it's always {@code null}.
         *
         * <p>
         * This is required for e.g. handling 429 response error.
         * </p>
         */
        private TimeSyncResponse response = null;
    }
}
