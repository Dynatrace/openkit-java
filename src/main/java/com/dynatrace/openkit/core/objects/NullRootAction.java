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

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;

import java.net.URLConnection;

/**
 * This implementation of {@link RootAction} is returned by {@link Session#enterAction(String)} when the
 * {@link Session#end()}has been called before.
 */
public enum NullRootAction implements RootAction {

    /**
     * The sole {@link NullRootAction} instance
     */
    INSTANCE;

    @Override
    public Action enterAction(String actionName) {
        return new NullAction(this);
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
    public Action reportValue(String valueName, long value) {
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

    @Deprecated
    @Override
    public Action reportError(String errorName, int errorCode, String reason) {
        return this;
    }

    @Override
    public Action reportError(String errorName, int errorCode) {
        return this;
    }

    @Override
    public Action reportError(String errorName, String causeName, String causeDescription, String causeStackTrace) {
        return this;
    }

    @Override
    public Action reportError(String errorName, Throwable throwable) {
        return this;
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
    public Action leaveAction() {
        return null; // no parent action
    }

    @Override
    public void close() {
    }
}
