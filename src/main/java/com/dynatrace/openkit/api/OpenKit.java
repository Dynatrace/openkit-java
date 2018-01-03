/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

/**
 * This interface provides basic OpenKit functionality, like creating a Session and shutting down OpenKit.
 */
public interface OpenKit {

    /**
     * Name of Dynatrace HTTP header which is used for tracing web requests.
     */
    String WEBREQUEST_TAG_HEADER = "X-dynaTrace";

    /**
     * Waits until OpenKit is fully initialized.
     * <p>
     * <p>
     * The calling thread is blocked until OpenKit is fully initialized or until OpenKit is shut down using the
     * {@link #shutdown()} method.
     * <p>
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
     * Shuts down OpenKit, ending all open Sessions and waiting for them to be sent.
     */
    void shutdown();

}
