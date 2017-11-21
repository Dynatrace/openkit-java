/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

/**
 * This interface provides basic OpenKit functionality, like creating a Session and shutting down the OpenKit.
 */
public interface OpenKit {

    /**
     * Name of Dynatrace HTTP header which is used for tagging web requests.
     */
    String WEBREQUEST_TAG_HEADER = "X-dynaTrace";

    /**
     * Initializes the OpenKit.
     * <p>
     *     The initial settings are received in a background thread, allowing the OpenKit user to continue
     *     without waiting for OpenKit to be initialized.
     *
     *     After calling this method {@link #waitForInitCompletion()} can be used to wait until OpenKit is
     *     fully initialized.
     * </p>
     *
     * Must be done before any other calls to the OpenKit, otherwise those calls to the OpenKit will do nothing.
     */
    void initialize();

    /**
     * Waits until OpenKit is fully initialized.
     *
     * <p>
     *     The calling thread is blocked until OpenKit is fully initialized or until OpenKit is shut down using the
     *     {@link #shutdown()} method.
     *
     *     Due to misconfiguration in the {@link com.dynatrace.openkit.OpenKitFactory} when creating the
     *     instance (e.g. wrong endpoint URL) this method might hang indefinitely, unless {@link #shutdown()} is called.
     * </p>
     *
     * @return {@code true} when OpenKit is fully initialized, {@code false} when a shutdown request was made.
     */
    boolean waitForInitCompletion();

    /**
     * Waits until OpenKit is fully initialized or the given timeout expired.
     *
     * <p>
     *     The calling thread is blocked until OpenKit is fully initialized or until OpenKit is shut down using the
     *     {@link #shutdown()} method or the timeout expired..
     *
     *     Due to misconfiguration in the {@link com.dynatrace.openkit.OpenKitFactory} when creating the
     *     instance (e.g. wrong endpoint URL) this method might hang indefinitely, unless {@link #shutdown()} is called.
     * </p>
     *
     * @param timeoutMillis The maximum number of milliseconds to wait for initialization being completed.
     *
     * @return {@code true} when OpenKit is fully initialized, {@code false} when a shutdown request was made or {@code timeoutMillis} expired.
     */
    boolean waitForInitCompletion(long timeoutMillis);

    /**
     * Gets a boolean flag indicating whether OpenKit is initialized or not.
     *
     * @return {@code true} if OpenKit is fully initialized, {@code false} if OpenKit still performs initialization.
     */
    boolean isInitialized();

    /**
     * Defines the version of the application.
     *
     * @param applicationVersion application version
     */
    void setApplicationVersion(String applicationVersion);

    /**
     * Returns the Device used by this OpenKit instance. This can be used to provide basic information, like operating system,
     * manufacturer and model information.
     *
     * @return Device used by this OpenKit instance
     */
    Device getDevice();

    /**
     * Creates a Session instance which can then be used to create Actions.
     *
     * @param clientIPAddress client IP address where this Session is coming from
     *
     * @return Session instance to work with
     */
    Session createSession(String clientIPAddress);

    /**
     * Shuts down the OpenKit, ending all open Sessions and waiting for them to be sent.
     */
    void shutdown();

}
