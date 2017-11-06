package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.providers.TimeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BeaconSendingTimeSyncState extends BeaconSendingState {

    static final int TIME_SYNC_REQUESTS = 5;
    static final int TIME_SYNC_RETRY_COUNT = 20;

    private final boolean initialTimeSync;

    BeaconSendingTimeSyncState() {
        this(false);
    }

    BeaconSendingTimeSyncState(boolean initialTimeSync) {
        this.initialTimeSync = initialTimeSync;
    }

    @Override
    void execute(BeaconSendingContext context) {

        List<Long> timeSyncOffsets = executeTimeSyncRequests(context);

        // time sync requests were *not* successful -> use 0 as cluster time offset
        if (timeSyncOffsets.size() < TIME_SYNC_REQUESTS) {
            // if this is the initial sync try, we have to initialize the time provider
            // in every other case we keep the previous setting
            if (initialTimeSync) {
                TimeProvider.initialize(0, false);
            }
            return;
        }

        long clusterTimeOffset = computeClusterTimeOffset(timeSyncOffsets);

        // initialize time provider with cluster time offset
        TimeProvider.initialize(clusterTimeOffset, true);
    }

	/**
	 * Execute the time synchronisation requests (HTTP requests).
	 */
	private List<Long> executeTimeSyncRequests(BeaconSendingContext context) {

        int retry = 0;
        List<Long> timeSyncOffsets = new ArrayList<Long>(TIME_SYNC_REQUESTS);

        // no check for shutdown here, time sync has to be completed
        while (timeSyncOffsets.size() < TIME_SYNC_REQUESTS && retry++ < TIME_SYNC_RETRY_COUNT) {
            // execute time-sync request and take timestamps
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
                } else {
                    // if no -> stop time sync, it's not supported
                    break;
                }
            } else {
                context.sleep();
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

        return (long)Math.round(sum / (double) count);
    }
}
