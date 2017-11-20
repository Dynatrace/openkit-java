package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * State context for beacon sending states.
 */
public class BeaconSendingContext {

    /**
     * Default sleep time in milliseconds (used by {@link #sleep()}).
     */
    static final long DEFAULT_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

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
     *
     * <p>
     *     The state is initialized to {@link BeaconSendingInitState},
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

    /**
     * Executes the current state.
     */
    public void executeCurrentState() {
        currentState.execute(this);
    }

    /**
     * Requests a shutdown.
     */
    public void requestShutdown() {
        shutdown.set(true);
    }

    /**
     * Gets a boolean flag indicating whether shutdown was requested before or not.
     */
    public boolean isShutdownRequested() {
        return shutdown.get();
    }

    /**
     * Wait until OpenKit has been fully initialized.
     *
     * <p>
     *     If initialization is interrupted (e.g. {@link #requestShutdown()} was called), then this method also returns.
     * </p>
     *
     * @return {@code} true OpenKit is fully initialized, {@code false} OpenKit init got interrupted.
     */
    public boolean waitForInit() {
        try {
            initCountDownLatch.await();
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }
        return initSucceeded;
    }

    /**
     * Gets a boolean indicating whether the current state is a terminal state or not.
     *
     * @return {@code true} if the current state is a terminal state, {@code false} otherwise.
     */
    public boolean isInTerminalState() {
        return currentState.isTerminalState();
    }

    /**
     * Gets a boolean flag indicating whether capturing is turned on or off.
     *
     * @return {@code true} if capturing is turned on, {@code false} otherwise.
     */
    public boolean isCaptureOn() {
        return configuration.isCapture();
    }

    /**
     * Gets a boolean flag indicating whether time sync is supported or not.
     *
     * @return {@code true} if time sync is supported, {@code false} otherwise.
     */
    boolean isTimeSyncSupported() {
        return timeSyncSupported;
    }

    /**
     * Disable the time sync support.
     *
     * <p>
     *     Note: There is no way to re-enable it, because as soon as a server tells OpenKit it does not support
     *     time syncing, a time sync request is never again re-executed.
     * </p>
     */
    void disableTimeSyncSupport() {
        timeSyncSupported = false;
    }

    /**
     * Gets the current state.
     * @return current state.
     */
    AbstractBeaconSendingState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current state.
     * @param newState The "new" current state, after performing state transition.
     */
    void setCurrentState(AbstractBeaconSendingState newState) {
        currentState = newState;
    }

    /**
     * Complete OpenKit initialization.
     *
     * <p>
     *     This will wake up every caller waiting in the {@link #waitForInit()} method.
     * </p>
     *
     * @param success {@code true} if OpenKit was successfully initialized, {@code false} if it was interrupted.
     */
    void initCompleted(boolean success) {
        initSucceeded = success;
        initCountDownLatch.countDown();
    }

    /**
     * Gets the HTTP client provider.
     * @return A class responsible for retrieving an instance of {@link HTTPClient}.
     */
    HTTPClientProvider getHTTPClientProvider() {
        return httpClientProvider;
    }

    /**
     * Convenience method to retrieve an HTTP client.
     * @return HTTP client received from {@link HTTPClientProvider}.
     */
    HTTPClient getHTTPClient() {
        return httpClientProvider.createClient(configuration.getHttpClientConfig());
    }

    /**
     * Gets the current timestamp.
     * @return current timestamp as milliseconds elapsed since epoch (1970-01-01T00:00:00.000)
     */
    long getCurrentTimestamp() {
        return timingProvider.provideTimestampInMilliseconds();
    }

    /**
     * Sleep some time ({@link #DEFAULT_SLEEP_TIME_MILLISECONDS}.
     * @throws InterruptedException When sleeping thread got interrupted.
     */
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

        if (!isCaptureOn()) {
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
