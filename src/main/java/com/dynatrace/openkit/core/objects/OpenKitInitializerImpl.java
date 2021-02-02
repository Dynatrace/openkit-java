/**
 * Copyright 2018-2021 Dynatrace LLC
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

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.SessionWatchdog;
import com.dynatrace.openkit.core.SessionWatchdogContext;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.providers.DefaultHTTPClientProvider;
import com.dynatrace.openkit.providers.DefaultSessionIDProvider;
import com.dynatrace.openkit.providers.DefaultThreadIDProvider;
import com.dynatrace.openkit.providers.DefaultTimingProvider;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

public class OpenKitInitializerImpl implements OpenKitInitializer {

    private final Logger logger;
    private final PrivacyConfiguration privacyConfiguration;
    private final OpenKitConfiguration openKitConfiguration;
    private final TimingProvider timingProvider;
    private final ThreadIDProvider threadIdProvider;
    private final SessionIDProvider sessionIdProvider;
    private final BeaconCache beaconCache;
    private final BeaconCacheEvictor beaconCacheEvictor;
    private final BeaconSender beaconSender;
    private final SessionWatchdog sessionWatchdog;

    public OpenKitInitializerImpl(AbstractOpenKitBuilder builder) {
        logger = builder.getLogger();
        privacyConfiguration = PrivacyConfiguration.from(builder);
        openKitConfiguration = OpenKitConfiguration.from(builder);

        timingProvider = new DefaultTimingProvider();
        threadIdProvider = new DefaultThreadIDProvider();
        sessionIdProvider = new DefaultSessionIDProvider();

        beaconCache = new BeaconCacheImpl(logger);
        beaconCacheEvictor = new BeaconCacheEvictor(logger, beaconCache, BeaconCacheConfiguration.from(builder), timingProvider);

        HTTPClientConfiguration httpClientConfig = HTTPClientConfiguration.from(openKitConfiguration);
        beaconSender = new BeaconSender(logger, httpClientConfig, new DefaultHTTPClientProvider(logger), timingProvider);
        sessionWatchdog = new SessionWatchdog(logger, new SessionWatchdogContext(timingProvider));
    }


    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public PrivacyConfiguration getPrivacyConfiguration() {
        return privacyConfiguration;
    }

    @Override
    public OpenKitConfiguration getOpenKitConfiguration() {
        return openKitConfiguration;
    }

    @Override
    public TimingProvider getTimingProvider() {
        return timingProvider;
    }

    @Override
    public ThreadIDProvider getThreadIdProvider() {
        return threadIdProvider;
    }

    @Override
    public SessionIDProvider getSessionIdProvider() {
        return sessionIdProvider;
    }

    @Override
    public BeaconCache getBeaconCache() {
        return beaconCache;
    }

    @Override
    public BeaconCacheEvictor getBeaconCacheEvictor() {
        return beaconCacheEvictor;
    }

    @Override
    public BeaconSender getBeaconSender() {
        return beaconSender;
    }

    @Override
    public SessionWatchdog getSessionWatchdog() {
        return sessionWatchdog;
    }
}
