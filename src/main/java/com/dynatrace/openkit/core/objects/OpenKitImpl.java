/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.DefaultHTTPClientProvider;
import com.dynatrace.openkit.providers.DefaultThreadIDProvider;
import com.dynatrace.openkit.providers.DefaultTimingProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.io.IOException;
import java.util.List;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl extends OpenKitComposite implements OpenKit {

    /** Session returned by {@link #createSession(String)}, after calling {@link #shutdown()} */
    static final Session NULL_SESSION = new NullSession();
    /** Cache class used to store serialized {@link Beacon} data */
    private final BeaconCacheImpl beaconCache;
    /** Cache eviction thread */
    private final BeaconCacheEvictor beaconCacheEvictor;
    /** BeaconSender reference */
    private final BeaconSender beaconSender;
    /** Container storing configuration given into the OpenKit builders */
    private final Configuration configuration;
    /** Provider responsible to provide the thread id. */
    private final ThreadIDProvider threadIDProvider;
    /** Provider responsible to provide time related functions */
    private final TimingProvider timingProvider;
    /** {@link Logger} for tracing log message */
    private final Logger logger;
    /** Boolean value, indicating whether this {@link OpenKit} instance is shutdown or not */
    private boolean isShutdown = false;
    /** Object for synchronizing access */
    private final Object lockObject = new Object();

    /**
     * Public constructor for creating an OpenKit instance.
     *
     * @param logger Logger for logging messages.
     * @param configuration OpenKit configuration
     */
    public OpenKitImpl(Logger logger, Configuration configuration) {
        logOpenKitInstanceCreation(logger, configuration);

        this.logger = logger;
        this.configuration = configuration;
        timingProvider = new DefaultTimingProvider();
        threadIDProvider = new DefaultThreadIDProvider();
        beaconCache = new BeaconCacheImpl(logger);
        beaconCacheEvictor = new BeaconCacheEvictor(logger, beaconCache, configuration.getBeaconCacheConfiguration(), timingProvider);
        beaconSender = new BeaconSender(logger, configuration, new DefaultHTTPClientProvider(logger), timingProvider);
    }

    /**
     * Internal constructor that shall be used for testing only.
     *
     * @param logger Logger for logging messages.
     * @param configuration OpenKit configuration
     * @param timingProvider Provider for getting timing information
     * @param threadIDProvider For getting thread identifier
     * @param beaconCache Cache where beacon data is stored
     * @param beaconSender Sending that is responsible for sending beacon related data
     * @param beaconCacheEvictor Evictor to prevent OOM due to full cache
     */
    OpenKitImpl(Logger logger,
                Configuration configuration,
                TimingProvider timingProvider,
                ThreadIDProvider threadIDProvider,
                BeaconCacheImpl beaconCache,
                BeaconSender beaconSender,
                BeaconCacheEvictor beaconCacheEvictor) {
        logOpenKitInstanceCreation(logger, configuration);

        this.configuration = configuration;
        this.logger = logger;
        this.threadIDProvider = threadIDProvider;
        this.timingProvider = timingProvider;
        this.beaconCache = beaconCache;
        this.beaconSender = beaconSender;
        this.beaconCacheEvictor = beaconCacheEvictor;
    }

    /**
     * Helper class to write a message upon instance creation.
     *
     * @param logger The logger to which to write to
     * @param configuration OpenKit related configuration
     */
    private static void logOpenKitInstanceCreation(Logger logger, Configuration configuration) {
        if (logger.isInfoEnabled()) {
            logger.info(OpenKitImpl.class.getSimpleName() + " - " + configuration.getOpenKitType() + " OpenKit " + OpenKitConstants.DEFAULT_APPLICATION_VERSION
                + " instantiated");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                OpenKitImpl.class.getSimpleName() + " - applicationName=" + configuration.getApplicationName() + ", applicationID=" + configuration.getApplicationID()
                    + ", deviceID=" + configuration.getDeviceID() + ", endpointURL=" + configuration.getEndpointURL());
        }
    }

    /**
     * Initialize this OpenKit instance.
     *
     * <p>
     *     This method starts the {@link BeaconSender} and is called directly after
     *     the instance has been created in {@link com.dynatrace.openkit.AbstractOpenKitBuilder}.
     * </p>
     */
    public void initialize() {
        beaconCacheEvictor.start();
        beaconSender.initialize();
    }

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
            logger.debug(getClass().getSimpleName() + " createSession(" + clientIPAddress + ")");
        }
        synchronized (lockObject) {
            if (!isShutdown) {
                // create beacon for session
                Beacon beacon = new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider);
                // create session and add it to the list of children
                SessionImpl session = new SessionImpl(logger, this, beaconSender, beacon);
                storeChildInList(session);

                return session;
            }
        }

        return NULL_SESSION;
    }

    @Override
    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " shutdown() - shutdown requested");
        }
        synchronized (lockObject) {
            if (isShutdown) {
                // shutdown has been called before
                return;
            }
            isShutdown = true;
        }

        // close all open children
        List<OpenKitObject> childObjects = getCopyOfChildObjects();
        for (OpenKitObject childObject : childObjects) {
            try {
                childObject.close();
            } catch (IOException e) {
                // should not happen, nevertheless let's log an error
                logger.error(this + "Caught IOException while closing OpenKitObject (" + childObject + ")", e);
            }
        }

        beaconCacheEvictor.stop();
        beaconSender.shutdown();
    }

    @Override
    void onChildClosed(OpenKitObject childObject) {
        synchronized (lockObject) {
            removeChildFromList(childObject);
        }
    }
}
