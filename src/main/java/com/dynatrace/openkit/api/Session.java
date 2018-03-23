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

package com.dynatrace.openkit.api;

import java.io.Closeable;

/**
 * This interface provides functionality to create Actions in a Session.
 */
public interface Session extends Closeable {

    /**
     * Enters an Action with a specified name in this Session.
     *
     * @param actionName name of the Action
     * @return Action instance to work with
     */
    RootAction enterAction(String actionName);

    /**
     * Tags a session with the provided {@code userTag}.
     *
     * @param userTag id of the user
     */
    void identifyUser(String userTag);

    /**
     * Reports a crash with a specified error name, crash reason and a stacktrace.
     *
     * @param errorName  name of the error leading to the crash (e.g. Exception class)
     * @param reason     reason or description of that error
     * @param stacktrace stacktrace leading to that crash
     */
    void reportCrash(String errorName, String reason, String stacktrace);

    /**
     * Ends this Session and marks it as ready for immediate sending.
     */
    void end();
}
