package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOffState extends AbstractBeaconSendingState {

	private static final int STATUS_CHECK_INTERVAL = 2 * 60 * 60 * 1000;	// wait 2h (in ms) for next status request

	private StatusResponse statusResponse;

	BeaconSendingStateCaptureOffState() {
		super(false);
	}

	@Override
	void doExecute(BeaconSendingContext context) {

		long currentTime = context.getCurrentTimestamp();

		long delta =  currentTime - (context.getLastStatusCheckTime() + STATUS_CHECK_INTERVAL);
		if (delta > 0 && !context.isShutdownRequested()) {
			// still have some time to sleep
			try {
				context.sleep(delta);
			} catch (InterruptedException e) {
				// sleep was interrupted -> flush session
				context.setCurrentState(new BeaconSendingFlushSessionsState());
				Thread.currentThread().interrupt(); // re-interrupt
				return;
			}
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

		if (statusResponse == null)
			return; // nothing to handle

		context.handleStatusResponse(statusResponse);
		if (context.isCaptureOn()) {
			// capturing is turned on -> make state transition to CaptureOne (via TimeSync)
			context.setCurrentState(new BeaconSendingTimeSyncState(false));
		}
	}
}
