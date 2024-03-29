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

package com.dynatrace.openkit.api;

import java.io.Closeable;
import java.net.URLConnection;
import java.util.Map;

import com.dynatrace.openkit.util.json.objects.JSONValue;

/**
 * This interface provides functionality to create Actions in a Session.
 */
public interface Session extends Closeable {

    /**
     * Enters an Action with a specified name in this Session.
     *
     * <p>
     *     If the given {@code actionName} is {@code null} or an empty string,
     *     no reporting will happen on that {@link RootAction}.
     * </p>
     *
     * @param actionName name of the Action
     * @return Action instance to work with
     */
    RootAction enterAction(String actionName);

    /**
     * Tags a session with the provided {@code userTag}.
     *
     * <p>
     *     If the given {@code userTag} is {@code null} or an empty string,
     *     this is equivalent to logging off the user.
     * </p>
     * <p>
     *     The last non-empty {@code userTag} is re-applied to split sessions.
     *     Details are described in
     *     https://github.com/Dynatrace/openkit-java/blob/main/docs/internals.md#identify-users-on-split-sessions.
     * </p>
     *
     * @param userTag id of the user or {@code null}/{@code ""} to simulate a log off.
     */
    void identifyUser(String userTag);

    /**
     * Reports a crash with a specified error name, crash reason and a stacktrace.
     *
     * <p>
     *     If the given {@code errorName} is {@code null} or an empty string,
     *     no crash report will be sent to the server.
     *     If the {@code reason} is longer than 1000 characters,
     *     it is truncated to this value.
     *     If the {@code stacktrace} is longer than 128.000 characters,
     *     it is truncated according to the last line break.
     * </p>
     *
     * @param errorName  name of the error leading to the crash (e.g. Exception class)
     * @param reason     reason or description of that error
     * @param stacktrace stacktrace leading to that crash
     */
    void reportCrash(String errorName, String reason, String stacktrace);

    /**
     * Reports a crash with error name, crash reason and stacktrace determined from given {@link Throwable}.
     *
     * <p>
     *     This method is offered as convenience method for {@link #reportCrash(Throwable)}.
     * </p>
     *
     * @param throwable The {@link Throwable} causing the crash.
     */
    void reportCrash(Throwable throwable);

    /**
     * Reports the network technology in use (e.g. 2G, 3G, 802.11x, offline, ...)
     * Use {@code null} to clear the value again and it will no longer be sent with the next beacon.
     *
     * @param technology the used network technology
     */
    void reportNetworkTechnology(String technology);

    /**
     * Reports the type of connection with which the device is connected to the network.
     * Use {@code null} to clear the value again and it will no longer be sent with the next beacon.
     *
     * @param connectionType the type of connection
     */
    void reportConnectionType(ConnectionType connectionType);

    /**
     * Reports the name of the cellular network carrier.
     * Use {@code null} to clear the value again and it will no longer be sent with the next beacon.
     *
     * <p>
     * The given value will be truncated to 250 characters.
     * </p>
     *
     * @param carrier the cellular network carrier
     */
    void reportCarrier(String carrier);

    /**
     * Traces a web request - which is provided as a URLConnection - and allows adding timing information to this request.
     * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
     * the resulting server-side PurePath.
     *
     * @param connection the URLConnection of the HTTP request to be tagged and timed
     * @return a WebRequestTracer which allows adding timing information
     */
    WebRequestTracer traceWebRequest(URLConnection connection);

    /**
     * Allows tracing and timing of a web request handled by any 3rd party HTTP Client (e.g. Apache, Google, Jetty, ...).
     * In this case the Dynatrace HTTP header ({@link OpenKitConstants#WEBREQUEST_TAG_HEADER}) has to be set manually to the
     * tag value of this WebRequestTracer. <br>
     * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
     * the resulting server-side PurePath.
     *
     * @param url the URL of the web request to be tagged and timed
     * @return a WebRequestTracer which allows getting the tag value and adding timing information
     */
    WebRequestTracer traceWebRequest(String url);

    /**
     * Send a Business Event
     * 
     * With sendBizEvent, you can report a business event. These standalone events are being sent detached
     * from user actions or sessions.
     * 
     * Note: The 'dt' key, as well as all 'dt.' prefixed keys are considered reserved by Dynatrace
     * and will be stripped from the passed in attributes.
     *
     * Note: Business events are only supported on Dynatrace SaaS deployments currently.
     *
     * @param type Mandatory event type
     * @param attributes Must be a valid JSON object. The resulting event will be populated with the 'attributes'-parameter 
     * and enriched with additional properties. Therefore, even empty objects are valid.
     */
    void sendBizEvent(String type, Map<String, JSONValue> attributes);

    /**
     * Ends this Session and marks it as ready for immediate sending.
     */
    void end();
}
