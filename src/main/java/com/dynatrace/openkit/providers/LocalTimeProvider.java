/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

/**
 * Simple TimeProvider implementation for getting a local timestamp.
 */
public class LocalTimeProvider extends TimeProvider {

	@Override
	public long provideTimestamp() {
		return System.currentTimeMillis();
	}

}
