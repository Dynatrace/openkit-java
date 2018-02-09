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
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTracer;

import java.net.URLConnection;

/**
 * This class is returned as Action by {@link RootAction#enterAction(String)} when the {@link RootAction#leaveAction()}
 * has been called before.
 */
class NullAction implements Action {

    private static final WebRequestTracer NULL_TRACER = new NullWebRequestTracer();

    private final Action parentAction;

    /**
     * Construct null action without parent.
     */
    NullAction() {
        this(null);
    }

    /**
     * Construct null action with parent action.
     * @param parentAction
     */
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
