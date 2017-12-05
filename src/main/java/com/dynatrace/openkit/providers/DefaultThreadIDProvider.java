/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

/**
 * Simple ThreadIDProvider implementation for getting the current thread ID.
 */
public class DefaultThreadIDProvider implements ThreadIDProvider {

	@Override
	public long getThreadID() {
		return Thread.currentThread().getId();
	}

}
