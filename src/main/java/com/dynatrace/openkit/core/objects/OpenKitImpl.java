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

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.core.BeaconSender;
import com.dynatrace.openkit.core.SessionWatchdog;
import com.dynatrace.openkit.core.caching.BeaconCache;
import com.dynatrace.openkit.core.caching.BeaconCacheEvictor;
import com.dynatrace.openkit.core.configuration.OpenKitConfiguration;
import com.dynatrace.openkit.core.configuration.PrivacyConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
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
    /** watchdog thread to perform certain actions on a session after a specific time */
    private final SessionWatchdog sessionWatchdog;

    /** Boolean value, indicating whether this {@link OpenKit} instance is shutdown or not */
    private boolean isShutdown = false;
    /** Object for synchronizing access */
    private final Object lockObject = new Object();

    /**
     * Public constructor for creating an OpenKit instance.
     *
     * @param initializer provider to get all OpenKit related configuration parameters.
     */
    public OpenKitImpl(OpenKitInitializer initializer) {
        this.logger = initializer.getLogger();
        this.privacyConfiguration = initializer.getPrivacyConfiguration();
        this.openKitConfiguration = initializer.getOpenKitConfiguration();
        this.threadIDProvider = initializer.getThreadIdProvider();
        this.timingProvider = initializer.getTimingProvider();
        this.sessionIDProvider = initializer.getSessionIdProvider();
        this.beaconCache = initializer.getBeaconCache();
        this.beaconSender = initializer.getBeaconSender();
        this.beaconCacheEvictor = initializer.getBeaconCacheEvictor();
        this.sessionWatchdog = initializer.getSessionWatchdog();

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
     *     the instance has been created in {@link com.dynatrace.openkit.DynatraceOpenKitBuilder}.
     * </p>
     */
    public void initialize() {
        beaconCacheEvictor.start();
        sessionWatchdog.initialize();
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
                SessionCreator sessionCreator = new SessionCreatorImpl(this, clientIPAddress);
                SessionProxyImpl sessionProxy = new SessionProxyImpl(
                        logger,
                        this,
                        sessionCreator,
                        timingProvider,
                        beaconSender,
                        sessionWatchdog
                );

                storeChildInList(sessionProxy);

                return sessionProxy;
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
        sessionWatchdog.shutdown();
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
