/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.*;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl implements OpenKit {

    // Beacon cache
    private final BeaconCache beaconCache;

    // BeaconSender reference
    private final BeaconSender beaconSender;

    // AbstractConfiguration reference
    private final Configuration configuration;
    private final ThreadIDProvider threadIDProvider;
    private final TimingProvider timingProvider;

    //Logging context
    private final Logger logger;

    // *** constructors ***

    public OpenKitImpl(Logger logger, Configuration config) {
        this(logger, config, new DefaultHTTPClientProvider(logger), new DefaultTimingProvider(), new DefaultThreadIDProvider());
    }

    protected OpenKitImpl(Logger logger, Configuration config, HTTPClientProvider httpClientProvider, TimingProvider timingProvider, ThreadIDProvider threadIDProvider) {
        configuration = config;
        this.logger = logger;
        this.threadIDProvider = threadIDProvider;
        this.timingProvider = timingProvider;
        beaconCache = new BeaconCache();
        // TODO stefan.eberl@dynatrace.com - BeaconCacheEvictor must be instantiated here too
        beaconSender = new BeaconSender(configuration, httpClientProvider, timingProvider);
    }

    /**
     * Initialize this OpenKit instance.
     * <p>
     * <p>
     * This method starts the {@link BeaconSender} and is called directly after
     * the instance has been created in {@link com.dynatrace.openkit.AbstractOpenKitBuilder}.
     * </p>
     */
    public void initialize() {
        beaconSender.initialize();
    }

    // *** OpenKit interface methods ***

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
        // create beacon for session
        Beacon beacon = new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider);
        // create session
        return new SessionImpl(beaconSender, beacon);
    }

    @Override
    public void shutdown() {
        beaconSender.shutdown();
    }
}
