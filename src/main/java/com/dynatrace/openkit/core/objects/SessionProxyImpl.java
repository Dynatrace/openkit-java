/**
 * Copyright 2018-2021 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.SessionWatchdog;
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfigurationUpdateCallback;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.TimingProvider;
import com.dynatrace.openkit.util.json.objects.JSONValue;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Implements a surrogate for a {@link Session} to perform session splitting after:
 * <ul>
 *     <li>a configured number of events</li>
 *     <li>after a configured idle timeout</li>
 *     <li>after a configured maximum session duration</li>
 * </ul>
 */
public class SessionProxyImpl extends OpenKitComposite implements Session, ServerConfigurationUpdateCallback {

    // object used for synchronization.
    private final Object lockObject = new Object();
    // log message reporter
    private final Logger logger;
    // Parent object of this session proxy
    private final OpenKitComposite parent;
    // creator for split sessions
    private final SessionCreator sessionCreator;
    // provider to obtain the current time
    private final TimingProvider timingProvider;
    // sender of beacon data
    private final BeaconSender beaconSender;
    // watchdog to split sessions after idle/max timeout or to close split off sessions which were not closable on split
    private final SessionWatchdog sessionWatchdog;
    // the current session instance
    private SessionImpl currentSession;
    // holds the number of received calls to enterAction
    private int topLevelActionCount = 0;
    // specifies the timestamp when the last top level event happened
    private long lastInteractionTime;
    // the server configuration of the first session (will be initialized when first session is updated with server config)
    private ServerConfiguration serverConfiguration;
    // indicates if this session proxy was already finished
    private boolean isFinished;
    // last user tag reported via identifyUser
    private String lastUserTag = null;

    SessionProxyImpl(
            Logger logger,
            OpenKitComposite parent,
            SessionCreator sessionCreator,
            TimingProvider timingProvider,
            BeaconSender beaconSender,
            SessionWatchdog sessionWatchdog
    ) {
        this.logger = logger;
        this.parent = parent;
        this.sessionCreator = sessionCreator;
        this.timingProvider = timingProvider;
        this.beaconSender = beaconSender;
        this.sessionWatchdog = sessionWatchdog;

        ServerConfiguration currentServerConfig = beaconSender.getLastServerConfiguration();
        createInitialSessionAndMakeCurrent(currentServerConfig);
    }

    @Override
    public RootAction enterAction(String actionName) {
        if (actionName == null || actionName.isEmpty()) {
            logger.warning(this + " enterAction: actionName must not be null or empty");
            return NullRootAction.INSTANCE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " enterAction(" + actionName + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSessionByEvents();
                if (session.getBeacon().isActionReportingAllowedByPrivacySettings()) {
                    // avoid session splitting by action count, if user opted out of action collection
                    recordTopActionEvent();
                } else {
                    recordTopLevelEventInteraction();
                }
                return session.enterAction(actionName);
            }
        }

        return NullRootAction.INSTANCE;
    }

    @Override
    public void identifyUser(String userTag) {
        if (logger.isDebugEnabled()) {
            logger.debug(this + " identifyUser(" + userTag + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSessionByEvents();
                recordTopLevelEventInteraction();
                session.identifyUser(userTag);
                lastUserTag = userTag;
            }
        }
    }

    @Override
    public void reportCrash(String errorName, String reason, String stacktrace) {
        if (errorName == null || errorName.isEmpty()) {
            logger.warning(this + " reportCrash: errorName must not be null or empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " reportCrash(" + errorName + ", " + reason + ", " + stacktrace + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSessionByEvents();
                recordTopLevelEventInteraction();
                session.reportCrash(errorName, reason, stacktrace);

                // create new session after crash report
                splitAndCreateNewInitialSession();
            }
        }
    }

    @Override
    public void reportCrash(Throwable throwable) {
        if (throwable == null) {
            logger.warning(this + " reportCrash: throwable must not be null");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " reportCrash(" + throwable + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSessionByEvents();
                recordTopLevelEventInteraction();
                session.reportCrash(throwable);

                // create new session after crash report
                splitAndCreateNewInitialSession();
            }
        }
    }

    @Override
    public WebRequestTracer traceWebRequest(URLConnection connection) {
        if (connection == null) {
            logger.warning(this + " traceWebRequest (URLConnection): connection must not be null");
            return NullWebRequestTracer.INSTANCE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " traceWebRequest (URLConnection) (" + connection + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSessionByEvents();
                recordTopLevelEventInteraction();
                return session.traceWebRequest(connection);
            }
        }

        return NullWebRequestTracer.INSTANCE;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        if (url == null || url.isEmpty()) {
            logger.warning(this + " traceWebRequest (String): url must not be null or empty");
            return NullWebRequestTracer.INSTANCE;
        }
        if (!WebRequestTracerStringURL.isValidURLScheme(url)) {
            logger.warning(this + " traceWebRequest (String): url \"" + url + "\" does not have a valid scheme");
            return NullWebRequestTracer.INSTANCE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " traceWebRequest (String) (" + url + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                Session session = getOrSplitCurrentSessionByEvents();
                recordTopLevelEventInteraction();
                return session.traceWebRequest(url);
            }
        }

        return NullWebRequestTracer.INSTANCE;
    }

    void sendEvent(String name, Map<String, JSONValue> attributes) {
        if (name == null || name.isEmpty()) {
            logger.warning(this + " sendEvent (String, Map): name must not be null or empty");
            return;
        }

        if (attributes != null && attributes.containsKey("name")) {
            logger.warning(this + " sendEvent (String, Map): name must not be used in the attributes as it will be overridden!");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this + " sendEvent(" + name + ", " + attributes.toString() + ")");
        }

        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSessionByEvents();
                recordTopLevelEventInteraction();
                session.sendEvent(name, attributes);
            }
        }
    }

    @Override
    public void end() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + " end()");
        }

        synchronized (lockObject) {
            if (isFinished) {
                return;
            }
            isFinished = true;
        }

        closeChildObjects();

        parent.onChildClosed(this);
        sessionWatchdog.removeFromSplitByTimeout(this);
    }

    /**
     * Close all child objects of this {@link SessionProxyImpl} which are still open.
     */
    void closeChildObjects() {
        List<OpenKitObject> childObjects = getCopyOfChildObjects();

        for (OpenKitObject childObject : childObjects) {
            if (childObject instanceof SessionImpl) {
                // child object is a session - special treatment is needed for sessions
                SessionImpl childSession = (SessionImpl) childObject;
                // end the child session and send the end session event
                // if the child session is the current session
                childSession.end(childSession == currentSession);
            } else {
                closeChildObject(childObject);
            }
        }
    }

    /**
     * Close single child object.
     *
     * @param childObject Child object to close.
     */
    private void closeChildObject(OpenKitObject childObject) {
        try {
            childObject.close();
        } catch (IOException e) {
            // should not happen, nevertheless let's log an error
            logger.error(this + "Caught IOException while closing OpenKitObject (" + childObject + ")", e);
        }
    }

    /**
     * Indicates whether this session proxy was finished or is still open.
     */
    public boolean isFinished() {
        synchronized (lockObject) {
            return isFinished;
        }
    }

    @Override
    public void close() {
        end();
    }

    @Override
    void onChildClosed(OpenKitObject childObject) {
        synchronized (lockObject) {
            removeChildFromList(childObject);
            if (childObject instanceof SessionImpl) {
                sessionWatchdog.dequeueFromClosing((SessionImpl) childObject);
            }
        }
    }

    /**
     * Returns the number of top level action calls which were made to the current session. Intended to be used by unit
     * tests only.
     */
    int getTopLevelActionCount() {
        synchronized (lockObject) {
            return topLevelActionCount;
        }
    }

    /**
     * Returns the time when the last top level event was called. Intended to be used by unit tests only.
     */
    long getLastInteractionTime() {
        synchronized (lockObject) {
            return lastInteractionTime;
        }
    }

    /**
     * Returns the server configuration of this session proxy. Intended to be used by unit tests only.
     */
    ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    /**
     * Returns the current active session or creates a new session if {@link #isSessionSplitByEventsRequired()}.
     */
    private SessionImpl getOrSplitCurrentSessionByEvents() {
        if (isSessionSplitByEventsRequired()) {
            closeOrEnqueueCurrentSessionForClosing();
            createSplitSessionAndMakeCurrent(serverConfiguration);
            reTagCurrentSession();
        }
        return currentSession;
    }

    /**
     * Indicates if the maximum number of top level actions is reached and session splitting by events needs to be
     * performed.
     */
    private boolean isSessionSplitByEventsRequired() {
        if (serverConfiguration == null || !serverConfiguration.isSessionSplitByEventsEnabled()) {
            return false;
        }

        return serverConfiguration.getMaxEventsPerSession() <= topLevelActionCount;
    }

    /**
     * Will end the current active session, enque the old one for closing, and create a new session.
     *
     * <p>
     * The new session is created using the {@see #createInitialSession}.
     * </p>
     *
     * <p>
     * This method must be called only when the {@link #lockObject} is held.
     * </p>
     */
    private void splitAndCreateNewInitialSession() {
        closeOrEnqueueCurrentSessionForClosing();

        // create a completely new SessionImpl
        sessionCreator.reset();
        createInitialSessionAndMakeCurrent(serverConfiguration);
        reTagCurrentSession();
    }

    private void closeOrEnqueueCurrentSessionForClosing() {
        // for grace period use half of the idle timeout
        // or fallback to session interval if not configured
        int closeGracePeriodInMillis = serverConfiguration.getSessionTimeoutInMilliseconds() > 0
                ? serverConfiguration.getSessionTimeoutInMilliseconds() / 2
                : serverConfiguration.getSendIntervalInMilliseconds();

        sessionWatchdog.closeOrEnqueueForClosing(currentSession, closeGracePeriodInMillis);
    }


    /**
     * Will end the current active session and start a new one but only if the following conditions are met:
     * <ul>
     *     <li>this session proxy is not {@link #isFinished() finished}.</li>
     *     <li>
     *          session splitting by idle timeout is enabled and the current session was idle for longer than the
     *          configured timeout.
     *     </li>
     *     <li>
     *         session splitting by maximum session duration is enabled and the session was open for longer than the
     *         maximum configured session duration.
     *     </li>
     * </ul>
     *
     * @return the time when the session might be split next. This can either be the time when the maximum session
     * duration is reached or the time when the idle timeout expires. In case this session proxy is finished, {@code -1}
     * is returned.
     */
    public long splitSessionByTime() {
        synchronized (lockObject) {
            if (isFinished()) {
                return -1;
            }

            long nextSplitTime = calculateNextSplitTime();
            long now = timingProvider.provideTimestampInMilliseconds();
            if (nextSplitTime < 0 || now < nextSplitTime) {
                return nextSplitTime;
            }

            splitAndCreateNewInitialSession();

            return calculateNextSplitTime();
        }
    }

    /**
     * Calculates and returns the next point in time when this session is to be split. The returned time might either be
     * <ul>
     *     <li>the time when the session expires after the max. session duration elapsed.</li>
     *     <li>the time when the session expires after being idle.</li>
     * </ul>
     * depending on which happens earlier.
     */
    private long calculateNextSplitTime() {
        if (serverConfiguration == null) {
            return -1;
        }

        boolean splitByIdleTimeout = serverConfiguration.isSessionSplitByIdleTimeoutEnabled();
        boolean splitBySessionDuration = serverConfiguration.isSessionSplitBySessionDurationEnabled();

        long idleTimeOut = lastInteractionTime + serverConfiguration.getSessionTimeoutInMilliseconds();
        long sessionMaxTime = currentSession.getBeacon().getSessionStartTime()
                + serverConfiguration.getMaxSessionDurationInMilliseconds();

        if (splitByIdleTimeout && splitBySessionDuration) {
            return Math.min(idleTimeOut, sessionMaxTime);
        } else if (splitByIdleTimeout) {
            return idleTimeOut;
        } else if (splitBySessionDuration) {
            return sessionMaxTime;
        }

        return -1;
    }

    private void createInitialSessionAndMakeCurrent(ServerConfiguration initialServerConfig) {
        createAndAssignCurrentSession(initialServerConfig, null);
    }

    private void createSplitSessionAndMakeCurrent(ServerConfiguration updatedServerConfig) {
        createAndAssignCurrentSession(null, updatedServerConfig);
    }

    /**
     * Creates a new session and adds it to the beacon sender. The top level action count is reset to zero and the last
     * interaction time is set to the current timestamp.
     *
     * <p>
     * In case the given {@code initialServerConfig} is not null, the new session will be initialized with this server
     * configuration. The created session however will not be in state {@link SessionState#isConfigured() configured},
     * meaning new session requests will be performed for this session.
     * </p>
     * <p>
     * In case the given {@code updatedServerConfig} is not null, the new session will be updated with this server
     * configuration. The created session will be in state {@link SessionState#isConfigured()}, meaning new session
     * requests will be omitted.
     * </p>
     *
     * @param initialServerConfig the server configuration with which the session will be initialized. Can be {@code null}.
     * @param updatedServerConfig the server configuration with which the session will be updated. Can be {@code null}.
     */
    private void createAndAssignCurrentSession(ServerConfiguration initialServerConfig, ServerConfiguration updatedServerConfig) {
        SessionImpl session = sessionCreator.createSession(this);
        Beacon beacon = session.getBeacon();
        beacon.setServerConfigurationUpdateCallback(this);
        storeChildInList(session);

        lastInteractionTime = beacon.getSessionStartTime();
        topLevelActionCount = 0;

        if (initialServerConfig != null) {
            session.initializeServerConfiguration(initialServerConfig);
        }

        if (updatedServerConfig != null) {
            session.updateServerConfiguration(updatedServerConfig);
        }

        synchronized (lockObject) {
            // synchronize access
            currentSession = session;
        }

        this.beaconSender.addSession(session);
    }

    private void recordTopLevelEventInteraction() {
        lastInteractionTime = timingProvider.provideTimestampInMilliseconds();
    }

    private void recordTopActionEvent() {
        ++topLevelActionCount;
        recordTopLevelEventInteraction();
    }

    private void reTagCurrentSession() {
        if (lastUserTag == null || lastUserTag.length() == 0 || currentSession == null) {
            return;
        }

        currentSession.identifyUser(lastUserTag);
    }

    @Override
    public void onServerConfigurationUpdate(ServerConfiguration serverConfig) {
        synchronized (lockObject) {
            if (serverConfiguration != null) {
                serverConfiguration = serverConfiguration.merge(serverConfig);
                return;
            }

            serverConfiguration = serverConfig;

            if (isFinished()) {
                return;
            }

            if (serverConfiguration.isSessionSplitBySessionDurationEnabled() ||
                    serverConfiguration.isSessionSplitByIdleTimeoutEnabled()) {
                sessionWatchdog.addToSplitByTimeout(this);
            }
        }
    }

    @Override
    public String toString() {
        Beacon beacon = currentSession.getBeacon();
        return getClass().getSimpleName()
                + " [sn=" + beacon.getSessionNumber() + ", seq=" + beacon.getSessionSequenceNumber() + "]";
    }
}
