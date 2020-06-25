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
package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

/**
 * Provider of the input parameters for the {@link SessionCreator}
 */
public interface SessionCreatorInput {

    /**
     * Returns the logger to report/trace messages.
     */
    Logger getLogger();

    /**
     * Returns the application / device related configuration
     */
    OpenKitConfiguration getOpenKitConfiguration();

    /**
     * Returns the privacy related configuration
     */
    PrivacyConfiguration getPrivacyConfiguration();

    /**
     * Returns the beacon cache in which new sessions/beacons will be stored until they are sent.
     */
    BeaconCache getBeaconCache();

    /**
     * Returns the provider to obtain the next session ID
     */
    SessionIDProvider getSessionIdProvider();

    /**
     * Returns the provider to obtain the ID of the current thread.
     */
    ThreadIDProvider getThreadIdProvider();

    /**
     * Returns the provider to obtain the current timestamp.
     */
    TimingProvider getTimingProvider();

    /**
     * Returns the current server ID.
     */
    int getCurrentServerId();
}
