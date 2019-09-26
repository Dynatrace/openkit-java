/**
 *   Copyright 2018-2019 Dynatrace LLC
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

import com.dynatrace.openkit.api.Session;

/**
 * Holds information about the current state of a session
 */
public interface SessionState {

    /**
     * Indicates whether the {@link com.dynatrace.openkit.api.Session} is still new.
     *
     * <p>
     *     A {@link com.dynatrace.openkit.api.Session} is considered as new if it has not yet received any configuration
     *     updates from the server and it if it also is not finished.
     * </p>
     */
    boolean isNew();

    /**
     * Indicates whether the {@link com.dynatrace.openkit.api.Session} is configured or not.
     *
     * <p>
     *     A {@link com.dynatrace.openkit.api.Session} is considered as configured if it received configuration updates
     *     from the server.
     * </p>
     */
    boolean isConfigured();

    /**
     * Indicates if the {@link com.dynatrace.openkit.api.Session} is finished and was configured.
     */
    boolean isConfiguredAndFinished();

    /**
     * Indicates if the {@link com.dynatrace.openkit.api.Session} is configured and not yet finished.
     */
    boolean isConfiguredAndOpen();

    /**
     * Indicates if the {@link com.dynatrace.openkit.api.Session} is finished.
     *
     * <p>
     *     A session is considered as finished, after the {@link Session#end()} method was called.
     * </p>
     */
    boolean isFinished();
}
