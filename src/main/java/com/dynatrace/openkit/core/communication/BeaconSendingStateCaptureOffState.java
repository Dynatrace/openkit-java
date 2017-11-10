package com.dynatrace.openkit.core.communication;

import java.util.concurrent.TimeUnit;

import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOffState extends AbstractBeaconSendingState {

    /**
     * number of retries for the status request
     */
    static final int STATUS_REQUEST_RETRIES = 5;
    static final long INITIAL_RETRY_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);
    /**
     * maximum time to wait till next status check
     */
    private static final long STATUS_CHECK_INTERVAL = TimeUnit.HOURS.toMillis(2);

    private StatusResponse statusResponse;

    BeaconSendingStateCaptureOffState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        long currentTime = context.getCurrentTimestamp();

        long delta = STATUS_CHECK_INTERVAL - (currentTime - context.getLastStatusCheckTime());
        if (delta > 0) {
            context.sleep(delta);
        }

        statusResponse = null;

        sendStatusRequests(context);
        handleStatusResponse(context);

        // update the last status check time in any case
        context.setLastStatusCheckTime(currentTime);
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingFlushSessionsState();
    }

    private void sendStatusRequests(BeaconSendingContext context) throws InterruptedException {

        int retry = 0;
        long sleepTimeInMillis = INITIAL_RETRY_SLEEP_TIME_MILLISECONDS;
        while (retry++ < STATUS_REQUEST_RETRIES && !context.isShutdownRequested()) {

            statusResponse = context.getHTTPClient().sendStatusRequest();
            if (statusResponse != null) {
                break; // got a response
            }

            if (retry < STATUS_REQUEST_RETRIES) {
                context.sleep(sleepTimeInMillis);
                sleepTimeInMillis *= 2;
            }
        }
    }

    private void handleStatusResponse(BeaconSendingContext context) {

        if (statusResponse == null) {
            return; // nothing to handle
        }

        context.handleStatusResponse(statusResponse);
        if (context.isCaptureOn()) {
            // capturing is turned on -> make state transition to CaptureOne (via TimeSync)
            AbstractBeaconSendingState nextState = BeaconSendingTimeSyncState.isTimeSyncRequired(context)
                ? new BeaconSendingTimeSyncState(false)
                : new BeaconSendingStateCaptureOnState();
            context.setCurrentState(nextState);
        }
    }
}
