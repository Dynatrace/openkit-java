/**
 * Copyright 2018-2021 Dynatrace LLC
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

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

/**
 * Abstract base class implementing the {@link Action} interface.
 */
public abstract class BaseActionImpl extends OpenKitComposite implements Action {

    /** {@link Logger} for tracing log message */
    final Logger logger;

    /** Parent object of this {@link Action} */
    private OpenKitComposite parent;
    /** The parent action id */
    final int parentActionID;

    /** object for synchronization, internal for derived classes within this package */
    final Object lockObject = new Object();

    /** Unique identifier of this {@link Action} */
    final int id;
    /** Name of this {@link Action} */
    final String name;

    /** start time when this {@link Action} has been started */
    private final long startTime;
    /** end time when this {@link Action} has been ended */
    private long endTime = -1;
    /** Start sequence number of this {@link Action} */
    private final int startSequenceNo;
    /** End sequence number of this {@link Action} */
    private int endSequenceNo = -1;

    /** boolean indicating whether this action has been left or not */
    private boolean isActionLeft;

    /** Beacon for sending data */
    final Beacon beacon;

    /**
     * Constructor for constructing the base action class.
     *
     * @param logger The logger used to log information
     * @param parent The parent object, to which this web action belongs to
     * @param name The action's name
     * @param beacon The beacon for retrieving certain data and sending data
     */
    BaseActionImpl(Logger logger,
                   OpenKitComposite parent,
                   String name,
                   Beacon beacon) {
        this.logger = logger;
        this.parent = parent;
        parentActionID = parent.getActionID();


        id = beacon.createID();
        this.name = name;

        startTime = beacon.getCurrentTimestamp();
        startSequenceNo = beacon.createSequenceNumber();

        isActionLeft = false;

        this.beacon = beacon;
    }

    @Override
    public void close() {
        leaveAction();
    }

    @Override
    public Action reportEvent(String eventName) {
        if (eventName == null || eventName.isEmpty()) {
            logger.warning(this + "reportEvent: eventName must not be null or empty");
            return this;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportEvent(" + eventName + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                beacon.reportEvent(getID(), eventName);
            }
        }
        return this;
    }

    @Override
    public Action reportValue(String valueName, int value) {
        if (valueName == null || valueName.isEmpty()) {
            logger.warning(this + "reportValue (int): valueName must not be null or empty");
            return this;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportValue (int) (" + valueName + ", " + value + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                beacon.reportValue(getID(), valueName, value);
            }
        }
        return this;
    }

    @Override
    public Action reportValue(String valueName, double value) {
        if (valueName == null || valueName.isEmpty()) {
            logger.warning(this + "reportValue (double): valueName must not be null or empty");
            return this;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportValue (double) (" + valueName + ", " + value + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                beacon.reportValue(getID(), valueName, value);
            }
        }
        return this;
    }

    @Override
    public Action reportValue(String valueName, String value) {
        if (valueName == null || valueName.isEmpty()) {
            logger.warning(this + "reportValue (String): valueName must not be null or empty");
            return this;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportValue (String) (" + valueName + ", " + value + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                beacon.reportValue(getID(), valueName, value);
            }
        }
        return this;
    }

    @Override
    public Action reportError(String errorName, int errorCode, String reason) {
        if (errorName == null || errorName.isEmpty()) {
            logger.warning(this + "reportError: errorName must not be null or empty");
            return this;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + "reportError(" + errorName + ", " + errorCode + ", " + reason + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                beacon.reportError(getID(), errorName, errorCode, reason);
            }
        }
        return this;
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
        synchronized (lockObject) {
            if (!isActionLeft()) {
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
            logger.debug(this + "traceWebRequest(" + url + ")");
        }
        synchronized (lockObject) {
            if (!isActionLeft()) {
                WebRequestTracerBaseImpl webRequestTracer = new WebRequestTracerStringURL(logger, this, beacon, url);
                storeChildInList(webRequestTracer);

                return webRequestTracer;
            }
        }

        return NullWebRequestTracer.INSTANCE;
    }

    @Override
    public Action leaveAction() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "leaveAction(" + name + ")");
        }
        synchronized (lockObject) {
            if (isActionLeft()) {
                // leaveAction has been called previously
                return getParentAction();
            }
            isActionLeft = true;
        }

        // close all child object
        // Note: at this point it's save to do any further operations outside a synchronized block
        // after the endTime has been set, no further child objects must be added
        List<OpenKitObject> childObjects = getCopyOfChildObjects();
        for (OpenKitObject childObject : childObjects) {
            try {
                childObject.close();
            } catch (IOException e) {
                // should not happen, nevertheless let's log an error
                logger.error(this + "Caught IOException while closing OpenKitObject (" + childObject + ")", e);
            }
        }

        // set end time and end sequence number
        endTime = beacon.getCurrentTimestamp();
        endSequenceNo = beacon.createSequenceNumber();

        // serialize this action after setting all remaining information
        beacon.addAction(this);

        // detach from parent
        parent.onChildClosed(this);
        parent = null;

        return getParentAction();
    }

    /**
     * Get the parent {@link} Action, which might be {@code null} in case the parent does not implement {@link Action}.
     *
     * @return The parent action object, or {@code null} if parent does not implement {@link Action}.
     */
    protected abstract  Action getParentAction();

    @Override
    void onChildClosed(OpenKitObject childObject) {
        synchronized (lockObject) {
            removeChildFromList(childObject);
        }
    }

    @Override
    public int getActionID() {
        return getID();
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentID() {
        return parentActionID;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getStartSequenceNo() {
        return startSequenceNo;
    }

    public int getEndSequenceNo() {
        return endSequenceNo;
    }

    boolean isActionLeft() {
        return isActionLeft;
    }
}
