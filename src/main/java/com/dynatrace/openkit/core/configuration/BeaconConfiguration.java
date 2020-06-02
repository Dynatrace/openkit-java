/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit.core.configuration;

/**
 * Configuration class storing all necessary data for a {@link com.dynatrace.openkit.protocol.Beacon}.
 */
public class BeaconConfiguration {

    /** OpenKit related configuration, that has been configured using the builder */
    private final OpenKitConfiguration openKitConfiguration;
    /** Privacy configuration, which has also been configured via the builder */
    private final PrivacyConfiguration privacyConfiguration;
    /** HTTP client configuration */
    private final HTTPClientConfiguration httpClientConfiguration;
    /** Server configuration, which can be updated by the server. */
    private ServerConfiguration serverConfiguration;
    /** indicator if the {@link ServerConfiguration} was set or not */
    private boolean isServerConfigurationSet;

    /** callback when the server configuration is updated. */
    private ServerConfigurationUpdateCallback serverConfigUpdateCallback;
    /** Object for synchronization */
    private final Object lockObject = new Object();

    private BeaconConfiguration(OpenKitConfiguration openKitConfiguration,
                                PrivacyConfiguration privacyConfiguration, int serverId) {
        this.openKitConfiguration = openKitConfiguration;
        this.privacyConfiguration = privacyConfiguration;
        this.httpClientConfiguration = HTTPClientConfiguration.modifyWith(openKitConfiguration)
                .withServerID(serverId)
                .build();
        this.serverConfiguration = null; // not set for the first time
    }

    /**
     * Create {@link BeaconConfiguration} from given {@link OpenKitConfiguration} and {@link PrivacyConfiguration}.
     *
     * @param openKitConfiguration OpenKit configuration
     * @param privacyConfiguration Privacy settings configuration.
     *
     * @return {@code null} if any of the given argument is {@code null}, otherwise a new {@link BeaconConfiguration}.
     */
    public static BeaconConfiguration from(OpenKitConfiguration openKitConfiguration,
                                           PrivacyConfiguration privacyConfiguration, int serverId) {
        if (openKitConfiguration == null || privacyConfiguration == null) {
            return null;
        }

        return new BeaconConfiguration(openKitConfiguration, privacyConfiguration, serverId);
    }

    /**
     * Get the OpenKit configuration object.
     *
     * @return OpenKit related configuration.
     */
    public OpenKitConfiguration getOpenKitConfiguration() {
        return openKitConfiguration;
    }

    /**
     * Get the OpenKit privacy configuration.
     *
     * @return Privacy related configuration.
     */
    public PrivacyConfiguration getPrivacyConfiguration() {
        return privacyConfiguration;
    }

    /**
     * Get server configuration that has been set before.
     *
     * <p>
     *     If no server configuration has been set, use the default one.
     * </p>
     *
     * @return A {@link ServerConfiguration} object.
     */
    public ServerConfiguration getServerConfiguration() {
        synchronized (lockObject) {
            return serverConfiguration != null
                ? serverConfiguration
                : ServerConfiguration.DEFAULT;
        }
    }

    /**
     * Enables the capturing and sets {@link #isServerConfigurationSet()}
     */
    public void enableCapture() {
        updateCaptureWith(true);
    }

    /**
     * Disables capturing and sets {@link #isServerConfigurationSet()}
     */
    public void disableCapture() {
        updateCaptureWith(false);
    }

    /**
     * Enables/disables capture according to the given state
     *
     * @param captureState the state to which capture will be set
     */
    private void updateCaptureWith(boolean captureState) {
        synchronized (lockObject) {
            ServerConfiguration currentServerConfig = getServerConfiguration();
            serverConfiguration = new ServerConfiguration.Builder(currentServerConfig)
                    .withCapture(captureState)
                    .build();

            isServerConfigurationSet = true;
        }
    }

    /**
     * Initializes this beacon configuration with the given server configuration. This will not set
     * {@link #isServerConfigurationSet()} to {@code true} so that new session requests to the server will still be done.
     *
     * In case the {@link #isServerConfigurationSet()} was already set, this method does nothing.
     *
     * @param initialServerConfiguration the server configuration to initialize this beacon configuration with.
     */
    public void initializeServerConfiguration(ServerConfiguration initialServerConfiguration) {
        if (initialServerConfiguration == null || initialServerConfiguration.equals(ServerConfiguration.DEFAULT)) {
            // ignore DEFAULT configuration since server configuration update does not take over certain attributes
            // when merging and the configuration already exists.
            return;
        }

        synchronized (lockObject) {
            if (isServerConfigurationSet) {
                return;
            }

            serverConfiguration = initialServerConfiguration;
        }

        notifyServerConfigurationUpdate(initialServerConfiguration);
    }

    /**
     * Update the ServerConfiguration object.
     *
     * <p>
     *     If this is the first call to this method, use the configuration as is, otherwise
     *     merge the given configuration with the one already stored.
     * </p>
     *
     * @param newServerConfiguration New server configuration, as received from the server.
     */
    public void updateServerConfiguration(ServerConfiguration newServerConfiguration) {
        if (newServerConfiguration == null) {
            return;
        }

        synchronized (lockObject) {
            if (isServerConfigurationSet) {
                // server configuration already exists,
                // therefore merge new one with the existing one.
                newServerConfiguration = serverConfiguration.merge(newServerConfiguration);
            }
            serverConfiguration = newServerConfiguration;
            isServerConfigurationSet = true;
        }

        // notify has to be called outside of the synchronized block
        // to avoid deadlock situations with SessionProxyImpl
        notifyServerConfigurationUpdate(newServerConfiguration);
    }

    private void notifyServerConfigurationUpdate(ServerConfiguration serverConfig) {
        if (serverConfigUpdateCallback != null) {
            serverConfigUpdateCallback.onServerConfigurationUpdate(serverConfig);
        }
    }

    /**
     * Get a boolean indicating whether the server configuration has been set before or not.
     *
     * @return {@code true} if the {@link ServerConfiguration} has been set before, {@code false} otherwise.
     */
    public boolean isServerConfigurationSet() {
        synchronized (lockObject) {
            return isServerConfigurationSet;
        }
    }

    /**
     * Sets the callback which will be invoked when the server configuration will be updated.
     * @param updateCallback the callback to be called on server configuration updates.
     */
    public void setServerConfigurationUpdateCallback(ServerConfigurationUpdateCallback updateCallback) {
        synchronized (lockObject) {
            serverConfigUpdateCallback = updateCallback;
        }
    }

    /**
     * Get HTTP client configuration.
     *
     * @return HTTP client configuration.
     */
    public HTTPClientConfiguration getHTTPClientConfiguration() {
        return httpClientConfiguration;
    }
}
