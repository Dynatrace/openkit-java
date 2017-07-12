/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.providers;

/**
 * Abstract class for providing a thread ID. Mostly needed for testing purposes.
 */
public abstract class ThreadIDProvider {

	private static ThreadIDProvider threadIDProvider = new DefaultThreadIDProvider();

	protected abstract long provideThreadID();

	public static long getThreadID() {
		return threadIDProvider.provideThreadID();
	}

	// FOR TESTS ONLY: set thread provider
	public static void setThreadIDProvider(ThreadIDProvider threadIDProvider) {
		ThreadIDProvider.threadIDProvider = threadIDProvider;
	}

}
