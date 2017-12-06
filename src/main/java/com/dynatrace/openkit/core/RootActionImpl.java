package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Actual implementation of the {@link RootAction} interface.
 */
public class RootActionImpl extends ActionImpl implements RootAction {

    // Beacon reference
    private final Beacon beacon;
    // data structures for managing child actions
    private SynchronizedQueue<Action> openChildActions = new SynchronizedQueue<Action>();

    // *** constructors ***

    public RootActionImpl(Beacon beacon, String name, SynchronizedQueue<Action> parentActions) {
        super(beacon, name, parentActions);
        this.beacon = beacon;
    }

    // *** interface methods ***

    @Override
    public Action enterAction(String actionName) {
        return new ActionImpl(beacon, actionName, this, openChildActions);
    }

    // *** protected methods ***

    @Override
    protected Action doLeaveAction() {
        // leave all open Child-Actions
        while (!openChildActions.isEmpty()) {
            Action action = openChildActions.get();
            action.leaveAction();
        }

        // call leaveAction in base class
        return super.doLeaveAction();
    }
}
