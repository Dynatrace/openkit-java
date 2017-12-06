/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

/**
 * This interface provides functionality to create Actions in a Session.
 */
public interface Session {

	/**
	 * Enters an Action with a specified name in this Session.
	 *
	 * @param actionName	name of the Action
	 * @return				Action instance to work with
	 */
	RootAction enterAction(String actionName);

	/**
	 * Tags a session with the provided userId
	 *
	 * @param userId	id of the user
	 */
	void identifyUser(String userId);

	/**
	 * Reports a crash with a specified error name, crash reason and a stacktrace.
	 *
	 * @param errorName			name of the error leading to the crash (e.g. Exception class)
	 * @param reason			reason or description of that error
	 * @param stacktrace		stacktrace leading to that crash
	 */
	void reportCrash(String errorName, String reason, String stacktrace);

	/**
	 * Ends this Session and marks it as finished for sending.
	 */
	void end();
}
