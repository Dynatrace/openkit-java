package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;

class BeaconSendingFlushSessionsState extends AbstractBeaconSendingState {

	BeaconSendingFlushSessionsState() {
		super(false);
	}

	@Override
	void doExecute(BeaconSendingContext context) {

		// end open sessions -> will be finished afterwards
		SessionImpl[] openSessions = context.getAllOpenSessions();
		for (SessionImpl openSession : openSessions) {
			openSession.end();
		}

		// flush already finished (and previously opened) sessions
		SessionImpl finishedSession = context.getNextFinishedSession();
		while (finishedSession != null) {
			finishedSession.sendBeacon(context.getHTTPClientProvider());
			finishedSession = context.getNextFinishedSession();
		}

		// make last state transition to terminal state
		context.setCurrentState(new BeaconSendingTerminalState());
	}

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return new BeaconSendingTerminalState();
    }
}
