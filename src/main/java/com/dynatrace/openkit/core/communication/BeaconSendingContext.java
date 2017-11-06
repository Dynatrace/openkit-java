package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * State context for beacon sending states.
 */
public class BeaconSendingContext {

    static final long DEFAULT_SLEEP_TIME_MILLISECONDS = 1000;

    private final AbstractConfiguration configuration;
    private final HTTPClientProvider httpClientProvider;
    private final TimingProvider timingProvider;

    /** container storing all open sessions */
    private final LinkedBlockingQueue<SessionImpl> openSessions = new LinkedBlockingQueue<SessionImpl>();

    /** container storing all finished sessions */
    private final LinkedBlockingQueue<SessionImpl> finishedSessions = new LinkedBlockingQueue<SessionImpl>();

    /** current state of beacon sender */
    private BeaconSendingState currentState;

    /** boolean indicating whether shutdown was requested or not */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /** timestamp when open sessions were last sent */
    private long lastOpenSessionBeaconSendTime;
    /** timestamp when last status check was done */
    private long lastStatusCheckTime;

    /** countdown latch updated when init was done - which can either be success or failure */
    private final CountDownLatch initCountDownLatch = new CountDownLatch(1);
    /** boolean indicating whether init was successful or not */
    private boolean initSucceeded = false;

    public BeaconSendingContext(AbstractConfiguration configuration,
                                HTTPClientProvider httpClientProvider,
                                TimingProvider timingProvider) {

        this.configuration = configuration;
        this.httpClientProvider = httpClientProvider;
        this.timingProvider = timingProvider;

        currentState = new BeaconSendingInitState();
    }

    public void executeCurrentState() {
        currentState.execute(this);
    }

    public void requestShutdown() {
        shutdown.set(true);
    }

    public boolean isShutdownRequested() {
        return shutdown.get();
    }

    public boolean waitForInit() {
        try {
            initCountDownLatch.await();
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }
        return initSucceeded;
    }

    void setCurrentState(BeaconSendingState newState) {
        currentState = newState;
    }

    BeaconSendingState getCurrentState() {
        return currentState;
    }

    void initCompleted(boolean success) {
        initSucceeded = success;
        initCountDownLatch.countDown();
    }

	public HTTPClientProvider getHTTPClientProvider() {
		return httpClientProvider;
	}

    HTTPClient getHTTPClient() {
        return httpClientProvider.createClient(configuration.getHttpClientConfig());
    }

    long getCurrentTimestamp() {
        return timingProvider.provideTimestampInMilliseconds();
    }

    void sleep() {
        sleep(DEFAULT_SLEEP_TIME_MILLISECONDS);
    }

    void sleep(long millis) {
        timingProvider.sleep(millis);
    }

    void setLastOpenSessionBeaconSendTime(long timestamp) {
        lastOpenSessionBeaconSendTime = timestamp;
    }

    long getLastOpenSessionBeaconSendTime() {
        return lastOpenSessionBeaconSendTime;
    }

    void setLastStatusCheckTime(long timestamp) {
        lastStatusCheckTime = timestamp;
    }

    long getLastStatusCheckTime() {
        return lastStatusCheckTime;
    }

    int getSendInterval() {
    	return configuration.getSendInterval();
	}

    void handleStatusResponse(StatusResponse statusResponse) {

        configuration.updateSettings(statusResponse);

        if (!configuration.isCapture()) {
            // capturing was turned off
            clearAllSessions();
        }
    }

    private void clearAllSessions() {

        openSessions.clear();
        finishedSessions.clear();
    }

    boolean isCaptureOn() {
    	return configuration.isCapture();
	}

	public SessionImpl getNextFinishedSession() {
		return finishedSessions.poll();
	}

	public SessionImpl[] getAllOpenSessions() {
    	return openSessions.toArray(new SessionImpl[0]);
	}
}
