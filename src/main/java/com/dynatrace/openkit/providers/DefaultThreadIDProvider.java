/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

/**
 * Simple ThreadIDProvider implementation for getting the current thread ID.
 */
public class DefaultThreadIDProvider extends ThreadIDProvider {

	@Override
	public long provideThreadID() {
		return Thread.currentThread().getId();
	}

}
