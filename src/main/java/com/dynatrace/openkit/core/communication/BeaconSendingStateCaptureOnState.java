package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.protocol.StatusResponse;

class BeaconSendingStateCaptureOnState extends AbstractBeaconSendingState {

    static final int BEACON_SEND_RETRY_ATTEMPTS = 3;

    /**
     * store last received status response
     */
    private StatusResponse statusResponse = null;

    BeaconSendingStateCaptureOnState() {
        super(false);
    }

    @Override
    void doExecute(BeaconSendingContext context) throws InterruptedException {

        // every two hours a time sync shall be performed
        if (BeaconSendingTimeSyncState.isTimeSyncRequired(context)) {
            context.setCurrentState(new BeaconSendingTimeSyncState());
            return;
        }

        context.sleep();

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

    private void sendFinishedSessions(BeaconSendingContext context) throws InterruptedException {

        // check if there's finished Sessions to be sent -> immediately send beacon(s) of finished Sessions
        SessionImpl finishedSession = context.getNextFinishedSession();
        while (finishedSession != null) {
            finishedSession.sendBeacon(context.getHTTPClientProvider(), BEACON_SEND_RETRY_ATTEMPTS);
            finishedSession = context.getNextFinishedSession();
        }
    }

    private void sendOpenSessions(BeaconSendingContext context) throws InterruptedException {

        long currentTimestamp = context.getCurrentTimestamp();
        if (currentTimestamp <= context.getLastOpenSessionBeaconSendTime() + context.getSendInterval()) {
            return; // still some time to send open sessions
        }

        SessionImpl[] openSessions = context.getAllOpenSessions();
        for (SessionImpl session : openSessions) {
            session.sendBeacon(context.getHTTPClientProvider(), BEACON_SEND_RETRY_ATTEMPTS);
        }
    }

    private void handleStatusResponse(BeaconSendingContext context) {

        if (statusResponse == null) {
            return; // nothing to handle
        }

        context.handleStatusResponse(statusResponse);
        if (!context.isCaptureOn()) {
            // capturing is turned off -> make state transition
            context.setCurrentState(new BeaconSendingStateCaptureOffState());
        }
    }
}
