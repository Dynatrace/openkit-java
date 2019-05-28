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
    public static final ServerConfiguration DEFAULT = new ServerConfiguration();

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
     * Create a default server configuration.
     *
     * <p>
     *     To access the default server configuration use the {@link ServerConfiguration#DEFAULT}.
     * </p>
     */
    private ServerConfiguration() {
        isCaptureEnabled = DEFAULT_CAPTURE_ENABLED;
        isCrashReportingEnabled = DEFAULT_CRASH_REPORTING_ENABLED;
        isErrorReportingEnabled = DEFAULT_ERROR_REPORTING_ENABLED;
        sendIntervalInMilliseconds = DEFAULT_SEND_INTERVAL;
        serverID = DEFAULT_SERVER_ID;
        beaconSizeInBytes = DEFAULT_BEACON_SIZE;
        multiplicity = DEFAULT_MULTIPLICITY;
    }

    /**
     * Create a server configuration from a status response.
     *
     * <p>
     *     Use the {@link ServerConfiguration#from(StatusResponse)} to get a {@link ServerConfiguration}.
     * </p>
     *
     * @param statusResponse The status response from Dynatrace or AppMon.
     */
    private ServerConfiguration(StatusResponse statusResponse) {
        isCaptureEnabled = statusResponse.isCapture();
        isCrashReportingEnabled = statusResponse.isCaptureCrashes();
        isErrorReportingEnabled = statusResponse.isCaptureErrors();
        sendIntervalInMilliseconds = statusResponse.getSendInterval();
        serverID = statusResponse.getServerID();
        beaconSizeInBytes = statusResponse.getMaxBeaconSize();
        multiplicity = statusResponse.getMultiplicity();
    }

    /**
     * Create a {@link ServerConfiguration} from given {@link StatusResponse}.
     *
     * @param statusResponse The status response for which to create a {@link ServerConfiguration}.
     * @return Newly created {@link ServerConfiguration} or {@code null} if given argument is {@code null}
     */
    public static ServerConfiguration from(StatusResponse statusResponse) {
        if (statusResponse == null) {
            return null;
        }
        return new ServerConfiguration(statusResponse);
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
}
