/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.WebRequestTag;

/**
 * Dummy implementation of the {@link WebRequestTag} interface, used when capture is off.
 */
public class DummyWebRequestTag implements WebRequestTag {

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
	public void startTiming() {
		// do nothing
	}

	@Override
	public void stopTiming() {
		// do nothing
	}

}
