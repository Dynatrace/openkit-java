package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.WebRequestTracer;

public class NullWebRequestTracer implements WebRequestTracer {

    @Override
    public String getTag() {
        return "";
    }

    @Override
    public WebRequestTracer setResponseCode(int responseCode) {
        return this;
    }

    @Override
    public WebRequestTracer setBytesSent(int bytesSent) {
        return this;
    }

    @Override
    public WebRequestTracer setBytesReceived(int bytesReceived) {
        return this;
    }

    @Override
    public WebRequestTracer start() {
        return this;
    }

    @Override
    public void stop() {
        // intentionally left empty, due to NullObject pattern
    }
}
