package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;

public class NullSession implements Session {

    private static final RootAction NULL_ROOT_ACTION = new NullRootAction();

    @Override
    public RootAction enterAction(String actionName) {
        return NULL_ROOT_ACTION;
    }

    @Override
    public void identifyUser(String userTag) {
        // intentionally left empty, due to NullObject pattern
    }

    @Override
    public void reportCrash(String errorName, String reason, String stacktrace) {
        // intentionally left empty, due to NullObject pattern
    }

    @Override
    public void end() {
        // intentionally left empty, due to NullObject pattern
    }
}
