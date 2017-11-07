package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.providers.TimeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class BeaconSendingTimeSyncState extends AbstractBeaconSendingState {

    static final int TIME_SYNC_REQUESTS = 5;
    static final int TIME_SYNC_RETRY_COUNT = 5;
    static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    private final boolean initialTimeSync;

    BeaconSendingTimeSyncState() {
        this(false);
    }

    BeaconSendingTimeSyncState(boolean initialTimeSync) {
        super(false);
        this.initialTimeSync = initialTimeSync;
    }

    @Override
    void doExecute(BeaconSendingContext context) {

        List<Long> timeSyncOffsets;
        try {
            timeSyncOffsets = executeTimeSyncRequests(context);
        } catch (InterruptedException e) {
            // assume time sync was shut down
            context.initCompleted(false);
            context.setCurrentState(new BeaconSendingTerminalState());
            Thread.currentThread().interrupt(); // re-interrupt
            return;
        }

        // time sync requests were *not* successful -> use 0 as cluster time offset
        if (timeSyncOffsets.size() < TIME_SYNC_REQUESTS) {
            // if this is the initial sync try, we have to initialize the time provider
            // in every other case we keep the previous setting
            if (initialTimeSync) {
                TimeProvider.initialize(0, false);
            }

            // if time sync failed, always go to capture off state
            context.setCurrentState(new BeaconSendingStateCaptureOffState());
            return;
        }

        long clusterTimeOffset = computeClusterTimeOffset(timeSyncOffsets);

        // initialize time provider with cluster time offset
        TimeProvider.initialize(clusterTimeOffset, true);

        // advance to next state
        if (context.isCaptureOn()) {
            context.setCurrentState(new BeaconSendingStateCaptureOnState());
        } else {
            context.setCurrentState(new BeaconSendingStateCaptureOffState());
        }

        // mark init being completed if it's the initial time sync
        if (initialTimeSync) {
            context.initCompleted(true);
        }
        context.setLastTimeSyncTime(context.getCurrentTimestamp());
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingTerminalState();
    }

    /**
     * Execute the time synchronisation requests (HTTP requests).
     */
    private List<Long> executeTimeSyncRequests(BeaconSendingContext context) throws InterruptedException {

        List<Long> timeSyncOffsets = new ArrayList<Long>(TIME_SYNC_REQUESTS);

        int retry = 0;
        long sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;

        // no check for shutdown here, time sync has to be completed
        while (timeSyncOffsets.size() < TIME_SYNC_REQUESTS && retry++ < TIME_SYNC_RETRY_COUNT && !context.isShutdownRequested()) {
            // doExecute time-sync request and take timestamps
            long requestSendTime = context.getCurrentTimestamp();
            TimeSyncResponse timeSyncResponse = context.getHTTPClient().sendTimeSyncRequest();
            long responseReceiveTime = context.getCurrentTimestamp();

            if (timeSyncResponse != null) {
                long requestReceiveTime = timeSyncResponse.getRequestReceiveTime();
                long responseSendTime = timeSyncResponse.getResponseSendTime();

                // check both timestamps for being > 0
                if ((requestReceiveTime > 0) && (responseSendTime > 0)) {
                    // if yes -> continue time-sync
                    long offset = ((requestReceiveTime - requestSendTime) + (responseSendTime - responseReceiveTime)) / 2;
                    timeSyncOffsets.add(offset);
                    retry = 0; // on successful response reset the retry count & initial sleep time
                    sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;
                } else {
                    // if no -> stop time sync, it's not supported
                    break;
                }
            } else {
                context.sleep(sleepTimeInMillis);
                sleepTimeInMillis *= 2;
            }
        }

        return timeSyncOffsets;
    }

    private long computeClusterTimeOffset(List<Long> timeSyncOffsets) {
        // time sync requests were successful -> calculate cluster time offset
        Collections.sort(timeSyncOffsets);

        // take median value from sorted offset list
        long median = timeSyncOffsets.get(TIME_SYNC_REQUESTS / 2);

        // calculate variance from median
        long medianVariance = 0;
        for (int i = 0;
             i < TIME_SYNC_REQUESTS;
             i++) {
            long diff = timeSyncOffsets.get(i) - median;
            medianVariance += diff * diff;
        }
        medianVariance = medianVariance / TIME_SYNC_REQUESTS;

        // calculate cluster time offset as arithmetic mean of all offsets that are in range of 1x standard deviation
        long sum = 0;
        long count = 0;
        for (int i = 0;
             i < TIME_SYNC_REQUESTS;
             i++) {
            long diff = timeSyncOffsets.get(i) - median;
            if (diff * diff <= medianVariance) {
                sum += timeSyncOffsets.get(i);
                count++;
            }
        }

        return (long) Math.round(sum / (double) count);
    }
}
