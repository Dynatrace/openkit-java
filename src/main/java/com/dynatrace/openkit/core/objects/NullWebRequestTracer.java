/**
 * Copyright 2018-2021 Dynatrace LLC
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
import com.dynatrace.openkit.api.WebRequestTracer;

/**
 * This implementation of {@link WebRequestTracer} is returned by {@link Action#traceWebRequest(String)} or
 * {@link Action#traceWebRequest(java.net.URLConnection)} when the {@link Action#leaveAction()} ()}
 * has been called before.
 */
public enum NullWebRequestTracer implements WebRequestTracer {

    /**
     * The sole {@link NullWebRequestTracer} instance
     */
    INSTANCE;

    @Override
    public String getTag() {
        return "";
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
    public void stop(int respondeCode) {
        // nothing, NullObject pattern
    }

    @Override
    public void close() {
        // nothing, NullObject pattern
    }
}
