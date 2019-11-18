/**
 * Copyright 2018-2019 Dynatrace LLC
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
import com.dynatrace.openkit.core.configuration.ServerConfiguration;
import com.dynatrace.openkit.core.configuration.ServerConfigurationUpdateCallback;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

/**
 * Implements a surrogate for a {@link Session} to perform session splitting after a configured number of events.
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
    // the current session instance
    private SessionImpl currentSession;
    // holds the number of received calls to any of the top level events (identify user, enter action, ...)
    private int topLevelEventCount = 0;
    // the server configuration of the first session (will be initialized when first session is updated with server config)
    private ServerConfiguration serverConfiguration;
    // indicates if this session proxy was already finished
    private boolean isFinished;

    SessionProxyImpl(
            Logger logger,
            OpenKitComposite parent,
            SessionCreator sessionCreator
    ) {
        this.logger = logger;
        this.parent = parent;
        this.sessionCreator = sessionCreator;

        this.currentSession = createSession();
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
                SessionImpl session = getOrSplitCurrentSession();
                recordTopLevelEventInvocation();
                return session.enterAction(actionName);
            }
        }

        return NullRootAction.INSTANCE;
    }

    @Override
    public void identifyUser(String userTag) {
        if (userTag == null || userTag.isEmpty()) {
            logger.warning(this + " identifyUser: userTag must not be null or empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " identifyUser(" + userTag + ")");
        }
        synchronized (lockObject) {
            if (!isFinished) {
                SessionImpl session = getOrSplitCurrentSession();
                recordTopLevelEventInvocation();
                session.identifyUser(userTag);
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
                SessionImpl session = getOrSplitCurrentSession();
                recordTopLevelEventInvocation();
                session.reportCrash(errorName, reason, stacktrace);
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
                SessionImpl session = getOrSplitCurrentSession();
                recordTopLevelEventInvocation();
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
                Session session = getOrSplitCurrentSession();
                recordTopLevelEventInvocation();
                return session.traceWebRequest(url);
            }
        }

        return NullWebRequestTracer.INSTANCE;
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

        List<OpenKitObject> childObjects = getCopyOfChildObjects();
        for (OpenKitObject childObject : childObjects) {
            try {
                childObject.close();
            } catch (IOException e) {
                // should not happen, nevertheless let's log an error
                logger.error(this + "Caught IOException while closing OpenKitObject (" + childObject + ")", e);
            }
        }

        parent.onChildClosed(this);
    }

    @Override
    public void close() {
        end();
    }

    @Override
    void onChildClosed(OpenKitObject childObject) {
        synchronized (lockObject) {
            removeChildFromList(childObject);
        }
    }

    int getTopLevelEventCount() {
        synchronized (lockObject) {
            return topLevelEventCount;
        }
    }

    private SessionImpl getOrSplitCurrentSession() {
        if (isSessionSplitRequired()) {
            currentSession = createSession();
            currentSession.updateServerConfiguration(serverConfiguration);
            topLevelEventCount = 0;
        }
        return currentSession;
    }

    private boolean isSessionSplitRequired() {
        if (serverConfiguration == null || !serverConfiguration.isSessionSplitByEventsEnabled()) {
            return false;
        }

        return serverConfiguration.getMaxEventsPerSession() <= topLevelEventCount;
    }

    private SessionImpl createSession() {
        SessionImpl session = sessionCreator.createSession(this);
        session.getBeacon().setServerConfigurationUpdateCallback(this);
        storeChildInList(session);

        return session;
    }

    private void recordTopLevelEventInvocation() {
        ++topLevelEventCount;
    }

    @Override
    public void onServerConfigurationUpdate(ServerConfiguration serverConfig) {
        synchronized (lockObject) {
            if(serverConfiguration == null) {
                serverConfiguration = serverConfig;
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
