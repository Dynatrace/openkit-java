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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

/**
 * Actual implementation of the {@link Session} interface.
 */
public class SessionImpl extends OpenKitComposite implements Session {

    /** {@link Logger} for tracing log message */
    private final Logger logger;

    /** object for synchronization */
    private final Object lockObject = new Object();

    /** Parent object of this {@link Session} */
    private OpenKitComposite parent;

    /** Root action returned in case of misconfiguration */
    private static final RootAction NULL_ROOT_ACTION = new NullRootAction();
    /** Web request tracer returned in case of misconfiguration */
    private static final WebRequestTracer NULL_WEB_REQUEST_TRACER = new NullWebRequestTracer();

    /** end time of this {@link Session} */
    private long endTime = -1L;

    // BeaconSender and Beacon reference
    private final BeaconSender beaconSender;
    private final Beacon beacon;

    SessionImpl(Logger logger, OpenKitComposite parent, BeaconSender beaconSender, Beacon beacon) {
        this.logger = logger;
        this.parent = parent;
        this.beaconSender = beaconSender;
        this.beacon = beacon;

        beaconSender.startSession(this);
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
            return NULL_ROOT_ACTION;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "enterAction(" + actionName + ")");
        }
        synchronized (lockObject) {
            if (!isSessionEnded()) {
                RootActionImpl result = new RootActionImpl(logger, this, actionName, beacon);
                storeChildInList(result);
                return result;
            }
        }

        return NULL_ROOT_ACTION;
    }

    @Override
    public void identifyUser(String userTag) {
        if (userTag == null || userTag.isEmpty()) {
            logger.warning(this + "identifyUser: userTag must not be null or empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "identifyUser(" + userTag + ")");
        }
        synchronized (lockObject) {
            if (!isSessionEnded()) {
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
        synchronized (lockObject) {
            if (!isSessionEnded()) {
                beacon.reportCrash(errorName, reason, stacktrace);
            }
        }
    }

    @Override
    public WebRequestTracer traceWebRequest(URLConnection connection) {
        if (connection == null) {
            logger.warning(this + "traceWebRequest (URLConnection): connection must not be null");
            return NULL_WEB_REQUEST_TRACER;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "traceWebRequest (URLConnection) (" + connection + ")");
        }
        synchronized (lockObject) {
            if (!isSessionEnded()) {
                WebRequestTracerBaseImpl webRequestTracer = new WebRequestTracerURLConnection(logger, this, beacon, connection);
                storeChildInList(webRequestTracer);
                return webRequestTracer;
            }
        }

        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        if (url == null || url.isEmpty()) {
            logger.warning(this + "traceWebRequest (String): url must not be null or empty");
            return NULL_WEB_REQUEST_TRACER;
        }
        if (!WebRequestTracerStringURL.isValidURLScheme(url)) {
            logger.warning(this + "traceWebRequest (String): url \"" + url + "\" does not have a valid scheme");
            return NULL_WEB_REQUEST_TRACER;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "traceWebRequest (String) (" + url + ")");
        }
        synchronized (lockObject) {
            if (!isSessionEnded()) {
                WebRequestTracerBaseImpl webRequestTracer = new WebRequestTracerStringURL(logger, this, beacon, url);
                storeChildInList(webRequestTracer);
                return webRequestTracer;
            }
        }

        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public void end() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "end()");
        }

        synchronized (lockObject) {
            // check if end() was already called before by looking at endTime
            if (isSessionEnded()) {
                return;
            }

            endTime = beacon.getCurrentTimestamp();
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

        // create end session data on beacon
        beacon.endSession(this);

        // finish session and stop managing it
        beaconSender.finishSession(this);

        // last but not least update parent relation
        parent.onChildClosed(this);
        parent = null;
    }

    // *** public methods ***

    // sends the current Beacon state
    public StatusResponse sendBeacon(HTTPClientProvider clientProvider) {
        return beacon.send(clientProvider);
    }

    // *** getter methods ***

    public long getEndTime() {
        return endTime;
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
     * Test if the session has already been ended.
     *
     * <p>
     * A session is considered as ended, if the endTime is set to something other than minus 1.
     * </p>
     *
     * @return {@code true} if the session has been ended already, {@code false} if the session is not ended yet.
     */
    boolean isSessionEnded() {
        return getEndTime() != -1L;
    }

    /**
     * Set the {@link BeaconConfiguration}
     */
    public void setBeaconConfiguration(BeaconConfiguration beaconConfiguration) {
        beacon.setBeaconConfiguration(beaconConfiguration);
    }

    /**
     * Get the {@link BeaconConfiguration}
     */
    public BeaconConfiguration getBeaconConfiguration() {
        return beacon.getBeaconConfiguration();
    }

    @Override
    void onChildClosed(OpenKitObject childObject) {
        synchronized (lockObject) {
            removeChildFromList(childObject);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + "] ";
    }
}
