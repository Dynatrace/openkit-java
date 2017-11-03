package com.dynatrace.openkit.core.communication;

/**
 * Base class for all beacon sending states.
 */
abstract class BeaconSendingState {

    /**
     * Execute current state.
     *
     * @param context State's context.
     */
    abstract void execute(BeaconSendingContext context);
}
