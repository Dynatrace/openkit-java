package com.dynatrace.openkit.core.communication;

/**
 * Base class for all beacon sending states.
 */
abstract class BeaconSendingState {

	/** Boolean variable indicating whether this state is a terminal state or not. */
	private final boolean isTerminalState;

	BeaconSendingState(boolean isTerminalState) {
		this.isTerminalState = isTerminalState;
	}

    /**
     * Execute current state.
     *
     * @param context State's context.
     */
    abstract void execute(BeaconSendingContext context);

	/**
	 * Get {@code true} if this state is a terminal state, {@code false} otherwise.
	 */
	boolean isTerminalState() {
		return isTerminalState;
	}
}
