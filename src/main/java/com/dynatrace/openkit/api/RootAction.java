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

package com.dynatrace.openkit.api;

/**
 * This interface provides the same functionality as Action, additionally it allows to create child Actions
 */
public interface RootAction extends Action {

    /**
     * Enters a (child) Action with a specified name on this Action.
     *
     * <p>
     *     If the given {@code actionName} is {@code null} or an empty string,
     *     no reporting will happen on that {@link RootAction}.
     * </p>
     *
     * @param actionName name of the Action
     * @return Action instance to work with
     */
    Action enterAction(String actionName);
}
