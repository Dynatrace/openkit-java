/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import java.net.URLConnection;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.WebRequestTag;

/**
 * Dummy implementation of the {@link Action} interface, used when capture is off.
 */
public class DummyAction implements RootAction {

	private static DummyWebRequestTag dummyWebRequestTagInstance = new DummyWebRequestTag();

	@Override
	public Action enterAction(String actionName) {
		// do nothing
		return this;
	}

	@Override
	public Action reportEvent(String eventName) {
		// do nothing
		return this;
	}

	@Override
	public Action reportValue(String valueName, int value) {
		// do nothing
		return this;
	}

	@Override
	public Action reportValue(String valueName, double value) {
		// do nothing
		return this;
	}

	@Override
	public Action reportValue(String valueName, String value) {
		// do nothing
		return this;
	}

	@Override
	public Action reportError(String errorName, int errorCode, String reason) {
		// do nothing
		return this;
	}

	@Override
	public WebRequestTag tagWebRequest(URLConnection connection) {
		// return DummyWebRequestTag and do nothing
		return dummyWebRequestTagInstance;
	}

	@Override
	public WebRequestTag tagWebRequest(String url) {
		// return DummyWebRequestTag and do nothing
		return dummyWebRequestTagInstance;
	}

	@Override
	public Action leaveAction() {
		// do nothing
		return this;
	}

}
