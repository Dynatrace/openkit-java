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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.protocol.AdditionalQueryParameters;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Actual implementation of the {@link Session} interface.
 */
public class SessionImpl extends OpenKitComposite implements Session {

    /**
     * The maximum number of "new session requests" to send per session.
     */
    public static final int MAX_NEW_SESSION_REQUESTS = 4;

    /** {@link Logger} for tracing log message */
    private final Logger logger;

    /** Parent object of this {@link Session} */
    private OpenKitComposite parent;

    /** Beacon reference */
    private final Beacon beacon;

    /** current state of the session (also used for synchronization  */
    private final SessionStateImpl state;

    /** the number of tries for new session requests */
    private int numRemainingNewSessionRequests = MAX_NEW_SESSION_REQUESTS;
    /** the time when the session is to be ended (including a grace period from when the session was split by events) */
    private final AtomicLong splitByEventsGracePeriodEndTimeInMillis = new AtomicLong(-1);

    SessionImpl(Logger logger, OpenKitComposite parent, Beacon beacon) {
        this.state = new SessionStateImpl(this);
        this.logger = logger;
        this.parent = parent;
        this.beacon = beacon;

        beacon.startSession();
    }

    @Override
    public void close() {
        end();
    }

    @Override
    public RootAction enterAction(String actionName) {
        if (actionName == null || actionName.isEmpty()) {
            logger.warning(this + "enterAction: actionName must not be null or empty");
            return NullRootAction.INSTANCE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "enterAction(" + actionName + ")");
        }
        synchronized (state) {
            if (!state.isFinishingOrFinished()) {
                RootActionImpl result = new RootActionImpl(logger, this, actionName, beacon);
                storeChildInList(result);
                return result;
            }
        }

        return NullRootAction.INSTANCE;
    }

    @Override
    public void identifyUser(String userTag) {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "identifyUser(" + userTag + ")");
        }
        synchronized (state) {
            if (!state.isFinishingOrFinished()) {
                beacon.identifyUser(userTag);
            }
        }
    }

    @Override
    public void reportCrash(String errorName, String reason, String stacktrace) {
        if (errorName == null || errorName.isEmpty()) {
            logger.warning(this + "reportCrash: errorName must not be null or empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportCrash(" + errorName + ", " + reason + ", " + stacktrace + ")");
        }
        synchronized (state) {
            if (!state.isFinishingOrFinished()) {
                beacon.reportCrash(errorName, reason, stacktrace);
            }
        }
    }

    @Override
    public void reportCrash(Throwable throwable) {
        if (throwable == null) {
            logger.warning(this + "reportCrash: throwable must not be null");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportCrash(" + throwable + ")");
        }
        synchronized (state) {
            if (!state.isFinishingOrFinished()) {
                beacon.reportCrash(throwable);
            }
        }
    }

    @Override
    public WebRequestTracer traceWebRequest(URLConnection connection) {
        if (connection == null) {
            logger.warning(this + "traceWebRequest (URLConnection): connection must not be null");
            return NullWebRequestTracer.INSTANCE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "traceWebRequest (URLConnection) (" + connection + ")");
        }
        synchronized (state) {
            if (!state.isFinishingOrFinished()) {
                WebRequestTracerBaseImpl webRequestTracer = new WebRequestTracerURLConnection(logger, this, beacon, connection);
                storeChildInList(webRequestTracer);
                return webRequestTracer;
            }
        }

        return NullWebRequestTracer.INSTANCE;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        if (url == null || url.isEmpty()) {
            logger.warning(this + "traceWebRequest (String): url must not be null or empty");
            return NullWebRequestTracer.INSTANCE;
        }
        if (!WebRequestTracerStringURL.isValidURLScheme(url)) {
            logger.warning(this + "traceWebRequest (String): url \"" + url + "\" does not have a valid scheme");
            return NullWebRequestTracer.INSTANCE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "traceWebRequest (String) (" + url + ")");
        }
        synchronized (state) {
            if (!state.isFinishingOrFinished()) {
                WebRequestTracerBaseImpl webRequestTracer = new WebRequestTracerStringURL(logger, this, beacon, url);
                storeChildInList(webRequestTracer);
                return webRequestTracer;
            }
        }

        return NullWebRequestTracer.INSTANCE;
    }

    @Override
    public void end() {
        end(true);
    }

    public void end(boolean sendSessionEndEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "end()");
        }

        if (!state.markAsIsFinishing()) {
            return; // end() was already called before
        }

        // forcefully leave all child elements
        // Since the end time was set, no further child objects are added to the internal list
        // so the following operations are safe outside the synchronized block
        List<OpenKitObject> childObjects = getCopyOfChildObjects();
        for (OpenKitObject childObject : childObjects) {
            try {
                childObject.close();
            } catch (IOException e) {
                // should not happen, nevertheless let's log an error
                logger.error(this + "Caught IOException while closing OpenKitObject (" + childObject + ")", e);
            }
        }

        // send the end event, only if a session is explicitly ended
        if (sendSessionEndEvent) {
            beacon.endSession();
        }

        state.markAsFinished();

        // last but not least update parent relation
        parent.onChildClosed(this);
        parent = null;
    }

    /**
     * Tries to end the current session by checking if there are no more child objects (actions / web request tracers)
     * open. In case no more child objects are open the session is ended otherwise it is kept open.
     *
     * @return {@code true} if the session was successfully ended (or was already ended before). {@code false} in case
     *  there are / were still open child objects (actions / web request tracers).
     */
    public boolean tryEnd() {
        synchronized (state) {
            if (state.isFinishingOrFinished()) {
                return true;
            }

            if (getChildCount() == 0) {
                end(false);
                return true;
            }

            state.markAsWasTriedForEnding();
            return false;
        }
    }

    /**
     * Sets the end time when the session is to be actually ended after a session split by event count. It might
     * occur that a session is not yet ready to be finished (e.g. open actions, tracers) when the session split happens.
     * In this case the session is kept open and closed at a later time. The given end time is the point in time at
     * which the session will be closed forcefully by the {@link com.dynatrace.openkit.core.SessionWatchdog} thread.
     *
     * @param endTime the time when the session is to be closed for good.
     */
    public void setSplitByEventsGracePeriodEndTimeInMillis(long endTime) {
        splitByEventsGracePeriodEndTimeInMillis.compareAndSet(-1, endTime);
    }

    /**
     * Returns the time when the session is to be ended (after it was not possible to end the session after splitting
     * events e.g. due to actions still being open). The returned time already includes a grace period.
     */
    public long getSplitByEventsGracePeriodEndTimeInMillis() {
        return splitByEventsGracePeriodEndTimeInMillis.get();
    }

    /**
     * Sends the current beacon state.
     *
     * @param clientProvider Provider class providing the client for data transmission.
     * @param additionalParameters additional parameters that will be appended to the beacon request (can be {@code null}).
     *
     * @return Response from client.
     */
    public StatusResponse sendBeacon(HTTPClientProvider clientProvider, AdditionalQueryParameters additionalParameters) {
        return beacon.send(clientProvider, additionalParameters);
    }

    /**
     * Clears data that has been captured so far.
     *
     * <p>
     * This is called, when capturing is turned off to avoid having too much data.
     * </p>
     */
    public void clearCapturedData() {
        beacon.clearData();
    }

    /**
     * Test if this Session is empty or not.
     *
     * <p>
     * A session is considered to be empty, if it does not contain any action or event data.
     * </p>
     *
     * @return {@code true} if the session is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return beacon.isEmpty();
    }

    /**
     * Initializes the {@link Beacon} with the given {@link ServerConfiguration}
     */
    public void initializeServerConfiguration(ServerConfiguration initialServerConfig) {
        beacon.initializeServerConfiguration(initialServerConfig);
    }

    /**
     * Update the {@link Beacon} with the given {@link ServerConfiguration}
     */
    public void updateServerConfiguration(ServerConfiguration serverConfiguration) {
        beacon.updateServerConfiguration(serverConfiguration);
    }

    public SessionState getState() {
        return state;
    }

    @Override
    void onChildClosed(OpenKitObject childObject) {
        synchronized (state) {
            removeChildFromList(childObject);

            if (state.wasTriedForEnding() && getChildCount() == 0) {
                end(false);
            }
        }
    }

    /**
     * Indicates whether sending data for this session is allowed or not.
     */
    public boolean isDataSendingAllowed() {
        return state.isConfigured() && beacon.isDataCapturingEnabled();
    }

    /**
     * Enables capturing for this session.
     *
     * <p>
     *     Will implicitly also set the {@link #getState() session state} to {@link SessionState#isConfigured() configured}.
     * </p>
     */
    public void enableCapture() {
        beacon.enableCapture();
    }

    /**
     * Disables capturing for this session.
     *
     * <p>
     *     Will implicitly also set the {@link #getState() session state} to {@link SessionState#isConfigured() configured}.
     * </p>
     */
    public void disableCapture() {
        beacon.disableCapture();
    }

    /**
     * Indicates whether new session requests can be sent or not.
     *
     * <p>
     *     This is directly related to {@link #decreaseNumRemainingSessionRequests()}.
     * </p>
     */
    public boolean canSendNewSessionRequest() {
        return numRemainingNewSessionRequests > 0;
    }

    /**
     * Decreases the number of remaining new session requests.
     *
     * <p>
     *     In case no more new session requests remain, {@link #canSendNewSessionRequest()} will return {@code false}
     * </p>
     */
    public void decreaseNumRemainingSessionRequests() {
        numRemainingNewSessionRequests--;
    }

    /**
     * Returns the beacon of this session.
     */
    Beacon getBeacon() {
        return beacon;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + "] ";
    }

    /**
     * Implements the internal state of the {@link Session}
     */
    private static class SessionStateImpl implements SessionState {

        private final SessionImpl session;
        private boolean isFinishing = false;
        private boolean isFinished = false;
        private boolean wasTriedForEnding = false;

        private SessionStateImpl(SessionImpl session) {
            this.session = session;
        }

        @Override
        public synchronized boolean wasTriedForEnding() {
            return wasTriedForEnding;
        }

        @Override
        public synchronized boolean isConfigured() {
            return session.beacon.isServerConfigurationSet();
        }

        @Override
        public synchronized boolean isConfiguredAndFinished() {
            return isConfigured() && isFinished;
        }

        @Override
        public synchronized boolean isConfiguredAndOpen() {
            return isConfigured() && !isFinished;
        }

        @Override
        public synchronized boolean isFinished() {
            return isFinished;
        }

        private synchronized boolean isFinishingOrFinished() {
            return isFinishing || isFinished;
        }

        private synchronized boolean markAsIsFinishing() {
            if (isFinishingOrFinished()) {
                return false;
            }

            isFinishing = true;
            return true;
        }

        private synchronized void markAsFinished() {
            isFinished = true;
        }

        private synchronized void markAsWasTriedForEnding() {
            wasTriedForEnding = true;
        }
    }
}
