package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOff extends BeaconSendingState {

	private static final int STATUS_CHECK_INTERVAL = 2 * 60 * 60 * 1000;	// wait 2h (in ms) for next status request

	private StatusResponse statusResponse;

	@Override
	void execute(BeaconSendingContext context) {

		long currentTime = context.getCurrentTimestamp();

		long delta =  currentTime - (context.getLastStatusCheckTime() + STATUS_CHECK_INTERVAL);
		if (delta > 0 && !context.isShutdownRequested()) {
			// still have some time to sleep
			context.sleep(delta);
		}

		// send the status request
		statusResponse = context.getHTTPClient().sendStatusRequest();

		handleStatusResponse(context);

		// update the last status check time in any case
		context.setLastStatusCheckTime(currentTime);
	}

	private void handleStatusResponse(BeaconSendingContext context) {

		if (statusResponse == null)
			return; // nothing to handle

		context.handleStatusResponse(statusResponse);
		if (context.isCaptureOn()) {
			// capturing is turned on -> make state transition
			context.setCurrentState(new BeaconSendingStateCaptureOn());
		}
	}
}
