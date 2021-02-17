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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class implementing the {@link HttpRequest} and forwarding calls to {@link HttpURLConnection} adaptee.
 */
public class HttpRequestHttpURLConnectionAdapter implements HttpRequest {

    private static final Set<String> RESTRICTED_REQUEST_HEADERS = new HashSet<String>();
    static  {
        // put all restricted header names in lower case, as HTTP headers are case insensitive
        // header names have been taken from sun.net.www.protocol.http.HttpURLConnection
        RESTRICTED_REQUEST_HEADERS.add("access-control-request-headers");
        RESTRICTED_REQUEST_HEADERS.add("access-control-request-method");
        RESTRICTED_REQUEST_HEADERS.add("connection");
        RESTRICTED_REQUEST_HEADERS.add("content-length");
        RESTRICTED_REQUEST_HEADERS.add("content-transfer-encoding");
        RESTRICTED_REQUEST_HEADERS.add("host");
        RESTRICTED_REQUEST_HEADERS.add("keep-alive");
        RESTRICTED_REQUEST_HEADERS.add("origin");
        RESTRICTED_REQUEST_HEADERS.add("trailer");
        RESTRICTED_REQUEST_HEADERS.add("transfer-encoding");
        RESTRICTED_REQUEST_HEADERS.add("upgrade");
        RESTRICTED_REQUEST_HEADERS.add("via");
    }

    /**
     * Adaptee of this class.
     */
    private final HttpURLConnection httpURLConnection;

    /**
     * Initializes a new instance of {@link HttpRequestHttpURLConnectionAdapter}
     * @param httpURLConnection underlying {@link HttpURLConnection}
     */
    public HttpRequestHttpURLConnectionAdapter(HttpURLConnection httpURLConnection) {
        this.httpURLConnection = httpURLConnection;
    }

    @Override
    public URL getUrl() {
        return httpURLConnection.getURL();
    }

    @Override
    public String getMethod() {
        return httpURLConnection.getRequestMethod();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return httpURLConnection.getRequestProperties();
    }

    @Override
    public String getHeader(String name) {
        return httpURLConnection.getRequestProperty(name);
    }

    @Override
    public void setHeader(String name, String value) {
        if (isInvalidRequestHeaderName(name)) {
            return;
        }

        httpURLConnection.setRequestProperty(name, value);
    }

    private boolean isInvalidRequestHeaderName(String name) {
        if (name == null) {
            return true;
        }

        return RESTRICTED_REQUEST_HEADERS.contains(name.toLowerCase());
    }
}
