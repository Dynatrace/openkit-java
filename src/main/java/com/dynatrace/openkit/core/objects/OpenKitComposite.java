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

package com.dynatrace.openkit.core.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * A composite base class for OpenKit objects.
 *
 * <p>
 *     It features a container to store child objects.
 *     Be careful that the container is not thread safe, which must be guaranteed by the implementing class.
 * </p>
 */
public abstract class OpenKitComposite implements OpenKitObject {

    /** default value of action id */
    private static final int DEFAULT_ACTION_ID = 0;

    /**
     * Container storing the children of this composite.
     */
    private final List<OpenKitObject> children = new LinkedList<OpenKitObject>();

    /**
     * Add a child object to the list of children.
     *
     * @param childObject The child object to add.
     */
    void storeChildInList(OpenKitObject childObject) {
        children.add(childObject);
    }

    /**
     * Remove a child object from the list of children.
     *
     * @param childObject The child object to remove.
     * @return {@code true} if the given {@code childObject} was successfully removed, {@code false} otherwise.
     */
    boolean removeChildFromList(OpenKitObject childObject) {
        return children.remove(childObject);
    }

    /**
     * Get a shallow copy of the {@link OpenKitObject} child objects.
     *
     * @return Shallow copy of child objects
     */
    List<OpenKitObject> getCopyOfChildObjects() {
        return new LinkedList<OpenKitObject>(children);
    }

    /**
     * Abstract method to notify the composite about closing/ending a child object.
     *
     * <p>
     *     The implementing class is fully responsible to handle the implementation.
     *     In most cases removing the child from the container {@see #removeChildFromList} is sufficient.
     * </p>
     */
    abstract void onChildClosed(OpenKitObject childObject);

    /**
     * Get the action id of this composite or {@code 0} if the composite is not an action.
     *
     * <p>
     *     The default return value of {@code 0} is implemented here.
     *     Action related composites need to override this method and return the appropriate value.
     * </p>
     *
     * @return The action id of this composite.
     */
    public int getActionID() {
        return DEFAULT_ACTION_ID;
    }
}
