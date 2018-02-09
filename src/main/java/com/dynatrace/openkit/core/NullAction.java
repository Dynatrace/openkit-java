package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.WebRequestTracer;

import java.net.URLConnection;

class NullAction implements Action {

    private static final WebRequestTracer NULL_TRACER = new NullWebRequestTracer();

    private final Action parentAction;

    NullAction() {
        this(null);
    }

    NullAction(Action parentAction) {
        this.parentAction = parentAction;
    }

    @Override
    public Action reportEvent(String eventName) {
        return this;
    }

    @Override
    public Action reportValue(String valueName, int value) {
        return this;
    }

    @Override
    public Action reportValue(String valueName, double value) {
        return this;
    }

    @Override
    public Action reportValue(String valueName, String value) {
        return this;
    }

    @Override
    public Action reportError(String errorName, int errorCode, String reason) {
        return this;
    }

    @Override
    public WebRequestTracer traceWebRequest(URLConnection connection) {
        return NULL_TRACER;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        return NULL_TRACER;
    }

    @Override
    public Action leaveAction() {
        return parentAction;
    }
}
