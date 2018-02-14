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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.communication.BeaconSendingContext;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.concurrent.TimeUnit;

/**
 * The BeaconSender is responsible for asynchronously sending the Beacons to the provided endpoint.
 * <p>
 * <p>
 * The {@code BeaconSender} manages the thread running OpenKit communication in the background.
 * </p>
 */
public class BeaconSender {

    private static final String THREAD_NAME = BeaconCacheEvictor.class.getSimpleName();
    static final long SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

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
     * <p>
     * To start the beacon sending the {@link #initialize()} method must be called.
     * </p>
     *
     * @param configuration  OpenKit configuration.
     * @param clientProvider Used for retrieving an {@link com.dynatrace.openkit.protocol.HTTPClient} instance.
     * @param timingProvider Used for some timing related things.
     */
    public BeaconSender(Configuration configuration, HTTPClientProvider clientProvider, TimingProvider timingProvider) {
        context = new BeaconSendingContext(configuration, clientProvider, timingProvider);
    }

    /**
     * Start beacon sender thread.
     * <p>
     * <p>
     * Note: The beacon sender has to perform some initialization code, which is done in the background,
     * before it actually starts sending beacons.
     * If it's a must to have OpenKit fully initialized use the {@link #waitForInit()} method to wait until initialized.
     * </p>
     */
    public synchronized void initialize() {

        // create and start the sending thread
        beaconSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // run the loop as long as OpenKit does not get shutdown or ends itself.
                while (!context.isInTerminalState()) {
                    context.executeCurrentState();
                }
            }
        });
        beaconSenderThread.setName(THREAD_NAME);
        beaconSenderThread.start();
    }

    /**
     * Wait until OpenKit is fully initialized or a shutdown request has been made.
     * <p>
     * <p>
     * This method might hang forever.
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

        context.requestShutdown();

        if (beaconSenderThread != null) {
            beaconSenderThread.interrupt();
            try {
                beaconSenderThread.join(SHUTDOWN_TIMEOUT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            beaconSenderThread = null;
        }
    }

    /**
     * When starting a new Session, put it into open Sessions.
     * <p>
     * <p>
     * A session is only put into the open Sessions if capturing is enabled.
     * In case capturing is disabled, this method has no effect.
     * </p>
     *
     * @param session Session to start.
     */
    public void startSession(SessionImpl session) {
        context.startSession(session);
    }

    /**
     * When finishing a Session, remove it from open Sessions and put it into finished Sessions.
     * <p>
     * <p>
     * As soon as a session get's finished it will transferred to the server.
     * </p>
     *
     * @param session Session to finish.
     */
    public void finishSession(SessionImpl session) {
        context.finishSession(session);
    }
}
