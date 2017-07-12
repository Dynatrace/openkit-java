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
	public Action enterAction(String actionName);

	/**
	 * Ends this Session and marks them as finished for sending.
	 */
	public void end();

}
