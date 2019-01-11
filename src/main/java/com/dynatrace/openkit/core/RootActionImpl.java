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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Actual implementation of the {@link RootAction} interface.
 */
public class RootActionImpl extends ActionImpl implements RootAction {

    // Beacon reference
    private final Beacon beacon;
    // data structures for managing child actions
    private final SynchronizedQueue<Action> openChildActions = new SynchronizedQueue<Action>();

    // *** constructors ***

    RootActionImpl(Logger logger, Beacon beacon, String name, SynchronizedQueue<Action> parentActions) {
        super(logger, beacon, name, parentActions);
        this.beacon = beacon;
    }

    // *** interface methods ***

    @Override
    public Action enterAction(String actionName) {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "enterAction(" + actionName + ")");
        }
        if (actionName == null || actionName.isEmpty()) {
            logger.warning(this + "enterAction: actionName must not be null or empty");
            return new NullAction(this);
        }
        if (!isActionLeft()) {
            return new ActionImpl(logger, beacon, actionName, this, openChildActions);
        }

        return new NullAction(this);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + ", id=" + getID() + ", name=" + getName() + "] ";
    }
}
