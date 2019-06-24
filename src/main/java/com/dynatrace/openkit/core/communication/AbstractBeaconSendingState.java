/**
 * Copyright 2018-2019 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.communication;

/**
 * Base class for all beacon sending states.
 *
 * <p>
 * Transition to:
 * <ul>
 * <li>{@link AbstractBeaconSendingState#getShutdownState()}</li>
 * </ul>
 * </p>
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
     *
     * <p>
     * In case shutdown was requested, a state transition is performed by this method to the {@link AbstractBeaconSendingState}
     * returned by {@link AbstractBeaconSendingState#getShutdownState()}.
     * </p>
     */
    void execute(BeaconSendingContext context) {

        try {
            doExecute(context);
        } catch (InterruptedException e) {
            onInterrupted(context);
            context.requestShutdown();
            Thread.currentThread().interrupt();
        }

        if (context.isShutdownRequested()) {
            context.setNextState(getShutdownState());
        }
    }

    /**
     * Perform cleanup on interrupt.
     *
     * @param context State's context.
     */
    void onInterrupted(BeaconSendingContext context) {
        // default -> do nothing
    }

    /**
     * Real state execution.
     *
     * @param context State's context.
     */
    abstract void doExecute(BeaconSendingContext context) throws InterruptedException;

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
