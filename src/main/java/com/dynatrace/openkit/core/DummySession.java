/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;

/**
 * Dummy implementation of the {@link Session} interface, used when capture is off.
 */
public class DummySession implements Session {

	private static DummyAction dummyActionInstance = new DummyAction();

	@Override
	public RootAction enterAction(String actionName) {
		// return DummyAction and do nothing
		return dummyActionInstance;
	}

	@Override
	public void reportCrash(String errorName, String reason, String stacktrace) {
		// do nothing
	}

	@Override
	public void end() {
		// do nothing
	}

}
