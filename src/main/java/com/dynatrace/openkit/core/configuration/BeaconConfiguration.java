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
     * Enables the capturing and implicitly sets {@link #isServerConfigurationSet()}
     */
    public void enableCapture() {
        updateCaptureWith(true);
    }

    /**
     * Disables capturing and implicitly sets {@link #isServerConfigurationSet()}
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
        }
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
            if (serverConfiguration == null) {
                // no server configuration has been set so far
                // no need to merge something -> just store it
                serverConfiguration = newServerConfiguration;
            } else {
                serverConfiguration = serverConfiguration.merge(newServerConfiguration);
            }
        }
    }

    /**
     * Get a boolean indicating whether the server configuration has been set before or not.
     *
     * @return {@code true} if the {@link ServerConfiguration} has been set before, {@code false} otherwise.
     */
    public boolean isServerConfigurationSet() {
        synchronized (lockObject) {
            return serverConfiguration != null;
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
