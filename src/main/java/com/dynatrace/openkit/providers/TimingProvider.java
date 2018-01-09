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
 * Interface providing timing related functionality.
 */
public interface TimingProvider {

    /**
     * Provide the current timestamp in milliseconds.
     */
    long provideTimestampInMilliseconds();

    /**
     * Sleep given amount of milliseconds.
     *
     * @throws InterruptedException When the sleep call gets interrupted.
     */
    void sleep(long milliseconds) throws InterruptedException;

    /**
     * Initialize timing provider with cluster time offset. If {@code false} is provided
     * for {@code isTimeSyncSupported}, the cluster offset is set to 0.
     *
     * @param clusterTimeOffset
     * @param isTimeSyncSupported
     */
    void initialize(long clusterTimeOffset, boolean isTimeSyncSupported);

    /**
     * Returns whether time sync is supported or not
     *
     * @return {@code true} if time sync is supported otherwise {@code false}
     */
    boolean isTimeSyncSupported();

    /**
     * Converts a local timestamp to cluster time.
     *
     * @param timestamp Timestamp in local time
     * @return Returns local time if not time synced or if not yet initialized
     */
    long convertToClusterTime(long timestamp);
}
