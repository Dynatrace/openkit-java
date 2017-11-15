package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.StatusResponse;

import java.util.concurrent.TimeUnit;

/**
 * Initial state for beacon sending.
 *
 * <p>
 *     The initial state is used to retrieve the configuration from the server and update the configuration.
 * </p>
 *
 * <p>
 *     Transition to:
 *     <ul>
 *         <li>{@link BeaconSendingTerminalState} if initial status request failed or on shutdown</li>
 *         <li>{@link BeaconSendingTimeSyncState} if initial status request succeeded</li>
 *     </ul>
 * </p>
 */
class BeaconSendingInitState extends AbstractBeaconSendingState {

    static final int MAX_INITIAL_STATUS_REQUEST_RETRIES = 5;
    static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    BeaconSendingInitState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        long currentTimestamp = context.getCurrentTimestamp();
        context.setLastOpenSessionBeaconSendTime(currentTimestamp);
        context.setLastStatusCheckTime(currentTimestamp);

        StatusResponse statusResponse;
        int retry = 0;
        long sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;

        while (true)
        {
            statusResponse = context.getHTTPClient().sendStatusRequest();
            if (!retryStatusRequest(context, statusResponse, retry))
            {
                break;
            }

            // if no (valid) status response was received -> sleep 1s [2s, 4s, 8s, 16s] and then retry (max 5 times altogether)
            context.sleep(sleepTimeInMillis);
            sleepTimeInMillis *= 2;
            retry++;
        }

        if (context.isShutdownRequested() || (statusResponse == null)) {
            // initial configuration request was either terminated from outside or the config could not be retrieved
            context.initCompleted(false);
            context.setCurrentState(new BeaconSendingTerminalState());
        } else {
            // success -> continue with time sync
            context.handleStatusResponse(statusResponse);
            context.setCurrentState(new BeaconSendingTimeSyncState(true));
        }
    }

    private boolean retryStatusRequest(BeaconSendingContext context, StatusResponse statusResponse, int retry) {

        return !context.isShutdownRequested()
            && (statusResponse == null)
            && (retry < MAX_INITIAL_STATUS_REQUEST_RETRIES);
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingTerminalState();
    }

    @Override
    void onInterrupted(BeaconSendingContext context) {

        context.initCompleted(false);
    }
}
