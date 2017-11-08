package com.dynatrace.openkit.core.communication;

import java.util.concurrent.TimeUnit;

import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOffState extends AbstractBeaconSendingState {

    /** maximum time to wait till next status check */
    private static final long STATUS_CHECK_INTERVAL = TimeUnit.HOURS.toMillis(2);

    private StatusResponse statusResponse;

    BeaconSendingStateCaptureOffState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        long currentTime = System.currentTimeMillis(); // TODO stefan.eberl

        long delta = STATUS_CHECK_INTERVAL - (currentTime - context.getLastStatusCheckTime());
        if (delta > 0) {
            context.sleep(delta);
        }

        // send the status request
        statusResponse = context.getHTTPClient().sendStatusRequest();

        handleStatusResponse(context);

        // update the last status check time in any case
        context.setLastStatusCheckTime(currentTime);
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingFlushSessionsState();
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
