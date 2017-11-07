package com.dynatrace.openkit.core.communication;

class BeaconSendingTerminalState extends BeaconSendingState {

	BeaconSendingTerminalState() {
		super(true);
	}

	@Override
    void execute(BeaconSendingContext context) {

    	// set the shutdown request - just to ensure it's set
        context.requestShutdown();
    }
}
