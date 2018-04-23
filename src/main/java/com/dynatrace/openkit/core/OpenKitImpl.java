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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

    private static final Session NULL_SESSION = new NullSession();

    // Beacon cache
    private final BeaconCacheImpl beaconCache;
    // Cache eviction thread
    private final BeaconCacheEvictor beaconCacheEvictor;

    // BeaconSender reference
    private final BeaconSender beaconSender;

    // AbstractConfiguration reference
    private final Configuration configuration;
    private final ThreadIDProvider threadIDProvider;
    private final TimingProvider timingProvider;

    //Logging context
    private final Logger logger;

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    // *** constructors ***

    public OpenKitImpl(Logger logger, Configuration config) {
        this(logger, config, new DefaultHTTPClientProvider(logger), new DefaultTimingProvider(), new DefaultThreadIDProvider());
    }

    protected OpenKitImpl(Logger logger, Configuration config, HTTPClientProvider httpClientProvider, TimingProvider timingProvider, ThreadIDProvider threadIDProvider) {
        if (logger.isInfoEnabled()) {
            // TODO: Use proper version information (incl. the build number)
            logger.info(config.getOpenKitType() + " OpenKit " + OpenKitConstants.DEFAULT_APPLICATION_VERSION
                    + " instantiated");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "applicationName=" + config.getApplicationName() + ", applicationID=" + config.getApplicationID()
                            + ", deviceID=" + config.getDeviceID() + ", endpointURL=" + config.getEndpointURL());
        }
        configuration = config;
        this.logger = logger;
        this.threadIDProvider = threadIDProvider;
        this.timingProvider = timingProvider;
        beaconCache = new BeaconCacheImpl(logger);
        beaconSender = new BeaconSender(logger, configuration, httpClientProvider, timingProvider);
        beaconCacheEvictor = new BeaconCacheEvictor(logger, beaconCache, configuration.getBeaconCacheConfiguration(), timingProvider);
    }

    /**
     * Initialize this OpenKit instance.
     *
     * <p>
     * This method starts the {@link BeaconSender} and is called directly after
     * the instance has been created in {@link com.dynatrace.openkit.AbstractOpenKitBuilder}.
     * </p>
     */
    public void initialize() {
        beaconCacheEvictor.start();
        beaconSender.initialize();
    }

    // *** OpenKit interface methods ***


    @Override
    public void close() {
        shutdown();
    }

    @Override
    public boolean waitForInitCompletion() {
        return beaconSender.waitForInit();
    }

    @Override
    public boolean waitForInitCompletion(long timeoutMillis) {
        return beaconSender.waitForInit(timeoutMillis);
    }

    @Override
    public boolean isInitialized() {
        return beaconSender.isInitialized();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Session createSession(String clientIPAddress) {
        if (logger.isDebugEnabled()) {
            logger.debug("OpenKit createSession(" + clientIPAddress + ")");
        }
        if (isShutdown.get()) {
            return NULL_SESSION;
        }
        // create beacon for session
        Beacon beacon = new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider);
        // create session
        return new SessionImpl(logger, beaconSender, beacon);
    }

    @Override
    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug("OpenKit shutdown requested");
        }
        isShutdown.set(true);
        beaconCacheEvictor.stop();
        beaconSender.shutdown();
    }
}
