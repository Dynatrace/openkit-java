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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.protocol.StatusResponse;

/**
 * Configuration class storing all configuration parameters as returned by Dynatrace/AppMon.
 */
public class ServerConfiguration {

    /** Default server configuration */
    public static final ServerConfiguration DEFAULT = new ServerConfiguration.Builder().build();

    /** by default capturing is enabled */
    static final boolean DEFAULT_CAPTURE_ENABLED = true;
    /** by default crash reporting is enabled */
    static final boolean DEFAULT_CRASH_REPORTING_ENABLED = true;
    /** by default error reporting is enabled */
    static final boolean DEFAULT_ERROR_REPORTING_ENABLED = true;
    /** default send interval is not defined */
    static final int DEFAULT_SEND_INTERVAL = -1;
    /** default server id depends on the backend */
    static final int DEFAULT_SERVER_ID = -1;
    /** default beacon size is not defined */
    static final int DEFAULT_BEACON_SIZE = -1;
    /** default multiplicity is 1 */
    static final int DEFAULT_MULTIPLICITY = 1;

    /** Boolean indicating whether capturing is enabled by the backend or not */
    private final boolean isCaptureEnabled;
    /** Boolean indicating whether crash reporting is enabled by the backend or not */
    private final boolean isCrashReportingEnabled;
    /** Boolean indicating whether error reporting is enabled by the backend or not */
    private final boolean isErrorReportingEnabled;
    /** Value specifying the send interval in milliseconds */
    private final int sendIntervalInMilliseconds;
    /** The server ID to send future requests to */
    private final int serverID;
    /** The maximum allowed beacon size (post body size) in bytes */
    private final int beaconSizeInBytes;
    /** The multiplicity value */
    private final int multiplicity;

    /**
     * Create a server configuration from a builder.
     *
     * @param builder The builder used to configure this instance.
     */
    private ServerConfiguration(Builder builder) {
        isCaptureEnabled = builder.isCaptureEnabled;
        isCrashReportingEnabled = builder.isCrashReportingEnabled;
        isErrorReportingEnabled = builder.isErrorReportingEnabled;
        sendIntervalInMilliseconds = builder.sendIntervalInMilliseconds;
        serverID = builder.serverID;
        beaconSizeInBytes = builder.beaconSizeInBytes;
        multiplicity = builder.multiplicity;
    }

    /**
     * Creates a new server configuration from the given {@link StatusResponse}
     *
     * @param statusResponse the status response from which to create the server configuration.
     * @return the newly created server configuration.
     */
    public static ServerConfiguration from(StatusResponse statusResponse) {
        if (statusResponse == null) {
            return null;
        }
        return new ServerConfiguration.Builder(statusResponse).build();
    }

    /**
     * Get a boolean indicating whether capturing is enabled in Dynatrace/AppMon or not.
     *
     * @return {@code true} if capturing is enabled, {@code false} otherwise.
     */
    public boolean isCaptureEnabled() {
        return isCaptureEnabled;
    }

    /**
     * Get a boolean indicating whether crash reporting is enabled in Dynatrace/AppMon or not.
     *
     * @return {@code true} if crash reporting is enabled, {@code false} otherwise.
     */
    public boolean isCrashReportingEnabled() {
        return isCrashReportingEnabled;
    }

    /**
     * Get a boolean indicating whether error reporting is enabled in Dynatrace/AppMon or not.
     *
     * @return {@code true} if error reporting is enabled, {@code false} otherwise.
     */
    public boolean isErrorReportingEnabled() {
        return isErrorReportingEnabled;
    }

    /**
     * Get the send interval in milliseconds.
     *
     * @return Send interval in milliseconds.
     */
    public int getSendIntervalInMilliseconds() {
        return sendIntervalInMilliseconds;
    }

    /**
     * Get the server's id to communicate with.
     *
     * @return Server ID to communicate with
     */
    public int getServerID() {
        return serverID;
    }

    /**
     * Get the maximum beacon size, that is the post body, in bytes.
     *
     * @return Maximum beacon size in bytes.
     */
    public int getBeaconSizeInBytes() {
        return beaconSizeInBytes;
    }

    /**
     * Get multiplicity value.
     *
     * <p>
     *     Multiplicity is a factor on the server side, which is greater than 1 to prevent overload situations.
     *     This value comes from the server and has to be sent back to the server.
     * </p>
     *
     * @return Multiplicity factor
     */
    public int getMultiplicity() {
        return multiplicity;
    }

    /**
     * Get a boolean indicating whether sending arbitrary data to the server is allowed or not.
     *
     * <p>
     *     Sending data is only allowed if all of the following conditions evaluate to true.
     *     * {@link #isCaptureEnabled()} is {@code true}
     *     * {@link #getMultiplicity()} is greater than {@code 0}
     *
     *     To check if sending errors is allowed, use {@link #isSendingErrorsAllowed()}.
     *     To check if sending crashes is allowed, use {@link #isSendingCrashesAllowed()}.
     * </p>
     *
     * @return {@code true} if data sending is allowed, {@code false} otherwise.
     */
    public boolean isSendingDataAllowed() {
        return isCaptureEnabled() && getMultiplicity() > 0;
    }

    /**
     * Get a boolean indicating whether sending crashes to the server is allowed or not.
     *
     * <p>
     *     Sending crashes is only allowed if all of the following conditions evaluate to true.
     *     * {@link #isSendingDataAllowed()} yields {@code true}
     *     * {@link #isCrashReportingEnabled()} is {@code true}
     * </p>
     *
     * @return {@code true} if sending crashes is allowed, {@code false} otherwise.
     */
    public boolean isSendingCrashesAllowed() {
        return isSendingDataAllowed() && isCrashReportingEnabled();
    }

    /**
     * Get a boolean indicating whether sending errors to the server is allowed or not.
     *
     * <p>
     *     Sending errors is only allowed if all of the following conditions evaluate to true.
     *     * {@link #isSendingDataAllowed()} yields {@code true}
     *     * {@link #isErrorReportingEnabled()} is {@code true}
     * </p>
     *
     * @return {@code true} if sending errors is allowed, {@code false} otherwise.
     */
    public boolean isSendingErrorsAllowed() {
        return isSendingDataAllowed() && isErrorReportingEnabled();
    }

    /**
     * Merges given {@code other} with {@code this} instance and return the merged instance.
     *
     * <p>
     *     Most fields are taken from {@code other}, except for {@link #multiplicity} and {@link #serverID}
     *     which doe not change.
     * </p>
     *
     * @param other The other instance to merge with.
     * @return New {@link ServerConfiguration} instance with merged values.
     */
    public ServerConfiguration merge(ServerConfiguration other) {

        Builder builder = new Builder();

        // settings from this
        builder.withMultiplicity(this.getMultiplicity())
               .withServerID(this.getServerID());

        // settings from other
        builder.withCapture(other.isCaptureEnabled);
        builder.withCrashReporting(other.isCrashReportingEnabled);
        builder.withErrorReporting(other.isErrorReportingEnabled);

        return builder.withSendIntervalInMilliseconds(other.getSendIntervalInMilliseconds())
                      .withBeaconSizeInBytes(other.getBeaconSizeInBytes())
                      .build();
    }

    /**
     * Builder class for creating a custom instance of {@link ServerConfiguration}.
     */
    public static final class Builder {
        private boolean isCaptureEnabled = DEFAULT_CAPTURE_ENABLED;
        private boolean isCrashReportingEnabled = DEFAULT_CRASH_REPORTING_ENABLED;
        private boolean isErrorReportingEnabled = DEFAULT_ERROR_REPORTING_ENABLED;
        private int sendIntervalInMilliseconds = DEFAULT_SEND_INTERVAL;
        private int serverID = DEFAULT_SERVER_ID;
        private int beaconSizeInBytes = DEFAULT_BEACON_SIZE;
        private int multiplicity = DEFAULT_MULTIPLICITY;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Construct and initialize fields from given {@link StatusResponse}.
         *
         * @param statusResponse Status response used for initializing the fields.
         */
        public Builder(StatusResponse statusResponse) {
            isCaptureEnabled = statusResponse.isCapture();
            isCrashReportingEnabled = statusResponse.isCaptureCrashes();
            isErrorReportingEnabled = statusResponse.isCaptureErrors();
            sendIntervalInMilliseconds = statusResponse.getSendInterval();
            serverID = statusResponse.getServerID();
            beaconSizeInBytes = statusResponse.getMaxBeaconSize();
            multiplicity = statusResponse.getMultiplicity();
        }

        /**
         * Construct and initialize fields from given {@link ServerConfiguration}.
         */
        public Builder(ServerConfiguration serverConfiguration) {
            isCaptureEnabled = serverConfiguration.isCaptureEnabled();
            isCrashReportingEnabled = serverConfiguration.isCrashReportingEnabled();
            isErrorReportingEnabled = serverConfiguration.isErrorReportingEnabled();
            sendIntervalInMilliseconds = serverConfiguration.getSendIntervalInMilliseconds();
            serverID = serverConfiguration.getServerID();
            beaconSizeInBytes = serverConfiguration.getBeaconSizeInBytes();
            multiplicity = serverConfiguration.getMultiplicity();
        }

        /**
         * Enables/disables capturing by setting {@link #isCaptureEnabled} to the corresponding value.
         *
         * @return {@code this}
         */
        public Builder withCapture(boolean captureState) {
            this.isCaptureEnabled = captureState;
            return this;
        }

        /**
         * Enables/disables crash reporting by setting {@link #isCrashReportingEnabled} to the corresponding value.
         *
         * @return {@code this}
         */
        public Builder withCrashReporting(boolean crashReportingState) {
            isCrashReportingEnabled = crashReportingState;
            return this;
        }

        /**
         * Enables/disables error reporting by setting {@link #isErrorReportingEnabled} to the corresponding value.
         *
         * @return {@code this}
         */
        public Builder withErrorReporting(boolean errorReportingState) {
            isErrorReportingEnabled = errorReportingState;
            return this;
        }

        /**
         * Configures the send interval.
         *
         * @param sendIntervalInMilliseconds Send interval in milliseconds.
         * @return {@code this}
         */
        public Builder withSendIntervalInMilliseconds(int sendIntervalInMilliseconds) {
            this.sendIntervalInMilliseconds = sendIntervalInMilliseconds;
            return this;
        }

        /**
         * Configures the server ID.
         *
         * @param serverID The server ID to communicate with.
         * @return {@code this}
         */
        public Builder withServerID(int serverID) {
            this.serverID = serverID;
            return this;
        }

        /**
         * Configures the beacon size in Bytes.
         *
         * @param beaconSizeInBytes Maximum allowed beacon size in bytes.
         * @return {@code this}
         */
        public Builder withBeaconSizeInBytes(int beaconSizeInBytes) {
            this.beaconSizeInBytes = beaconSizeInBytes;
            return this;
        }

        /**
         * Configures the multiplicity factor.
         *
         * @param multiplicity Multiplicity factor.
         * @return {@code this}
         */
        public Builder withMultiplicity(int multiplicity) {
            this.multiplicity = multiplicity;
            return this;
        }

        /**
         * Build the {@link ServerConfiguration} and return the new instance.
         *
         * @return Newly created {@link ServerConfiguration} instance.
         */
        public ServerConfiguration build() {
            return new ServerConfiguration(this);
        }
    }
}
