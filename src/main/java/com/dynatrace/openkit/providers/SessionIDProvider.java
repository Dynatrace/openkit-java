package com.dynatrace.openkit.providers;

/**
 * Interface providing consecutive numbers starting at a random offset
 */
public interface SessionIDProvider {

    /**
     * Provide the next sessionID
     * @returns the id that will be used for the next session
     */
    int getNextSessionID();

}
