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

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.providers.DefaultHTTPClientProvider;
import com.dynatrace.openkit.providers.DefaultSessionIDProvider;
import com.dynatrace.openkit.providers.DefaultThreadIDProvider;
import com.dynatrace.openkit.providers.DefaultTimingProvider;
import com.dynatrace.openkit.providers.SessionIDProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.io.IOException;
import java.util.List;

/**
 * Actual implementation of the {@link OpenKit} interface.
 */
public class OpenKitImpl extends OpenKitComposite implements OpenKit, SessionCreatorInput {

    /** {@link Logger} for tracing log message */
    private final Logger logger;

    /** Provider responsible to provide the thread id. */
    private final ThreadIDProvider threadIDProvider;
    /** Provider responsible to provide time related functions */
    private final TimingProvider timingProvider;
    /** Provider responsible to provide Session IDs */
    private final SessionIDProvider sessionIDProvider;

    /** Configuration object storing privacy related configuration */
    private final PrivacyConfiguration privacyConfiguration;
    /** Configuration object storing OpenKit configuration */
    private final OpenKitConfiguration openKitConfiguration;

    /** Cache class used to store serialized {@link Beacon} data */
    private final BeaconCache beaconCache;
    /** Cache eviction thread */
    private final BeaconCacheEvictor beaconCacheEvictor;
    /** BeaconSender reference */
    private final BeaconSender beaconSender;

    /** Boolean value, indicating whether this {@link OpenKit} instance is shutdown or not */
    private boolean isShutdown = false;
    /** Object for synchronizing access */
    private final Object lockObject = new Object();

    /**
     * Public constructor for creating an OpenKit instance.
     *
     * @param builder Builder used to set all OpenKit related configuration parameters.
     */
    public OpenKitImpl(AbstractOpenKitBuilder builder) {

        logger = builder.getLogger();
        privacyConfiguration = PrivacyConfiguration.from(builder);
        openKitConfiguration = OpenKitConfiguration.from(builder);

        timingProvider = new DefaultTimingProvider();
        threadIDProvider = new DefaultThreadIDProvider();
        sessionIDProvider = new DefaultSessionIDProvider();

        beaconCache = new BeaconCacheImpl(logger);
        beaconCacheEvictor = new BeaconCacheEvictor(logger, beaconCache, BeaconCacheConfiguration.from(builder), timingProvider);

        HTTPClientConfiguration httpClientConfig = HTTPClientConfiguration.from(openKitConfiguration);
        beaconSender = new BeaconSender(logger, httpClientConfig, new DefaultHTTPClientProvider(logger), timingProvider);

        logOpenKitInstanceCreation(logger, openKitConfiguration);
    }

    /**
     * Internal constructor that shall be used for testing only.
     *
     * @param logger Logger for logging messages.
     * @param privacyConfiguration OpenKit privacy configuration
     * @param openKitConfiguration General OpenKit related configuration
     * @param timingProvider Provider for getting timing information
     * @param threadIDProvider For getting thread identifier
     * @param beaconCache Cache where beacon data is stored
     * @param beaconSender Sending that is responsible for sending beacon related data
     * @param beaconCacheEvictor Evictor to prevent OOM due to full cache
     */
    OpenKitImpl(Logger logger,
                PrivacyConfiguration privacyConfiguration,
                OpenKitConfiguration openKitConfiguration,
                TimingProvider timingProvider,
                ThreadIDProvider threadIDProvider,
                SessionIDProvider sessionIDProvider,
                BeaconCache beaconCache,
                BeaconSender beaconSender,
                BeaconCacheEvictor beaconCacheEvictor) {
        this.logger = logger;
        this.privacyConfiguration = privacyConfiguration;
        this.openKitConfiguration = openKitConfiguration;
        this.threadIDProvider = threadIDProvider;
        this.timingProvider = timingProvider;
        this.sessionIDProvider = sessionIDProvider;
        this.beaconCache = beaconCache;
        this.beaconSender = beaconSender;
        this.beaconCacheEvictor = beaconCacheEvictor;

        logOpenKitInstanceCreation(this.logger, this.openKitConfiguration);
    }

    /**
     * Helper class to write a message upon instance creation.
     *
     * @param logger The logger to which to write to
     * @param openKitConfiguration OpenKit related configuration
     */
    private static void logOpenKitInstanceCreation(Logger logger, OpenKitConfiguration openKitConfiguration) {
        if (logger.isInfoEnabled()) {
            logger.info(OpenKitImpl.class.getSimpleName()
                + " - " + openKitConfiguration.getOpenKitType()
                + " OpenKit " + OpenKitConstants.DEFAULT_APPLICATION_VERSION
                + " instantiated");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(OpenKitImpl.class.getSimpleName()
                + " - applicationName=" + openKitConfiguration.getApplicationName()
                + ", applicationID=" + openKitConfiguration.getApplicationID()
                + ", deviceID=" + openKitConfiguration.getDeviceID()
                + ", origDeviceID=" + openKitConfiguration.getOrigDeviceID()
                + ", endpointURL=" + openKitConfiguration.getEndpointURL());
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

    @Override
    public Session createSession(String clientIPAddress) {
        if (logger.isDebugEnabled()) {
            logger.debug(getClass().getSimpleName() + " createSession(" + clientIPAddress + ")");
        }
        synchronized (lockObject) {
            if (!isShutdown) {
                // create beacon for session
                int serverId = beaconSender.getCurrentServerId();
                BeaconConfiguration beaconConfiguration = BeaconConfiguration.from(
                        openKitConfiguration,
                        privacyConfiguration,
                        serverId
                );
                Beacon beacon = new Beacon(
                        logger,
                        beaconCache,
                        beaconConfiguration,
                        clientIPAddress,
                        sessionIDProvider,
                        threadIDProvider,
                        timingProvider
                );
                // create session and add it to the list of children
                SessionImpl session = new SessionImpl(logger, this, beacon);
                beaconSender.addSession(session);

                storeChildInList(session);

                return session;
            }
        }

        return NullSession.INSTANCE;
    }

    @Override
    public Session createSession() {
        return createSession(null);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Session creator input
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public OpenKitConfiguration getOpenKitConfiguration() {
        return openKitConfiguration;
    }

    @Override
    public PrivacyConfiguration getPrivacyConfiguration() {
        return privacyConfiguration;
    }

    @Override
    public BeaconCache getBeaconCache() {
        return beaconCache;
    }

    @Override
    public SessionIDProvider getSessionIdProvider() {
        return sessionIDProvider;
    }

    @Override
    public ThreadIDProvider getThreadIdProvider() {
        return threadIDProvider;
    }

    @Override
    public TimingProvider getTimingProvider() {
        return timingProvider;
    }

    @Override
    public int getCurrentServerId() {
        return beaconSender.getCurrentServerId();
    }
}
