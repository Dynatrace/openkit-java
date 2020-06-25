/**
 *   Copyright 2018-2020 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.providers.RandomNumberGenerator;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

/**
 * Provides relevant data for initializing/creating a {@link Beacon}
 */
public interface BeaconInitializer {

    /**
     * Returns the logger for reporting messages.
     */
    Logger getLogger();

    /**
     * Returns the cache where the data of the beacon is stored until it gets sent.
     */
    BeaconCache getBeaconCache();

    /**
     * Returns the client IP address of the session / beacon.
     */
    String getClientIpAddress();

    /**
     * Returns the {@link SessionIDProvider} to obtain the identifier of the session / beacon
     */
    SessionIDProvider getSessionIdProvider();

    /**
     * Returns the sequence number for the beacon/session for identification in case of session split by events. The
     * session sequence number complements the session ID.
     */
    int getSessionSequenceNumber();

    /**
     * Returns the {@link ThreadIDProvider} to obtain the identifier of the current thread.
     */
    ThreadIDProvider getThreadIdProvider();

    /**
     * Returns the {@link TimingProvider} to obtain the current timestamp.
     */
    TimingProvider getTimingProvider();

    /**
     * Returns the {@link RandomNumberGenerator} to obtain random numbers (e.g. for randomizing device IDs)
     */
    RandomNumberGenerator getRandomNumberGenerator();
}
