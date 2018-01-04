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

import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.Beacon;

/**
 * Abstract base class implementation of the {@link WebRequestTracer} interface.
 */
public abstract class WebRequestTracerBaseImpl implements WebRequestTracer {

    // Dynatrace tag that has to be used for tracing the web request
    private String tag;

    // HTTP information: URL & response code
    protected String url = "<unknown>";
    private int responseCode = -1;
    private int bytesSent = -1;
    private int bytesReceived = -1;

    // start/end time & sequence number
    private long startTime = -1;
    private long endTime = -1;
    private int startSequenceNo = -1;
    private int endSequenceNo = -1;

    // Beacon and Action references
    private Beacon beacon;
    private ActionImpl action;

    // *** constructors ***

    public WebRequestTracerBaseImpl(Beacon beacon, ActionImpl action) {
        this.beacon = beacon;
        this.action = action;

        // creating start sequence number has to be done here, because it's needed for the creation of the tag
        startSequenceNo = beacon.createSequenceNumber();

        tag = beacon.createTag(action, startSequenceNo);
    }

    // *** WebRequestTracer interface methods ***

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public WebRequestTracer setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    @Override
    public WebRequestTracer setBytesSent(int bytesSent) {
        this.bytesSent = bytesSent;
        return this;
    }

    @Override
    public WebRequestTracer setBytesReceived(int bytesReceived) {
        this.bytesReceived = bytesReceived;
        return this;
    }

    @Override
    public WebRequestTracer start() {
        startTime = beacon.getCurrentTimestamp();
        return this;
    }

    @Override
    public void stop() {
        endTime = beacon.getCurrentTimestamp();
        endSequenceNo = beacon.createSequenceNumber();

        // add web request to beacon
        beacon.addWebRequest(action, this);
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

}
