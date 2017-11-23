package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.protocol.Beacon;

public class RootActionImpl extends ActionImpl implements RootAction {

    // Beacon reference
    private final Beacon beacon;
    // data structures for managing Action hierarchies
    private SynchronizedQueue<Action> openChildActions = new SynchronizedQueue<Action>();

    RootActionImpl(Beacon beacon, String name, SynchronizedQueue<Action> parentActions) {
        super(beacon, name, parentActions);
        this.beacon = beacon;
    }
    @Override
    public Action enterAction(String actionName) {
        return new ActionImpl(beacon, actionName, this, openChildActions);
    }

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
