package com.dynatrace.openkit.core.communication;

/**
 * Base class for all beacon sending states.
 */
abstract class AbstractBeaconSendingState {

    /**
     * Boolean variable indicating whether this state is a terminal state or not.
     */
    private final boolean isTerminalState;

    AbstractBeaconSendingState(boolean isTerminalState) {
        this.isTerminalState = isTerminalState;
    }

    /**
     * Execute the current state.
     * <p>
     * <p>
     * In case shutdown was requested, a state transition is performed by this method to the {@link AbstractBeaconSendingState}
     * returned by {@link AbstractBeaconSendingState#getShutdownState()}.
     * </p>
     */
    void execute(BeaconSendingContext context) {

        doExecute(context);

        if (context.isShutdownRequested()) {
            context.setCurrentState(getShutdownState());
        }
    }

    /**
     * Real state execution.
     *
     * @param context State's context.
     */
    abstract void doExecute(BeaconSendingContext context);

    /**
     * Get an instance of the {@link AbstractBeaconSendingState} to which a transition is made upon shutdown request.
     */
    abstract AbstractBeaconSendingState getShutdownState();

    /**
     * Get {@code true} if this state is a terminal state, {@code false} otherwise.
     */
    boolean isTerminalState() {
        return isTerminalState;
    }
}
