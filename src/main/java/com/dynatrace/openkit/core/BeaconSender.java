/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import java.util.concurrent.TimeUnit;

import com.dynatrace.openkit.core.communication.BeaconSendingContext;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;

/**
 * The BeaconSender is responsible for asynchronously sending the Beacons to the provided endpoint.
 */
public class BeaconSender {

    static final long  SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    /** Thread used to send the beacons in the background */
    private Thread beaconSenderThread;
    /** Context in terms of the State Design Pattern */
    private final BeaconSendingContext context;

    /**
     * Create a new BeaconSender.
     *
     * <p>
     *     To start the beacon sending the {@link #initialize()} method must be called.
     * </p>
     *
     * @param configuration OpenKit configuration.
     * @param clientProvider Used for retrieving an {@link com.dynatrace.openkit.protocol.HTTPClient} instance.
     * @param timingProvider Used for some timing related things.
     */
	public BeaconSender(AbstractConfiguration configuration, HTTPClientProvider clientProvider, TimingProvider timingProvider) {

	    context = new BeaconSendingContext(configuration, clientProvider, timingProvider);
	}

    /**
     * Start and initialize the beacon sending.
     *
     * @return {@code true} if BeaconSender was successfully initialized, {@code false} otherwise.
     */
	public boolean initialize() {

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
	    beaconSenderThread.start();

	    // wait until the "init stage" completed
	    boolean success = context.waitForInit();

	    if (!success) {
	        // current behaviour: if init was not successful, the BeaconSender thread terminates (wait for it)
            // NOTE: This will be changed in the future, as there will be several startup strategies
            try {
                beaconSenderThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return success;
	}

    /**
     * Shutdown the BeaconSender and wait until it's shutdown (at most {@link BeaconSender#SHUTDOWN_TIMEOUT} milliseconds.
     */
    public void shutdown() {

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
     *
     * <p>
     *     A session is only put into the open Sessions if capturing is enabled.
     *     In case capturing is disabled, this method has no effect.
     * </p>
     *
     * @param session Session to start.
     */
	public void startSession(SessionImpl session) {
		if (context.isCaptureOn()) {
			context.startSession(session);
		}
	}

    /**
     * When finishing a Session, remove it from open Sessions and put it into finished Sessions.
     *
     * <p>
     *     As soon as a session get's finished it will transferred to the server.
     * </p>
     *
     * @param session Session to finish.
     */
	public void finishSession(SessionImpl session) {

	    context.finishSession(session);
	}
}
