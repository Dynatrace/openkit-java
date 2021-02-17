/**
 *   Copyright 2018-2021 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dynatrace.openkit.protocol.http;

import com.dynatrace.openkit.api.http.HttpRequest;
import com.dynatrace.openkit.api.http.HttpRequestInterceptor;

public class NullHttpRequestInterceptor implements HttpRequestInterceptor {

    /**
     * Sole instance of this class.
     */
    public static final NullHttpRequestInterceptor INSTANCE = new NullHttpRequestInterceptor();

    /**
     * Use {@link #INSTANCE} to retrieve the sole instance of this class.
     */
    private NullHttpRequestInterceptor() {
    }

    @Override
    public void intercept(HttpRequest httpRequest) {
        // intentionally empty, due to null object pattern
    }
}
