/**
 * Copyright 2018-2020 Dynatrace LLC
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

    /**
     * Factor used to convert between milliseconds and nanoseconds.
     */
    static final long MILLIS_TO_NANOS_FACTOR = 1000L * 1000L;

    /**
     * Reference timestamp in nanoseconds.
     */
    private final long referenceTimestampNanos;

    public DefaultTimingProvider() {
        referenceTimestampNanos = (System.currentTimeMillis() * MILLIS_TO_NANOS_FACTOR) - System.nanoTime();
    }

    @Override
    public long provideTimestampInMilliseconds() {
        return (referenceTimestampNanos + System.nanoTime()) / MILLIS_TO_NANOS_FACTOR;
    }

    @Override
    public void sleep(long milliseconds) throws InterruptedException {

        Thread.sleep(milliseconds);
    }
}
