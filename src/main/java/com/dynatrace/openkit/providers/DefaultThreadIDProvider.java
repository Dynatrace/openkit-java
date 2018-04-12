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
        int hash = (int)((threadID64 ^ (threadID64 >>> 32)) & 0x7fffffff );

        return hash;
    }

}
