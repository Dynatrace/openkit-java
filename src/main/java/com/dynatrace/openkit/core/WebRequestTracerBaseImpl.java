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

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base class implementation of the {@link WebRequestTracer} interface.
 */
public abstract class WebRequestTracerBaseImpl implements WebRequestTracer {

    protected Logger logger;

    // Dynatrace tag that has to be used for tracing the web request
    private final String tag;

    // HTTP information: URL & response code
    protected String url = "<unknown>";
    private int responseCode = -1;
    private int bytesSent = -1;
    private int bytesReceived = -1;

    // start/end time & sequence number
    private long startTime = -1;
    private final AtomicLong endTime = new AtomicLong(-1);
    private final int startSequenceNo;
    private int endSequenceNo = -1;

    // Beacon and Action references
    private final Beacon beacon;
    private final ActionImpl action;

    // *** constructors ***

    WebRequestTracerBaseImpl(Logger logger, Beacon beacon, ActionImpl action) {
        this.logger = logger;
        this.beacon = beacon;
        this.action = action;

        // creating start sequence number has to be done here, because it's needed for the creation of the tag
        startSequenceNo = beacon.createSequenceNumber();

        tag = beacon.createTag(action, startSequenceNo);

        // if start is not called before using the setters the start time (e.g. load time) is not in 1970
        startTime = beacon.getCurrentTimestamp();
    }

    // *** WebRequestTracer interface methods ***

    @Override
    public String getTag() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "getTag() returning '" + tag + "'");
        }
        return tag;
    }

    @Override
    public WebRequestTracer setResponseCode(int responseCode) {
        if (!isStopped()) {
            this.responseCode = responseCode;
        }
        return this;
    }

    @Override
    public WebRequestTracer setBytesSent(int bytesSent) {
        if (!isStopped()) {
            this.bytesSent = bytesSent;
        }
        return this;
    }

    @Override
    public WebRequestTracer setBytesReceived(int bytesReceived) {
        if (!isStopped()) {
            this.bytesReceived = bytesReceived;
        }
        return this;
    }

    @Override
    public WebRequestTracer start() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "start()");
        }
        if (!isStopped()) {
            startTime = beacon.getCurrentTimestamp();
        }
        return this;
    }

    @Override
    public void stop() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "stop()");
        }
        if (!endTime.compareAndSet(-1, beacon.getCurrentTimestamp())) {
            // stop already called
            return;
        }
        endSequenceNo = beacon.createSequenceNumber();

        // add web request to beacon
        beacon.addWebRequest(action, this);
    }

    @Override
    public void close() {
        stop();
    }

    // *** getter methods ***

    public String getURL() {
        return url;
    }

    public int getResponseCode() {
        return responseCode;
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

    public int getBytesSent() {
        return bytesSent;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    boolean isStopped() {
        return getEndTime() != -1;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + ", id=" + action.getID() + ", url='" + url + "'] ";
    }
}
