/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

import java.net.URLConnection;

/**
 * This interface provides functionality to create (child) Actions, report events/values/errors and tracing web requests.
 */
public interface Action {

	/**
	 * Reports an event with a specified name (but without any value).
	 *
	 * @param eventName		name of the event
	 * @return				this Action (for usage as fluent API)
	 */
	Action reportEvent(String eventName);

	/**
	 * Reports an int value with a specified name.
	 *
	 * @param valueName		name of this value
	 * @param value			value itself
	 * @return				this Action (for usage as fluent API)
	 */
	Action reportValue(String valueName, int value);

	/**
	 * Reports a double value with a specified name.
	 *
	 * @param valueName		name of this value
	 * @param value			value itself
	 * @return				this Action (for usage as fluent API)
	 */
	Action reportValue(String valueName, double value);

	/**
	 * Reports a String value with a specified name.
	 *
	 * @param valueName		name of this value
	 * @param value			value itself
	 * @return				this Action (for usage as fluent API)
	 */
	Action reportValue(String valueName, String value);

	/**
	 * Reports an error with a specified name, error code and reason.
	 *
	 * @param errorName			name of this error
	 * @param errorCode			numeric error code of this error
	 * @param reason			reason for this error
	 * @return					this Action (for usage as fluent API)
	 */
	Action reportError(String errorName, int errorCode, String reason);

	/**
	 * Traces a web request - which is provided as a URLConnection - and allows adding timing information to this request.
	 * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
	 * the resulting server-side PurePath.
	 *
	 * @param connection	the URLConnection of the HTTP request to be tagged and timed
	 * @return				a WebRequestTracer which allows adding timing information
	 */
	WebRequestTracer traceWebRequest(URLConnection connection);

	/**
	 * Allows tracing and timing of a web request handled by any 3rd party HTTP Client (e.g. Apache, Google, Jetty, ...).
	 * In this case the Dynatrace HTTP header ({@link OpenKit#WEBREQUEST_TAG_HEADER}) has to be set manually to the
	 * tag value of this WebRequestTracer. <br>
	 * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
	 * the resulting server-side PurePath.
	 *
	 * @param url		the URL of the web request to be tagged and timed
	 * @return			a WebRequestTracer which allows getting the tag value and adding timing information
	 */
	WebRequestTracer traceWebRequest(String url);

	/**
	 * Leaves this Action.
	 *
	 * @return	the parent Action
	 */
	Action leaveAction();

}
