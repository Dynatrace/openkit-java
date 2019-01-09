/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.api;

/**
 * This interface allows tracing and timing of a web request.
 */
public interface WebRequestTracer {

    /**
     * Returns the Dynatrace tag which has to be set manually as Dynatrace HTTP header
     * ({@link OpenKitConstants#WEBREQUEST_TAG_HEADER}). <br>
     * This is only necessary for tracing web requests via 3rd party HTTP clients.
     *
     * @return the Dynatrace tag to be set as HTTP header value or an empty String if capture is off
     */
    String getTag();

    /**
     * Sets the response code of this web request. Has to be called before {@link WebRequestTracer#stop()}.
     *
     * @param responseCode response code of this web request
     */
    WebRequestTracer setResponseCode(int responseCode);

    /**
     * Sets the amount of sent data of this web request. Has to be called before {@link WebRequestTracer#stop()}.
     *
     * @param bytesSent number of bytes
     */
    WebRequestTracer setBytesSent(int bytesSent);

    /**
     * Sets the amount of received data of this web request. Has to be called before {@link WebRequestTracer#stop()}.
     *
     * @param bytesReceived number of bytes
     */
    WebRequestTracer setBytesReceived(int bytesReceived);

    /**
     * Starts the web request timing. Should be called when the web request is initiated.
     */
    WebRequestTracer start();

    /**
     * Stops the web request timing. Should be called when the web request is finished.
     */
    void stop();
}
