package com.dynatrace.openkit.core.communication;

import java.util.concurrent.TimeUnit;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOnState extends AbstractBeaconSendingState {

    /** re-execute time sync every two hours */
    private static long TIME_SYNC_RE_EXECUTE_DURATION = TimeUnit.HOURS.toMillis(2);

	/** store last received status response */
	private StatusResponse statusResponse = null;

	BeaconSendingStateCaptureOnState() {
		super(false);
	}

	@Override
	void doExecute(BeaconSendingContext context) {

	    // every two hours a time sync shall be performed
        if (isTimeSyncRequired(context)) {
            context.setCurrentState(new BeaconSendingTimeSyncState());
            return;
        }

        try {
            context.sleep();
        } catch (InterruptedException e) {
            // make transition to next state
            context.setCurrentState(new BeaconSendingFlushSessionsState());
            Thread.currentThread().interrupt(); // re-interrupt
            return;
        }

        statusResponse = null;

        // send all finished sessions
		sendFinishedSessions(context);

		// check if we need to send open sessions & do it if necessary
		sendOpenSessions(context);

		// check if send interval spent -> send current beacon(s) of open Sessions
		handleStatusResponse(context);
	}

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingFlushSessionsState();
    }

    private boolean isTimeSyncRequired(BeaconSendingContext context) {

	    long timeStamp = context.getCurrentTimestamp();

	    return ((context.getLastTimeSyncTime() < 0) ||
            ((timeStamp - context.getLastTimeSyncTime()) > TIME_SYNC_RE_EXECUTE_DURATION));
    }

    private void sendFinishedSessions(BeaconSendingContext context) {

		// check if there's finished Sessions to be sent -> immediately send beacon(s) of finished Sessions
		SessionImpl finishedSession = context.getNextFinishedSession();
		while (finishedSession != null) {
			finishedSession.sendBeacon(context.getHTTPClientProvider());
			finishedSession = context.getNextFinishedSession();
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
			context.setCurrentState(new BeaconSendingStateCaptureOffState());
		}
	}
}
