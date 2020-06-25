/**
 * Copyright 2018-2020 Dynatrace LLC
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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Actual implementation of the {@link RootAction} interface.
 */
public class RootActionImpl extends BaseActionImpl implements RootAction {

    /**
     * Constructor for constructing the root action class.
     *
     * @param logger The logger used to log information
     * @param parentSession The session, to which this root action belongs to
     * @param name The action's name
     * @param beacon The beacon for retrieving certain data and sending data
     */
    RootActionImpl(Logger logger, SessionImpl parentSession, String name, Beacon beacon) {
        super(logger, parentSession, name, beacon);
    }

    @Override
    public Action enterAction(String actionName) {
        if (actionName == null || actionName.isEmpty()) {
            logger.warning(this + "enterAction: actionName must not be null or empty");
            return new NullAction(this);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "enterAction(" + actionName + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                LeafActionImpl childAction = new LeafActionImpl(logger, this, actionName, beacon);
                storeChildInList(childAction);
                return childAction;
            }
        }

        return new NullAction(this);
    }

    @Override
    protected Action getParentAction() {
        // NOTE: root actions do not have a parent action
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + ", id=" + id + ", name=" + name + "] ";
    }
}
