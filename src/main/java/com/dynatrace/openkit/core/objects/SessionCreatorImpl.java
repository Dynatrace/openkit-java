/**
 * Copyright 2018-2020 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.BeaconInitializer;
import com.dynatrace.openkit.providers.DefaultRandomNumberGenerator;
import com.dynatrace.openkit.providers.FixedRandomNumberGenerator;
import com.dynatrace.openkit.providers.FixedSessionIdProvider;
import com.dynatrace.openkit.providers.RandomNumberGenerator;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

public class SessionCreatorImpl implements SessionCreator, BeaconInitializer {

    // log message reporter
    private final Logger logger;
    // OpenKit related configuration
    private final OpenKitConfiguration openKitConfiguration;
    // privacy related configuration
    private final PrivacyConfiguration privacyConfiguration;
    // provider to obtain the ID of the current thread
    private final ThreadIDProvider threadIdProvider;
    // provider to obtain the current time
    private final TimingProvider timingProvider;
    // cache for storing beacon data until it gets send
    private final BeaconCache beaconCache;

    private final String clientIpAddress;
    private final int serverId;

    private final SessionIDProvider continuousSessionIdProvider;
    private final RandomNumberGenerator continuousRandomGenerator;

    // provider which will always return the same session number
    private SessionIDProvider fixedSessionIdProvider;
    // provider which will always the same random number
    private RandomNumberGenerator fixedRandomNumberGenerator;

    private int sessionSequenceNumber;

    SessionCreatorImpl(SessionCreatorInput input, String clientIpAddress) {
        this.logger = input.getLogger();
        this.openKitConfiguration = input.getOpenKitConfiguration();
        this.privacyConfiguration = input.getPrivacyConfiguration();
        this.beaconCache = input.getBeaconCache();
        this.threadIdProvider = input.getThreadIdProvider();
        this.timingProvider = input.getTimingProvider();
        this.clientIpAddress = clientIpAddress;

        this.serverId = input.getCurrentServerId();
        this.continuousSessionIdProvider = input.getSessionIdProvider();
        this.continuousRandomGenerator = new DefaultRandomNumberGenerator();

        initializeFixedNumberProviders();
    }

    private void initializeFixedNumberProviders() {
        fixedSessionIdProvider = new FixedSessionIdProvider(continuousSessionIdProvider);
        fixedRandomNumberGenerator = new FixedRandomNumberGenerator(continuousRandomGenerator);
    }

    @Override
    public SessionImpl createSession(OpenKitComposite parent) {
        BeaconConfiguration configuration = BeaconConfiguration.from(
                openKitConfiguration,
                privacyConfiguration,
                serverId
        );

        Beacon beacon = new Beacon(this, configuration);
        SessionImpl session = new SessionImpl(logger, parent, beacon);

        sessionSequenceNumber++;

        return session;
    }

    @Override
    public void reset() {
        sessionSequenceNumber = 0;
        initializeFixedNumberProviders();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// BeaconInitializer implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public BeaconCache getBeaconCache() {
        return beaconCache;
    }

    @Override
    public String getClientIpAddress() {
        return clientIpAddress;
    }

    @Override
    public SessionIDProvider getSessionIdProvider() {
        return fixedSessionIdProvider;
    }

    @Override
    public int getSessionSequenceNumber() {
        return sessionSequenceNumber;
    }

    @Override
    public ThreadIDProvider getThreadIdProvider() {
        return threadIdProvider;
    }

    @Override
    public TimingProvider getTimingProvider() {
        return timingProvider;
    }

    @Override
    public RandomNumberGenerator getRandomNumberGenerator() {
        return fixedRandomNumberGenerator;
    }
}
