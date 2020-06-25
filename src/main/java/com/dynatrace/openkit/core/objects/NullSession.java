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

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;

import java.net.URLConnection;

/**
 * This {@link Session} implementation is returned by {@link OpenKit#createSession(String)} when the {@link OpenKit#shutdown()}
 * has been called before.
 */
public enum NullSession implements Session {

    /**
     * The sole {@link NullSession} instance
     */
    INSTANCE;

    @Override
    public RootAction enterAction(String actionName) {
        return NullRootAction.INSTANCE;
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
        return NullWebRequestTracer.INSTANCE;
    }

    @Override
    public WebRequestTracer traceWebRequest(String url) {
        return NullWebRequestTracer.INSTANCE;
    }

    @Override
    public void end() {
        // intentionally left empty, due to NullObject pattern
    }

    @Override
    public void close() {
        // nothing
    }
}
