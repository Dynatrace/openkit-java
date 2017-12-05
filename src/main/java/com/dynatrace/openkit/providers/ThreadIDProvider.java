package com.dynatrace.openkit.providers;

/**
 *  Interface that provides the thread id
 */
public interface ThreadIDProvider {
    /**
     * Return s the current thread id
     * @return
     */
    long getThreadID();
}
