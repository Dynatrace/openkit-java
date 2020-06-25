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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.communication.BeaconSendingContext;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.concurrent.TimeUnit;

/**
 * The BeaconSender is responsible for asynchronously sending the Beacons to the provided endpoint.
 * <p>
 *     The {@code BeaconSender} manages the thread running OpenKit communication in the background.
 * </p>
 */
public class BeaconSender {

    private static final String THREAD_NAME = BeaconSender.class.getSimpleName();
    private static final long SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    private final Logger logger;

    /**
     * Thread used to send the beacons in the background
     */
    private Thread beaconSenderThread;
    /**
     * Context in terms of the State Design Pattern
     */
    private final BeaconSendingContext context;

    /**
     * Create a new BeaconSender.
     * <p>
     *     To start the beacon sending the {@link #initialize()} method must be called.
     * </p>
     *
     * @param logger Logger for logging messages
     * @param httpClientConfiguration  Initial HTTP client configuration.
     * @param clientProvider Used for retrieving an {@link com.dynatrace.openkit.protocol.HTTPClient} instance.
     * @param timingProvider Used for some timing related things.
     */
    public BeaconSender(Logger logger, HTTPClientConfiguration httpClientConfiguration, HTTPClientProvider clientProvider, TimingProvider timingProvider) {
        this.logger = logger;
        this.context = new BeaconSendingContext(logger, httpClientConfiguration, clientProvider, timingProvider);
    }

    /**
     * Start beacon sender thread.
     * <p>
     *     Note: The beacon sender has to perform some initialization code, which is done in the background,
     *     before it actually starts sending beacons.
     *     If it's necessary to have OpenKit fully initialized use the {@link #waitForInit()} method to wait until initialized.
     * </p>
     */
    public synchronized void initialize() {
        final String className = getClass().getSimpleName();
        // create and start the sending thread
        beaconSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // run the loop as long as OpenKit does not get shutdown or ends itself.
                if (logger.isDebugEnabled()) {
                    logger.debug(className + " initialize() - thread started");
                }
                while (!context.isInTerminalState()) {
                    context.executeCurrentState();
                }
            }
        });
        beaconSenderThread.setDaemon(true);
        beaconSenderThread.setName(THREAD_NAME);
        beaconSenderThread.start();
    }

    /**
     * Wait until OpenKit is fully initialized or a shutdown request has been made.
     * <p>
     *     This method might hang forever.
     * </p>
     *
     * @return {@code true} if OpenKit is fully initialized, or {@code false} if shutdown has been requested during init phase.
     */
    public boolean waitForInit() {
        return context.waitForInit();
    }

    /**
     * Wait until OpenKit is fully initialized or a shutdown request has been made or given timeout expired.
     *
     * @param timeoutMillis The maximum number of milliseconds to wait for initialization being completed.
     * @return {@code true} if OpenKit is fully initialized, or {@code false} if shutdown has been requested during init phase.
     */
    public boolean waitForInit(long timeoutMillis) {
        return context.waitForInit(timeoutMillis);
    }

    /**
     * Get a boolean indicating whether OpenKit has been initialized or not.
     *
     * @return {@code true} if OpenKit has been initialized, {@code false} otherwise.
     */
    public boolean isInitialized() {
        return context.isInitialized();
    }

    /**
     * Shutdown the BeaconSender and wait until it's shutdown (at most {@link BeaconSender#SHUTDOWN_TIMEOUT} milliseconds.
     */
    public synchronized void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " shutdown() - thread request shutdown");
        }
        context.requestShutdown();

        if (beaconSenderThread != null) {
            beaconSenderThread.interrupt();
            try {
                beaconSenderThread.join(SHUTDOWN_TIMEOUT);
                if (logger.isDebugEnabled()) {
                    logger.debug(getClass().getSimpleName() + " shutdown() - thread stopped");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (logger.isDebugEnabled()) {
                    logger.debug(getClass().getSimpleName() + " shutdown() - Thread interrupted while waiting for BeaconSender thread to join");
                }
            }
            beaconSenderThread = null;
        }
    }

    /**
     * Returns the last known server configuration.
     */
    public ServerConfiguration getLastServerConfiguration() {
        return context.getLastServerConfiguration();
    }

    /**
     * Returns the current server ID to be used for creating new sessions
     */
    public int getCurrentServerId() {
        return context.getCurrentServerId();
    }

    /**
     * Adds the given session to the known sessions of this {@link BeaconSender}.
     *
     * <p>
     *     This method should be called when creating a new session.
     * </p>
     *
     * @param session the session to start.
     */
    public void addSession(SessionImpl session) {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " addSession()");
        }
        context.addSession(session);
    }
}
