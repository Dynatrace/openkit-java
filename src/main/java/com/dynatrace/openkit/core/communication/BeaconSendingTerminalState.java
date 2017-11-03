package com.dynatrace.openkit.core.communication;

class BeaconSendingTerminalState extends BeaconSendingState {

    @Override
    void execute(BeaconSendingContext context) {
        context.requestShutdown();
    }
}
