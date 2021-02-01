/**
 * Copyright 2018-2021 Dynatrace LLC
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
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Implementation of a leaf action.
 *
 * <p>
 *     A leaf action is an {@link Action} which cannot have further
 *     sub actions.
 *     Further sub objects may still be attached to this {@link Action}.
 * </p>
 */
public class LeafActionImpl extends BaseActionImpl {

    /** The parent action */
    private final Action parentAction;

    /**
     * Constructor for constructing the leaf action class.
     *
     * @param logger The logger used to log information
     * @param parentAction The root action, to which this leaf action belongs to
     * @param name The action's name
     * @param beacon The beacon for retrieving certain data and sending data
     */
    LeafActionImpl(Logger logger, RootActionImpl parentAction, String name, Beacon beacon) {
        super(logger, parentAction, name, beacon);
        this.parentAction = parentAction;
    }

    @Override
    protected Action getParentAction() {
        return parentAction;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + ", id=" + id + ", name=" + name
            + ", pa=" + parentActionID + "] ";
    }
}
