package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOn extends BeaconSendingState {

	/** store last received status response */
	private StatusResponse statusResponse = null;

	@Override
	void execute(BeaconSendingContext context) {

		// send all finished sessions
		sendFinishedSessions(context);

		// check if we need to send open sessions & do it if necessary
		sendOpenSessions(context);

		// check if send interval spent -> send current beacon(s) of open Sessions
		handleStatusResponse(context);
	}

	private void sendFinishedSessions(BeaconSendingContext context) {

		// check if there's finished Sessions to be sent -> immediately send beacon(s) of finished Sessions
		SessionImpl finishedSession = context.getNextFinishedSession();
		while (finishedSession != null) {
			finishedSession.sendBeacon(context.getHTTPClientProvider());
		}
	}

	private void sendOpenSessions(BeaconSendingContext context) {

		long currentTimestamp = context.getCurrentTimestamp();
		if (currentTimestamp <= context.getLastOpenSessionBeaconSendTime() + context.getSendInterval())
			return; // still some time to send open sessions

		SessionImpl[] openSessions = context.getAllOpenSessions();
		for (SessionImpl session : openSessions) {
			session.sendBeacon(context.getHTTPClientProvider());
		}
	}

	private void handleStatusResponse(BeaconSendingContext context) {

		if (statusResponse == null)
			return; // nothing to handle

		context.handleStatusResponse(statusResponse);
		if (!context.isCaptureOn()) {
			// capturing is turned off -> make state transition
			context.setCurrentState(new BeaconSendingStateCaptureOff());
		}
	}
}
