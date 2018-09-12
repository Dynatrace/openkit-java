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

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;

import java.net.URLConnection;

/**
 * This class is returned as Session by {@link OpenKit#createSession(String)} when the {@link OpenKit#shutdown()}
 * has been called before.
 */
public class NullSession implements Session {

    private static final RootAction NULL_ROOT_ACTION = new NullRootAction();
    private static final WebRequestTracer NULL_WEB_REQUEST_TRACER = new NullWebRequestTracer();

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
    public WebRequestTracer traceWebRequest(URLConnection connection) {
        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        return NULL_WEB_REQUEST_TRACER;
    }

    @Override
    public void end() {
        // intentionally left empty, due to NullObject pattern
    }

    @Override
    public void close() {
        end();
    }
}
