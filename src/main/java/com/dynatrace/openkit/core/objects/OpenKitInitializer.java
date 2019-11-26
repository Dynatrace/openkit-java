/**
 *   Copyright 2018-2019 Dynatrace LLC
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
package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.SessionWatchdog;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

/**
 * Provides relevant data for initializing / creating an OpenKit instance.
 */
public interface OpenKitInitializer {

    /**
     * Logger for reporting messages
     */
    Logger getLogger();

    /**
     * Privacy settings of which data is collected
     */
    PrivacyConfiguration getPrivacyConfiguration();

    /**
     * OpenKit / application related configuration.
     */
    OpenKitConfiguration getOpenKitConfiguration();

    /**
     * Provider to obtain the current timestamp.
     */
    TimingProvider getTimingProvider();

    /**
     * Provider for the identifier of the current thread.
     */
    ThreadIDProvider getThreadIdProvider();

    /**
     * Provider to obtain the identifier for the next session.
     */
    SessionIDProvider getSessionIdProvider();

    /**
     * Cache where beacon data is stored until it is sent.
     */
    BeaconCache getBeaconCache();

    /**
     * Eviction thread to avoid the beacon cache from overflowing.
     */
    BeaconCacheEvictor getBeaconCacheEvictor();

    /**
     * Sender thread for sending beacons to the server.
     */
    BeaconSender getBeaconSender();

    /**
     * Watchdog thread to perform certain actions for sessions at/after a specific time.
     */
    SessionWatchdog getSessionWatchdog();
}
