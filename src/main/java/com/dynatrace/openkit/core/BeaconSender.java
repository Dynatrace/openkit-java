/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.protocol.TimeSyncResponse;
import com.dynatrace.openkit.providers.TimeProvider;

/**
 * The BeaconSender is responsible for asynchronously sending the Beacons to the provided endpoint.
 */
public class BeaconSender implements Runnable {

	private static final int MAX_INITIAL_STATUS_REQUEST_RETRIES = 5;		// execute max 5 status request retries for getting initial settings
	private static final int TIME_SYNC_REQUESTS = 5;						// execute 5 time sync requests for time sync calculation
	private static final int STATUS_CHECK_INTERVAL = 2 * 60 * 60 * 1000;	// wait 2h (in ms) for next status request
	private static final int SHUTDOWN_TIMEOUT = 10 * 1000;					// wait max 10s (in ms) for beacon sender to complete data sending during shutdown

	// data structures for managing open and finished Sessions
	private SynchronizedQueue<SessionImpl> openSessions = new SynchronizedQueue<SessionImpl>();
	private SynchronizedQueue<SessionImpl> finishedSessions = new SynchronizedQueue<SessionImpl>();

	// Configuration reference
	private Configuration configuration;

	// beacon sender thread
	private Thread beaconSenderThread;

	// indicates that shutdown() was called and beacon sender thread should be ended
	private final AtomicBoolean shutdown;

	// timestamps for last beacon send and status check
	private long lastOpenSessionBeaconSendTime;
	private long lastStatusCheckTime;

	// *** constructors ***

	public BeaconSender(Configuration configuration) {
		this.configuration = configuration;
		shutdown = new AtomicBoolean(false);
	}

	// *** Runnable interface methods ***

	@Override
	public void run() {
		// loop until shutdown() is called
		while (!shutdown.get()) {
			// sleep 1s
			sleep();

			StatusResponse statusResponse = null;
			// check capture mode
			if (configuration.isCapture()) {
				statusResponse = null;

				// check if there's finished Sessions to be sent -> immediately send beacon(s) of finished Sessions
				while (!finishedSessions.isEmpty()) {
					SessionImpl session = finishedSessions.get();
					statusResponse = session.sendBeacon();
				}

				// check if send interval spent -> send current beacon(s) of open Sessions
				if (System.currentTimeMillis() > lastOpenSessionBeaconSendTime + configuration.getSendInterval()) {
					for (SessionImpl session : openSessions.toArrayList()) {
						statusResponse = session.sendBeacon();
					}

					// update open session beacon send timestamp in this case
					lastOpenSessionBeaconSendTime = System.currentTimeMillis();
				}

				// if at least one beacon was sent AND a (valid) status response was received -> update settings
				if (statusResponse != null) {
					configuration.updateSettings(statusResponse);
				}
			} else {
				// check if status check interval spent -> send status request & update settings
				if (System.currentTimeMillis() > lastStatusCheckTime + STATUS_CHECK_INTERVAL) {
					statusResponse = configuration.getCurrentHTTPClient().sendStatusRequest();

					// if a (valid) status response was received -> update settings
					if (statusResponse != null) {
						configuration.updateSettings(statusResponse);
					}

					// update status check timestamp in any case
					lastStatusCheckTime = System.currentTimeMillis();
				}
			}
		}

		// during shutdown, end all open Sessions
		while (!openSessions.isEmpty()) {
			Session session = openSessions.get();
			session.end();
		}

		// and finally send all the (now) finished Sessions
		while (!finishedSessions.isEmpty()) {
			SessionImpl session = finishedSessions.get();
			session.sendBeacon();
		}
	}

	// *** public methods ***

	// called indirectly by OpenKit.initialize(); tries to get initial settings and starts beacon sender thread
	public void initialize() {
		lastOpenSessionBeaconSendTime = System.currentTimeMillis();
		lastStatusCheckTime = lastOpenSessionBeaconSendTime;

		// try status check until max retries were executed or shutdown() is called, but at least once!
		StatusResponse statusResponse = null;
    	int retry = 0;
		do {
			retry++;
			statusResponse = configuration.getCurrentHTTPClient().sendStatusRequest();

			// if no (valid) status response was received -> sleep 1s and then retry (max 5 times altogether)
			if (statusResponse == null) {
				sleep();
			}
		} while (!shutdown.get() && (statusResponse == null) && (retry < MAX_INITIAL_STATUS_REQUEST_RETRIES));

		// update settings based on (valid) status response
		// if status response is null, updateSettings() will turn capture off, as there were no initial settings received
		configuration.updateSettings(statusResponse);

		if (configuration.isDynatrace()) {
			// initialize time provider with cluster time offset -> time sync
			TimeProvider.initialize(calculateClusterTimeOffset(), true);
		} else {
			// initialize time provider -> no time sync
			TimeProvider.initialize(0, false);
		}

		// start beacon sender thread
		beaconSenderThread = new Thread(this);
		beaconSenderThread.start();
	}

	// when starting a new Session, put it into open Sessions
	public void startSession(SessionImpl session) {
		openSessions.put(session);
	}

	// when finishing a Session, remove it from open Sessions and put it into finished Sessions
	public void finishSession(SessionImpl session) {
		openSessions.remove(session);
		finishedSessions.put(session);
	}

	// clear all open and finished Sessions
	public void clearSessions() {
		openSessions.clear();
		finishedSessions.clear();
	}

	// shutdown beacon sender thread, with a timeout
	public void shutdown() {
		shutdown.set(true);
		try {
			if (beaconSenderThread != null) {
				beaconSenderThread.interrupt();
				beaconSenderThread.join(SHUTDOWN_TIMEOUT);
			}
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	// *** private methods ***

	// helper method for sleeping 1s
	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// when interrupted (most probably by shutdown()), just wake up and continue execution
		}
	}

	// calculate the cluster time offset by doing time sync with the Dynatrace cluster
	private long calculateClusterTimeOffset() {
		ArrayList<Long> timeSyncOffsets = new ArrayList<Long>(TIME_SYNC_REQUESTS);
		// no check for shutdown here, time sync has to be completed
		while (timeSyncOffsets.size() < TIME_SYNC_REQUESTS) {
			// execute time-sync request and take timestamps
			long requestSendTime = TimeProvider.getTimestamp();
			TimeSyncResponse timeSyncResponse = configuration.getCurrentHTTPClient().sendTimeSyncRequest();
			long responseReceiveTime = TimeProvider.getTimestamp();

			if (timeSyncResponse != null) {
				long requestReceiveTime = timeSyncResponse.getRequestReceiveTime();
				long responseSendTime = timeSyncResponse.getResponseSendTime();

				// check both timestamps for being > 0
				if ((requestReceiveTime > 0) && (responseSendTime > 0)) {
					// if yes -> continue time-sync
					long offset = (long) (((requestReceiveTime - requestSendTime) +
							(responseSendTime - responseReceiveTime)) / 2.0);

					timeSyncOffsets.add(offset);
				} else {
					// if no -> stop time-sync
					break;
				}
			} else {
				sleep();
			}
		}

		// time sync requests were *not* successful -> use 0 as cluster time offset
		if (timeSyncOffsets.size() < TIME_SYNC_REQUESTS) {
			return 0;
		}

		// time sync requests were successful -> calculate cluster time offset
		Collections.sort(timeSyncOffsets);

		// take median value from sorted offset list
		long median = timeSyncOffsets.get(TIME_SYNC_REQUESTS / 2);

		// calculate variance from median
		long medianVariance = 0;
		for (int i = 0; i < TIME_SYNC_REQUESTS; i++) {
			long diff = timeSyncOffsets.get(i) - median;
			medianVariance += diff * diff;
		}
		medianVariance = medianVariance / TIME_SYNC_REQUESTS;

		// calculate cluster time offset as arithmetic mean of all offsets that are in range of 1x standard deviation
		long sum = 0;
		long count = 0;
		for (int i = 0; i < TIME_SYNC_REQUESTS; i++) {
			long diff = timeSyncOffsets.get(i) - median;
			if (diff * diff <= medianVariance) {
				sum += timeSyncOffsets.get(i);
				count++;
			}
		}

		return (long)Math.round(sum / (double) count);		// return cluster time offset
	}

}
