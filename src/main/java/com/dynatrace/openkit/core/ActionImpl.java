/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;

import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Actual implementation of the {@link Action} interface.
 */
public class ActionImpl implements Action {

    private static final WebRequestTracer NULL_WEB_REQUEST_TRACER = new NullWebRequestTracer();

    // logger
    protected final Logger logger;

    // Action ID, name and parent ID (default: null)
    private final int id;
    private final String name;

    private final ActionImpl parentAction;

    // start/end time & sequence number
    private final long startTime;
    private final AtomicLong endTime = new AtomicLong(-1);
    private final int startSequenceNo;
    private int endSequenceNo = -1;

    // Beacon reference
    private final Beacon beacon;

    private final SynchronizedQueue<Action> thisLevelActions;

    // *** constructors ***

    ActionImpl(Logger logger, Beacon beacon, String name, SynchronizedQueue<Action> thisLevelActions) {
        this(logger, beacon, name, null, thisLevelActions);
    }

    ActionImpl(Logger logger, Beacon beacon, String name, ActionImpl parentAction, SynchronizedQueue<Action> thisLevelActions) {
        this.logger = logger;

        this.beacon = beacon;
        this.parentAction = parentAction;

        this.startTime = beacon.getCurrentTimestamp();
        this.startSequenceNo = beacon.createSequenceNumber();
        this.id = beacon.createID();
        this.name = name;

        this.thisLevelActions = thisLevelActions;
        this.thisLevelActions.put(this);
    }

    // *** Action interface methods ***


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
        if (!isActionLeft()) {
            beacon.reportEvent(this, eventName);
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
        if (!isActionLeft()) {
            beacon.reportValue(this, valueName, value);
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
        if (!isActionLeft()) {
            beacon.reportValue(this, valueName, value);
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
        if (!isActionLeft()) {
            beacon.reportValue(this, valueName, value);
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
        if (!isActionLeft()) {
            beacon.reportError(this, errorName, errorCode, reason);
        }
        return this;
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
        if (!isActionLeft()) {
            return new WebRequestTracerURLConnection(logger, beacon, getID(), connection);
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
        if (!isActionLeft()) {
            return new WebRequestTracerStringURL(logger, beacon, getID(), url);
        }

        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public Action leaveAction() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "leaveAction(" + name + ")");
        }
        // check if leaveAction() was already called before by looking at endTime
        if (!endTime.compareAndSet(-1, beacon.getCurrentTimestamp())) {
            return parentAction;
        }

        return doLeaveAction();
    }

    protected Action doLeaveAction() {
        // set end time and end sequence number
        endTime.set(beacon.getCurrentTimestamp());
        endSequenceNo = beacon.createSequenceNumber();

        // add Action to Beacon
        beacon.addAction(this);

        // remove Action from the Actions on this level
        thisLevelActions.remove(this);

        return parentAction;            // can be null if there's no parent Action!
    }

    // *** getter methods ***

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getParentID() {
        return parentAction == null ? 0 : parentAction.getID();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime.get();
    }

    public int getStartSequenceNo() {
        return startSequenceNo;
    }

    public int getEndSequenceNo() {
        return endSequenceNo;
    }

    boolean isActionLeft() {
        return getEndTime() != -1;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + ", id=" + id + ", name=" + name + ", pa="
                + (parentAction != null ? parentAction.id : "no parent") + "] ";
    }
}
