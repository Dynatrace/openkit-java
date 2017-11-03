package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.StatusResponse;

/**
 * Initial state for beacon sending.
 *
 * <p>
 *     The initial state is used to retrieve the configuration from the server and update the configuration.
 * </p>
 */
class BeaconSendingInitState extends BeaconSendingState {

    static final int MAX_INITIAL_STATUS_REQUEST_RETRIES = 5;

    @Override
    void execute(BeaconSendingContext context) {

        long currentTimestamp = context.getCurrentTimestamp();
        context.setLastOpenSessionBeaconSendTime(currentTimestamp);
        context.setLastStatusCheckTime(currentTimestamp);

        StatusResponse statusResponse = null;
        int retry = 0;
        do {
            retry++;
            statusResponse = context.getClient().sendStatusRequest();

            // if no (valid) status response was received -> sleep 1s and then retry (max 5 times altogether)
            if (statusResponse == null) {
                context.sleep();
            }
        } while (!context.isShutdownRequested() && (statusResponse == null) && (retry < MAX_INITIAL_STATUS_REQUEST_RETRIES));

        if (context.isShutdownRequested() || (statusResponse == null)) {
            // initial configuration request was either terminated from outside or the config could not be retrieved
            // TODO stefan.eberl@dynatrace.com - Define some better error handling if init failed!
            context.initCompleted(false);
            context.requestShutdown();
            context.setCurrentState(new BeaconSendingTerminalState());
        } else {
            // success -> continue with time sync
            context.handleStatusResponse(statusResponse);
            context.setCurrentState(new BeaconSendingTimeSyncState(true));
        }
    }
}
