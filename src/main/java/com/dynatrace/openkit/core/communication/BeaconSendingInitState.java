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
    static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = 2 * 1000;
	BeaconSendingInitState() {
		super(false);
	}

	@Override
    void execute(BeaconSendingContext context) {

        long currentTimestamp = context.getCurrentTimestamp();
        context.setLastOpenSessionBeaconSendTime(currentTimestamp);
        context.setLastStatusCheckTime(currentTimestamp);

        StatusResponse statusResponse;
        int retry = 0;
        long sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;
        do {
            retry++;
            statusResponse = context.getHTTPClient().sendStatusRequest();

            // if no (valid) status response was received -> sleep 1s and then retry (max 5 times altogether)
            if (statusResponse == null) {
				try {
					context.sleep(sleepTimeInMillis);
					sleepTimeInMillis *= 2; // double sleep time in each iteration
				} catch (InterruptedException e) {
					// just make a state transition
					statusResponse = null;
					Thread.currentThread().interrupt();
					break;
				}
			}
        } while (!context.isShutdownRequested() && (statusResponse == null) && (retry < MAX_INITIAL_STATUS_REQUEST_RETRIES));

        if (context.isShutdownRequested() || (statusResponse == null)) {
            // initial configuration request was either terminated from outside or the config could not be retrieved
            // TODO stefan.eberl@dynatrace.com - Define some better error handling if init failed!
            context.initCompleted(false);
            context.setCurrentState(new BeaconSendingTerminalState());
        } else {
            // success -> continue with time sync
            context.handleStatusResponse(statusResponse);
            context.setCurrentState(new BeaconSendingTimeSyncState(true));
        }
    }
}
