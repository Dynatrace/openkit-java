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
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Abstract base class implementation of the {@link WebRequestTracer} interface.
 *
 * <p>
 *     This class is guaranteed to be thread safe.
 * </p>
 */
public abstract class WebRequestTracerBaseImpl implements WebRequestTracer, OpenKitObject {

    static final String UNKNOWN_URL =  "<unknown>";

    /** {@link Logger} for tracing log message */
    private final Logger logger;

    /** Parent object of this web request tracer */
    private OpenKitComposite parent;

    /** object for synchronization */
    private final Object lockObject = new Object();

    /** Dynatrace tag that has to be used for tracing the web request */
    private final String tag;

    /** Beacon for sending data */
    private final Beacon beacon;
    /** The parent action id */
    private final int parentActionID;

    /** URL to trace (excluding query args) */
    private final String url;
    /** The response code received from the request */
    private int responseCode = -1;
    /** The number of bytes sent */
    private int bytesSent = -1;
    /** The number of bytes received */
    private int bytesReceived = -1;

    /** Start time of the web request, set in {@link #start()} */
    private long startTime;
    /** End time of the web request, set in {@link #stop()} */
    private long endTime = - 1;
    /** starting sequence number */
    private final int startSequenceNo;
    /** ending sequence number */
    private int endSequenceNo = -1;

    /**
     * Constructor.
     *
     * @param logger The logger used to log information
     * @param parent The parent object, to which this web request tracer belongs to
     * @param url The URL to trace
     * @param beacon {@link Beacon} for data sending and tag creation
     */
    WebRequestTracerBaseImpl(Logger logger,
                             OpenKitComposite parent,
                             String url,
                             Beacon beacon) {
        this.logger = logger;
        this.parent = parent;
        this.url = url;
        this.beacon = beacon;
        parentActionID = parent.getActionID();

        // creating start sequence number has to be done here, because it's needed for the creation of the tag
        startSequenceNo = beacon.createSequenceNumber();

        tag = beacon.createTag(parentActionID, startSequenceNo);

        // if start is not called before using the setters the start time (e.g. load time) is not in 1970
        startTime = beacon.getCurrentTimestamp();
    }

    @Override
    public String getTag() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "getTag() returning '" + tag + "'");
        }
        return tag;
    }

    /**
     * @deprecated see {@link WebRequestTracer#setResponseCode(int)}
     */
    @Deprecated
    @Override
    public WebRequestTracer setResponseCode(int responseCode) {
        synchronized (lockObject) {
            if (!isStopped()) {
                this.responseCode = responseCode;
            }
        }
        return this;
    }

    @Override
    public WebRequestTracer setBytesSent(int bytesSent) {
        synchronized (lockObject) {
            if (!isStopped()) {
                this.bytesSent = bytesSent;
            }
        }
        return this;
    }

    @Override
    public WebRequestTracer setBytesReceived(int bytesReceived) {
        synchronized (lockObject) {
            if (!isStopped()) {
                this.bytesReceived = bytesReceived;
            }
        }
        return this;
    }

    @Override
    public WebRequestTracer start() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "start()");
        }
        synchronized (lockObject) {
            if (!isStopped()) {
                startTime = beacon.getCurrentTimestamp();
            }
        }
        return this;
    }

    @Override
    public void stop(int responseCode) {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "stop(rc='" + responseCode + "')");
        }
        synchronized (lockObject) {
            if (isStopped()) {
                // stop has been called previously
                return;
            }
            this.responseCode = responseCode;
            endSequenceNo = beacon.createSequenceNumber();
            endTime = beacon.getCurrentTimestamp();
        }

        // add web request to beacon
        beacon.addWebRequest(parentActionID, this);

        // last but not least notify the parent & detach from parent
        parent.onChildClosed(this);
        parent = null;
    }

    /**
     * @deprecated see {@link WebRequestTracer#stop()}
     */
    @Deprecated
    @Override
    public void stop() {
        stop(responseCode);
    }

    @Override
    public void close() {
        stop(responseCode);
    }

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
        return endTime;
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

    OpenKitComposite getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sn=" + beacon.getSessionNumber() + ", id=" + parentActionID + ", url='" + url + "'] ";
    }
}
