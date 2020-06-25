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

package com.dynatrace.openkit.api;

import java.io.Closeable;

/**
 * This interface provides basic OpenKit functionality, like creating a Session and shutting down OpenKit.
 */
public interface OpenKit extends Closeable {

    /**
     * Waits until OpenKit is fully initialized.
     *
     * <p>
     * The calling thread is blocked until OpenKit is fully initialized or until OpenKit is shut down using the
     * {@link #shutdown()} method.
     *
     * Be aware, if {@link com.dynatrace.openkit.AbstractOpenKitBuilder} is wrongly configured, for example when creating an
     * instance with an incorrect endpoint URL, then this method might hang indefinitely, unless {@link #shutdown()} is called.
     * </p>
     *
     * @return {@code true} when OpenKit is fully initialized, {@code false} when a shutdown request was made.
     */
    boolean waitForInitCompletion();

    /**
     * Waits until OpenKit is fully initialized or the given timeout expired.
     * <p>
     * <p>
     * The calling thread is blocked until OpenKit is fully initialized or until OpenKit is shut down using the
     * {@link #shutdown()} method or the timeout expired..
     * <p>
     * Be aware, if {@link com.dynatrace.openkit.AbstractOpenKitBuilder} is wrongly configured, for example when creating an
     * instance with an incorrect endpoint URL, then this method might hang indefinitely, unless {@link #shutdown()} is called or timeout expires.
     * </p>
     *
     * @param timeoutMillis The maximum number of milliseconds to wait for initialization being completed.
     * @return {@code true} when OpenKit is fully initialized, {@code false} when a shutdown request was made or {@code timeoutMillis} expired.
     */
    boolean waitForInitCompletion(long timeoutMillis);

    /**
     * Returns whether OpenKit is initialized or not.
     *
     * @return {@code true} if OpenKit is fully initialized, {@code false} if OpenKit still performs initialization.
     */
    boolean isInitialized();

    /**
     * Creates a Session instance which can then be used to create Actions.
     *
     * @param clientIPAddress client IP address where this Session is coming from
     * @return Session instance to work with
     */
    Session createSession(String clientIPAddress);

    /**
     * Creates a Session instance which can then be used to create Actions.
     *
     * <p>
     *     This is similar to the method {@link #createSession(String)}, except that
     *     the client's IP address is determined on the server side.
     * </p>
     *
     * @return Session instance to work with
     */
    Session createSession();

    /**
     * Shuts down OpenKit, ending all open Sessions and waiting for them to be sent.
     */
    void shutdown();

}
