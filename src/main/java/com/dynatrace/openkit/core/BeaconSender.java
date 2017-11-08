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

	// *** constructors ***
	public BeaconSender(AbstractConfiguration configuration, HTTPClientProvider clientProvider, TimingProvider timingProvider) {

	    context = new BeaconSendingContext(configuration, clientProvider, timingProvider);
	}

	// *** public methods ***

	// called indirectly by OpenKit.initialize(); tries to get initial settings and starts beacon sender thread
	public boolean initialize() {

	    beaconSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // run the loop as long as
                while (!context.isInTerminalState()) {
                    context.executeCurrentState();
                }
            }
        });
	    beaconSenderThread.start();

	    boolean success = context.waitForInit();
	    if (!success) {
            try {
                beaconSenderThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return success;
	}

    // shutdown beacon sender thread, with a timeout
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

	// when starting a new Session, put it into open Sessions
	public void startSession(SessionImpl session) {
		if (context.isCaptureOn()) {
			context.startSession(session);
		}
	}

	// when finishing a Session, remove it from open Sessions and put it into finished Sessions
	public void finishSession(SessionImpl session) {

	    context.finishSession(session);
	}



}
