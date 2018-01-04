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

public class DefaultTimingProvider implements TimingProvider {

    private long lastInitTime = 0;
    private long clusterTimeOffset = 0;
    private boolean isTimeSyncSupported = true;


    @Override
    public long provideTimestampInMilliseconds() {
        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long milliseconds) throws InterruptedException {

        Thread.sleep(milliseconds);
    }

    @Override
    public synchronized void initialize(long clusterTimeOffset, boolean isTimeSyncSupported) {
        // set init time in milliseconds since 1970-01-01
        lastInitTime = provideTimestampInMilliseconds();
        this.isTimeSyncSupported = isTimeSyncSupported;
        if (isTimeSyncSupported) {
            this.clusterTimeOffset = clusterTimeOffset;
        } else {
            this.clusterTimeOffset = 0;
        }
    }

    @Override
    public synchronized boolean isTimeSyncSupported() {
        return isTimeSyncSupported;
    }

    @Override
    public synchronized long convertToClusterTime(long timestamp) {
        return timestamp + clusterTimeOffset;
    }

    @Override
    public synchronized long getLastInitTimeInClusterTime() {
        return lastInitTime + clusterTimeOffset;
    }

    @Override
    public long getTimeSinceLastInitTime() {
        return getTimeSinceLastInitTime(provideTimestampInMilliseconds());
    }

    @Override
    public synchronized long getTimeSinceLastInitTime(long timestamp) {
        return timestamp - lastInitTime;
    }
}
