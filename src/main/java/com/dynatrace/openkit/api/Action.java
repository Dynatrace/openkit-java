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

/**
 * This interface provides functionality to create (child) Actions, report events/values/errors and tracing web requests.
 */
public interface Action extends Closeable {

    /**
     * Reports an event with a specified name (but without any value).
     *
     * <p>
     *     If given {@code eventName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param eventName name of the event
     * @return this Action (for usage as fluent API)
     */
    Action reportEvent(String eventName);

    /**
     * Reports an {@code int} value with a specified name.
     *
     * <p>
     *     If given {@code valueName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param valueName name of this value
     * @param value     value itself
     * @return this Action (for usage as fluent API)
     */
    Action reportValue(String valueName, int value);

    /**
     * Reports a {@code long} value with a specified name.
     *
     * <p>
     *     If given {@code valueName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param valueName name of this value
     * @param value     value itself
     * @return this Action (for usage as fluent API)
     */
    Action reportValue(String valueName, long value);

    /**
     * Reports a {@code double} value with a specified name.
     *
     * <p>
     *     If given {@code valueName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param valueName name of this value
     * @param value     value itself
     * @return this Action (for usage as fluent API)
     */
    Action reportValue(String valueName, double value);

    /**
     * Reports a {@link String} value with a specified name.
     *
     * <p>
     *     If given {@code valueName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param valueName name of this value
     * @param value     value itself
     *                  The {@code value} can be {@code null} or an empty String.
     * @return this Action (for usage as fluent API)
     */
    Action reportValue(String valueName, String value);

    /**
     * Reports an error with a specified name, error code and reason.
     *
     * <p>
     *     If given {@code errorName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param errorName name of this error
     * @param errorCode numeric error code of this error
     * @param reason    reason for this error
     * @return this Action (for usage as fluent API)
     *
     * @deprecated with version 2.1.0 Prefer {@link #reportError(String, int)}, since {@code reason} is unhandled.
     */
    @Deprecated
    Action reportError(String errorName, int errorCode, String reason);

    /**
     * Reports an error with a specified name and error code.
     *
     * <p>
     *     If given {@code errorName} is {@code null} or an empty String then no event is reported to the system.
     * </p>
     *
     * @param errorName name of this error
     * @param errorCode numeric error code of this error
     * @return this Action (for usage as fluent API)
     */
    Action reportError(String errorName, int errorCode);

    /**
     * Reports an error with a specified name and fields describing the cause of this error.
     *
     * <p>
     *     If given {@code errorName} is {@code null} or an empty string then no event is reported to the system.
     * </p>
     *
     * @param errorName name of this error
     * @param causeName name describing the cause of the error.
     *                  E.g. the class name of a caught exception.
     * @param causeDescription description what caused the error
     *                  E.g. {@link Throwable#getMessage()} of a caught exception.
     * @param causeStackTrace stack trace of the error
     *                  E.g. the {@link Throwable} stack trace
     * @return this Action (for usage as fluent API)
     */
    Action reportError(String errorName, String causeName, String causeDescription, String causeStackTrace);

    /**
     * Reports an error with a specified name and a {@link Throwable}.
     *
     * <p>
     *     If given {@code errorName} is {@code null} or an empty string then no event is reported to the system.
     * </p>
     *
     * @param errorName name of this error
     * @param throwable {@link Throwable} causing this error
     * @return this Action (for usage as fluent API)
     */
    Action reportError(String errorName, Throwable throwable);

    /**
     * Traces a web request - which is provided as a URLConnection - and allows adding timing information to this request.
     * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
     * the resulting server-side PurePath.
     *
     * <p>
     *     If given {@code connection} is {@code null} then no event is reported to the system.
     * </p>
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
     * <p>
     *     If given {@code url} is {@code null} or an empty string then no event is reported to the system.
     * </p>
     *
     * @param url the URL of the web request to be tagged and timed
     * @return a WebRequestTracer which allows getting the tag value and adding timing information
     */
    WebRequestTracer traceWebRequest(String url);

    /**
     * Leaves this Action.
     *
     * @return the parent Action, or null if there is no parent Action
     */
    Action leaveAction();

}
