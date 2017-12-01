/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.WebRequestTracer;

/**
 * Dummy implementation of the {@link WebRequestTracer} interface, used when capture is off.
 */
public class DummyWebRequestTracer implements WebRequestTracer {

	@Override
	public String getTag() {
		// return empty string
		return "";
	}

	@Override
	public void setResponseCode(int responseCode) {
		// do nothing
	}

	@Override
	public void start() {
		// do nothing
	}

	@Override
	public void stop() {
		// do nothing
	}

}
