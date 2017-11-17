package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;

/**
 * Un this state open sessions are finished. After that all sessions are sent tot the server.
 *
 * <p>
 *     Transition to:
 *     <ul>
 *         <li>{@link BeaconSendingTerminalState}</li>
 *     </ul>
 * </p>
 */
class BeaconSendingFlushSessionsState extends AbstractBeaconSendingState {

    static final int BEACON_SEND_RETRY_ATTEMPTS = 1;

    BeaconSendingFlushSessionsState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        // end open sessions -> will be flushed afterwards
        SessionImpl[] openSessions = context.getAllOpenSessions();
        for (SessionImpl openSession : openSessions) {
            openSession.end();
        }

        // flush already finished (and previously ended) sessions
        SessionImpl finishedSession = context.getNextFinishedSession();
        while (finishedSession != null) {
            finishedSession.sendBeacon(context.getHTTPClientProvider(), BEACON_SEND_RETRY_ATTEMPTS);
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
