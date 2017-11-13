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

    /**
     * container storing all open sessions
     */
    private final LinkedBlockingQueue<SessionImpl> openSessions = new LinkedBlockingQueue<SessionImpl>();

    /**
     * container storing all finished sessions
     */
    private final LinkedBlockingQueue<SessionImpl> finishedSessions = new LinkedBlockingQueue<SessionImpl>();
    /**
     * boolean indicating whether shutdown was requested or not
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    /**
     * countdown latch updated when init was done - which can either be success or failure
     */
    private final CountDownLatch initCountDownLatch = new CountDownLatch(1);
    /**
     * current state of beacon sender
     */
    private AbstractBeaconSendingState currentState;
    /**
     * timestamp when open sessions were last sent
     */
    private long lastOpenSessionBeaconSendTime;
    /**
     * timestamp when last status check was done
     */
    private long lastStatusCheckTime;
    /**
     * timestamp when last time sync was done
     */
    private long lastTimeSyncTime = -1;
    /**
     * boolean indicating whether init was successful or not
     */
    private boolean initSucceeded = false;
    /**
     * boolean indicating whether the server supports a time sync (true) or not (false).
     */
    private boolean timeSyncSupported = true;

    /**
     * Constructor.
     * <p>
     * <p>
     * The state is initialized to {@link BeaconSendingInitState},
     * </p>
     */
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

    public boolean isInTerminalState() {
        return currentState.isTerminalState();
    }

    public boolean isCaptureOn() {
        return configuration.isCapture();
    }

    public boolean isTimeSyncSupported() {
        return timeSyncSupported;
    }

    public void disableTimeSyncSupport() {
        timeSyncSupported = false;
    }

    AbstractBeaconSendingState getCurrentState() {
        return currentState;
    }

    void setCurrentState(AbstractBeaconSendingState newState) {
        currentState = newState;
    }

    void initCompleted(boolean success) {
        initSucceeded = success;
        initCountDownLatch.countDown();
    }

    HTTPClientProvider getHTTPClientProvider() {
        return httpClientProvider;
    }

    HTTPClient getHTTPClient() {
        return httpClientProvider.createClient(configuration.getHttpClientConfig());
    }

    long getCurrentTimestamp() {
        return timingProvider.provideTimestampInMilliseconds();
    }

    void sleep() throws InterruptedException {
        sleep(DEFAULT_SLEEP_TIME_MILLISECONDS);
    }

    void sleep(long millis) throws InterruptedException {
        timingProvider.sleep(millis);
    }

    long getLastOpenSessionBeaconSendTime() {
        return lastOpenSessionBeaconSendTime;
    }

    void setLastOpenSessionBeaconSendTime(long timestamp) {
        lastOpenSessionBeaconSendTime = timestamp;
    }

    long getLastStatusCheckTime() {
        return lastStatusCheckTime;
    }

    void setLastStatusCheckTime(long timestamp) {
        lastStatusCheckTime = timestamp;
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

    SessionImpl getNextFinishedSession() {
        return finishedSessions.poll();
    }

    SessionImpl[] getAllOpenSessions() {
        return openSessions.toArray(new SessionImpl[0]);
    }

    long getLastTimeSyncTime() {
        return lastTimeSyncTime;
    }

    void setLastTimeSyncTime(long lastTimeSyncTime) {
        this.lastTimeSyncTime = lastTimeSyncTime;
    }

    public void startSession(SessionImpl session) {

        openSessions.add(session);
    }

    public void finishSession(SessionImpl session) {

        if (openSessions.remove(session)) {
            finishedSessions.add(session);
        }
    }
}
