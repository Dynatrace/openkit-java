package com.dynatrace.openkit.core.communication;

class BeaconSendingTerminalState extends AbstractBeaconSendingState {

	BeaconSendingTerminalState() {
		super(true);
	}

	@Override
    void doExecute(BeaconSendingContext context) {

    	// set the shutdown request - just to ensure it's set
        context.requestShutdown();
    }

    @Override
    AbstractBeaconSendingState getShutdownState() {
        return this;
    }
}
