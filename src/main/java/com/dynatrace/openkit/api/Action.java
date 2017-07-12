/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

import java.net.URLConnection;

/**
 * This interface provides functionality to create (child) Actions, report events/values/errors and tag web requests.
 */
public interface Action {

	/**
	 * Enters a (child) Action with a specified name on this Action.
	 *
	 * @param actionName	name of the Action
	 * @return				Action instance to work with
	 */
	public Action enterAction(String actionName);

	/**
	 * Reports an event with a specified name (but without any value).
	 *
	 * @param eventName		name of the event
	 * @return				this Action (for usage as fluent API)
	 */
	public Action reportEvent(String eventName);

	/**
	 * Reports an int value with a specified name.
	 *
	 * @param valueName		name of this value
	 * @param value			value itself
	 * @return				this Action (for usage as fluent API)
	 */
	public Action reportValue(String valueName, int value);

	/**
	 * Reports a double value with a specified name.
	 *
	 * @param valueName		name of this value
	 * @param value			value itself
	 * @return				this Action (for usage as fluent API)
	 */
	public Action reportValue(String valueName, double value);

	/**
	 * Reports a String value with a specified name.
	 *
	 * @param valueName		name of this value
	 * @param value			value itself
	 * @return				this Action (for usage as fluent API)
	 */
	public Action reportValue(String valueName, String value);

	/**
	 * Reports an Error with a specified name, error code, reason and detailed description.
	 *
	 * @param errorName			name of this error
	 * @param errorCode			numeric error code of this error
	 * @param reason			reason for this error
	 * @return					this Action (for usage as fluent API)
	 */
	public Action reportError(String errorName, int errorCode, String reason);

	/**
	 * Tags a web request - which is provided as a URLConnection - and allows adding timing information to this request.
	 * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
	 * the resulting server-side PurePath.
	 *
	 * @param connection	the URLConnection of the HTTP request to be tagged and timed
	 * @return				a WebRequestTag which allows adding timing information
	 */
	public WebRequestTag tagWebRequest(URLConnection connection);

	/**
	 * Allows tagging and timing of a web request handled by any 3rd party HTTP Client (e.g. Apache, Google, Jetty, ...).
	 * In this case the Dynatrace HTTP header ({@link OpenKit#WEBREQUEST_TAG_HEADER}) has to be set manually to the
	 * tag value of this WebRequestTag. <br>
	 * If the web request is continued on a server-side Agent (e.g. Java, .NET, ...) this Session will be correlated to
	 * the resulting server-side PurePath.
	 *
	 * @param url		the URL of the web request to be tagged and timed
	 * @return			a WebRequestTag which allows getting the tag value and adding timing information
	 */
	public WebRequestTag tagWebRequest(String url);

	/**
	 * Leaves this Action.
	 *
	 * @return	this Action (for usage as fluent API)
	 */
	public Action leaveAction();

}
