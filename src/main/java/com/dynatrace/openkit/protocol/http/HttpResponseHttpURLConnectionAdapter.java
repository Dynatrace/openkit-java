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

import com.dynatrace.openkit.api.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpResponseHttpURLConnectionAdapter implements HttpResponse {

    private final HttpURLConnection httpURLConnection;

    public HttpResponseHttpURLConnectionAdapter(HttpURLConnection httpURLConnection) {
        this.httpURLConnection = httpURLConnection;
    }

    @Override
    public URL getRequestUrl() {
        return httpURLConnection.getURL();
    }

    @Override
    public String getRequestMethod() {
        return httpURLConnection.getRequestMethod();
    }

    @Override
    public int getResponseCode() {
        try {
            return httpURLConnection.getResponseCode();
        } catch (IOException e) {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public String getResponseMessage() {
        try {
            return httpURLConnection.getResponseMessage();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return httpURLConnection.getHeaderFields();
    }

    @Override
    public String getHeader(String name) {
        return httpURLConnection.getHeaderField(name);
    }
}
