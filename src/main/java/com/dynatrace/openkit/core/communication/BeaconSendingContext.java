/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.objects.SessionImpl;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    private final Logger logger;
    private final Configuration configuration;
    private final HTTPClientProvider httpClientProvider;
    private final TimingProvider timingProvider;

    /**
     * container storing all sessions
     */
    private final LinkedBlockingQueue<SessionWrapper> sessions = new LinkedBlockingQueue<SessionWrapper>();

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
     * state following after current state, nextState is usually set by doExecute of the current state
     */
    private AbstractBeaconSendingState nextState;
    /**
     * timestamp when open sessions were last sent
     */
    private long lastOpenSessionBeaconSendTime;
    /**
     * timestamp when last status check was done
     */
    private long lastStatusCheckTime;
    /**
     * boolean indicating whether init was successful or not
     */
    private final AtomicBoolean initSucceeded = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * <p>
     *     The state is initialized to {@link BeaconSendingInitState},
     * </p>
     */
    public BeaconSendingContext(Logger logger, Configuration configuration,
                                HTTPClientProvider httpClientProvider,
                                TimingProvider timingProvider) {
        this(logger, configuration, httpClientProvider, timingProvider, new BeaconSendingInitState());
    }

    /**
     * Constructor.
     *
     * <p>
     *     The initial state is provided. This constructor is intended for unit testing.
     * </p>
     */
    public BeaconSendingContext(Logger logger, Configuration configuration, HTTPClientProvider httpClientProvider,
            TimingProvider timingProvider, AbstractBeaconSendingState initialState) {
        this.logger = logger;
        this.configuration = configuration;
        this.httpClientProvider = httpClientProvider;
        this.timingProvider = timingProvider;

        currentState = initialState;
    }

    /**
     * Executes the current state.
     */
    public void executeCurrentState() {
        nextState = null;
        currentState.execute(this);

        if (nextState != null && nextState != currentState) { // currentState.execute(...) can trigger state changes
            if (logger.isInfoEnabled()) {
                logger.info(getClass().getSimpleName() + " executeCurrentState() - State change from '" + currentState + "' to '" + nextState + "'");
            }
            currentState = nextState;
        }
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
     * @return {@code true} OpenKit is fully initialized, {@code false} OpenKit init got interrupted.
     */
    public boolean waitForInit() {
        try {
            initCountDownLatch.await();
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }
        return initSucceeded.get();
    }

    /**
     * Wait until OpenKit has been fully initialized or timeout expired.
     *
     * <p>
     *     If initialization is interrupted (e.g. {@link #requestShutdown()} was called), then this method also returns.
     * </p>
     *
     * @param timeoutMillis
     *            The maximum number of milliseconds to wait for initialization being completed.
     * @return {@code true} if OpenKit is fully initialized, {@code false} if OpenKit init got interrupted or time to wait expired.
     */
    public boolean waitForInit(long timeoutMillis) {
        try {
            if (!initCountDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return false; // timeout expired
            }
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }

        return initSucceeded.get();
    }

    /**
     * Get a boolean indicating whether OpenKit is initialized or not.
     *
     * @return {@code true} if OpenKit is initialized, {@code false} otherwise.
     */
    public boolean isInitialized() {
        return initSucceeded.get();
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
    boolean isCaptureOn() {
        return configuration.isCapture();
    }

    /**
     * Gets the current state.
     *
     * @return current state.
     */
    AbstractBeaconSendingState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the next state.
     *
     * @param nextState Next state when state transition is performed.
     */
    void setNextState(AbstractBeaconSendingState nextState) {
        this.nextState = nextState;
    }

    /**
     * Returns the next state.
     *
     * @return the nextState
     */
    AbstractBeaconSendingState getNextState() {
        return nextState;
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
        initSucceeded.set(success);
        initCountDownLatch.countDown();
    }

    /**
     * Gets the HTTP client provider.
     *
     * @return A class responsible for retrieving an instance of {@link HTTPClient}.
     */
    HTTPClientProvider getHTTPClientProvider() {
        return httpClientProvider;
    }

    /**
     * Convenience method to retrieve an HTTP client.
     *
     * @return HTTP client received from {@link HTTPClientProvider}.
     */
    HTTPClient getHTTPClient() {
        return httpClientProvider.createClient(configuration.getHttpClientConfig());
    }

    /**
     * Gets the current timestamp.
     *
     * @return current timestamp as milliseconds elapsed since epoch (1970-01-01T00:00:00.000)
     */
    long getCurrentTimestamp() {
        return timingProvider.provideTimestampInMilliseconds();
    }

    /**
     * Sleep some time ({@link #DEFAULT_SLEEP_TIME_MILLISECONDS}.
     *
     * @throws InterruptedException When sleeping thread got interrupted.
     */
    void sleep() throws InterruptedException {
        sleep(DEFAULT_SLEEP_TIME_MILLISECONDS);
    }

    /**
     * Sleep given amount of milliseconds.
     *
     * @param millis The number of milliseconds to sleep.
     * @throws InterruptedException When sleeping thread got interrupted.
     */
    void sleep(long millis) throws InterruptedException {
        timingProvider.sleep(millis);
    }

    /**
     * Get timestamp when open sessions were sent last.
     */
    long getLastOpenSessionBeaconSendTime() {
        return lastOpenSessionBeaconSendTime;
    }

    /**
     * Set timestamp when open sessions were sent last.
     */
    void setLastOpenSessionBeaconSendTime(long timestamp) {
        lastOpenSessionBeaconSendTime = timestamp;
    }

    /**
     * Get timestamp when last status check was performed.
     */
    long getLastStatusCheckTime() {
        return lastStatusCheckTime;
    }

    /**
     * Set timestamp when last status check was performed.
     */
    void setLastStatusCheckTime(long timestamp) {
        lastStatusCheckTime = timestamp;
    }

    /**
     * Get the send interval for open sessions.
     */
    int getSendInterval() {
        return configuration.getSendInterval();
    }

    /**
     * Disable data capturing.
     */
    void disableCapture() {
        // first disable in configuration, so no further data will get collected
        configuration.disableCapture();
        clearAllSessionData();
    }

    /**
     * Handle the status response received from the server.
     */
    void handleStatusResponse(StatusResponse statusResponse) {
        configuration.updateSettings(statusResponse);

        if (!isCaptureOn()) {
            // capturing was turned off
            clearAllSessionData();
        }
    }

    /**
     * Clear captured data from all sessions.
     */
    private void clearAllSessionData() {

        // iterate over the elements
        Iterator<SessionWrapper> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            SessionWrapper wrapper = iterator.next();
            wrapper.clearCapturedData();
            if (wrapper.isSessionFinished()) {
                iterator.remove();
            }
        }
    }

    /**
     * Start a new session.
     *
     * <p>
     *     This add the {@code session} to the internal container of sessions.
     * </p>
     *
     * @param session The new session to start.
     */
    public void startSession(SessionImpl session) {
        sessions.add(new SessionWrapper(session));
    }

    /**
     * Get all sessions that are considered new.
     *
     * <p>
     *     The returned list is a snapshot and might change during traversal.
     * </p>
     *
     * @return A list of new sessions.
     */
    List<SessionWrapper> getAllNewSessions() {

        List<SessionWrapper> newSessions = new LinkedList<SessionWrapper>();

        for (SessionWrapper sessionWrapper : sessions) {
            if (!sessionWrapper.isBeaconConfigurationSet()) {
                newSessions.add(sessionWrapper);
            }
        }

        return newSessions;
    }

    /**
     * Get a list of all sessions that have been configured and are currently open.
     */
    List<SessionWrapper> getAllOpenAndConfiguredSessions() {

        List<SessionWrapper> openSessions = new LinkedList<SessionWrapper>();

        for (SessionWrapper sessionWrapper : sessions) {
            if (sessionWrapper.isBeaconConfigurationSet() && !sessionWrapper.isSessionFinished()) {
                openSessions.add(sessionWrapper);
            }
        }

        return openSessions;
    }

    /**
     * Get a list of all sessions that have been configured and are currently finished.
     */
    List<SessionWrapper> getAllFinishedAndConfiguredSessions() {

        List<SessionWrapper> finishedSessions = new LinkedList<SessionWrapper>();

        for (SessionWrapper sessionWrapper : sessions) {
            if (sessionWrapper.isBeaconConfigurationSet() && sessionWrapper.isSessionFinished()) {
                finishedSessions.add(sessionWrapper);
            }
        }

        return finishedSessions;
    }

    /**
     * Finish a session which has been started previously using {@link #startSession(SessionImpl)}.
     *
     * @param session The session to finish.
     */
    public void finishSession(SessionImpl session) {

        SessionWrapper sessionWrapper = findSessionWrapper(session);
        if (sessionWrapper != null) {
            sessionWrapper.finishSession();
        }
    }

    /**
     * Search and find {@link SessionWrapper} for given {@link SessionImpl}.
     *
     * @param session The session for which to search for appropriate wrapper.
     * @return Appropriate wrapper or {@code null} if not found.
     */
    private SessionWrapper findSessionWrapper(SessionImpl session) {

        for (SessionWrapper sessionWrapper : sessions) {
            if (sessionWrapper.getSession().equals(session)) {
                return sessionWrapper;
            }
        }

        return null;
    }

    /**
     * Remove {@link SessionWrapper} from list of all wrappers.
     *
     * @param sessionWrapper The wrapper to remove.
     */
    boolean removeSession(SessionWrapper sessionWrapper) {
        return sessions.remove(sessionWrapper);
    }
}
