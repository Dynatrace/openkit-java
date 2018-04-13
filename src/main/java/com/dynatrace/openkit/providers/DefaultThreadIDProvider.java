/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.providers;

/**
 * Simple ThreadIDProvider implementation for getting the current thread ID.
 */
public class DefaultThreadIDProvider implements ThreadIDProvider {

    @Override
    public int getThreadID() {
        long threadID64 = Thread.currentThread().getId();
        return convertNativeThreadIDToPositiveInteger(threadID64);
    }

    /**
     * Converts a native thread id to a positive integer Thread.currentThread().getId()
     *
     * <p>
     *      Thread.currentThread().getId() returns a long value.
     *      The Beacon protocol requires the thread id to be a positive integer value. By using the xor operation
     *      between higher and lower 32 bits of the long value we get an integer value. The returned integer
     *      can be negative though.
     *      Therefore  the most significant bit is forced to '0' by a bitwise-and operation with an integer
     *      where all bits except for the most significant bit are set to '1'.
     * </p>
     *
     * @param nativeThreadID the native thread id returned by
     * @return a positive integer value calculated from the native thread id
     */
    public static int convertNativeThreadIDToPositiveInteger(long nativeThreadID) {
        return (int)((nativeThreadID ^ (nativeThreadID >>> 32)) & 0x7fffffff );
    }
}
