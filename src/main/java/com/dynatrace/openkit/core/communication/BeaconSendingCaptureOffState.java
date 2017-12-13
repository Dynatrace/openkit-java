package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.StatusResponse;

import java.util.concurrent.TimeUnit;

/**
 * State where no data is captured. Periodically issues a status request to check if capturing shall be re-enabled.
 * The check interval is defined in {@link BeaconSendingCaptureOffState#INITIAL_RETRY_SLEEP_TIME_MILLISECONDS}.
 *
 * <p>
 *     Transition to:
 *     <ul>
 *         <li>{@link BeaconSendingCaptureOnState} if capturing is re-enabled</li>
 *         <li>{@link BeaconSendingFlushSessionsState} on shutdown</li>
 *         <li>{@link BeaconSendingTimeSyncState} if initial time sync failed</li>
 *     </ul>
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

    BeaconSendingCaptureOffState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        // disable capturing - avoid collecting further data
        context.disableCapture();

        long currentTime = context.getCurrentTimestamp();

        long delta = STATUS_CHECK_INTERVAL - (currentTime - context.getLastStatusCheckTime());
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
            context.handleStatusResponse(statusResponse);
        }
        // if initial time sync failed before
        if (context.isTimeSyncSupported() && !context.isTimeSynced()) {
            // then retry initial time sync
            context.setNextState(new BeaconSendingTimeSyncState(true));
        } else if (statusResponse != null && context.isCaptureOn()) {
            // capturing is re-enabled again, but only if we received a response from the server
            context.setNextState(new BeaconSendingCaptureOnState());
        }
    }
}

