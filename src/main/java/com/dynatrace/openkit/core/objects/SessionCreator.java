/**
 *   Copyright 2018-2021 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.core.objects;

/**
 * Defines a creator for new sessions.
 */
public interface SessionCreator {

    /**
     * Returns a newly created {@link SessionImpl}.
     *
     * @param parent the parent composite of the session to create.
     */
    SessionImpl createSession(OpenKitComposite parent);

    /**
     * Resets the internal state of this session creator. A reset includes the following:
     * <ul>
     *     <li>resetting the consecutive sequence session number which is increased every time a session is created.</li>
     *     <li>use a new session ID (which will stay the same for all newly created sessions)</li>
     *     <li>use a new randomized number (which will stay the same for all newly created sessions)</li>
     * </ul>
     */
    void reset();
}
