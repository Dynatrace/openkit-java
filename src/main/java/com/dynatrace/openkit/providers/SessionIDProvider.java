/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.providers;

/**
 * Interface providing consecutive numbers starting at a random offset
 */
public interface SessionIDProvider {

    /**
     * Provide the next sessionID
     * All positive integers greater than 0 can be used as sessionID
     *
     * @return the id that will be used for the next session
     */
    int getNextSessionID();

}
