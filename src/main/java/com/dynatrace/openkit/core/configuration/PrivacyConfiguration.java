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

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

/**
 * Configuration class storing user configured privacy settings.
 */
public class PrivacyConfiguration {

    private final DataCollectionLevel dataCollectionLevel;
    private final CrashReportingLevel crashReportingLevel;

    /**
     * Construct the privacy configuration
     *
     * @param builder Builder class used to configure all configuration related options.
     */
    private PrivacyConfiguration(AbstractOpenKitBuilder builder) {

        this.dataCollectionLevel = builder.getDataCollectionLevel();
        this.crashReportingLevel = builder.getCrashReportLevel();
    }

    /**
     * Create a {@link PrivacyConfiguration} from given {@link AbstractOpenKitBuilder}.
     *
     * @param builder The OpenKit builder for which to create a {@link PrivacyConfiguration}.
     * @return Newly created {@link PrivacyConfiguration} or {@code null} if given argument is {@code null}
     */
    public static PrivacyConfiguration from(AbstractOpenKitBuilder builder) {
        if (builder == null) {
            return null;
        }
        return new PrivacyConfiguration(builder);
    }

    /**
     * Get the data collection level.
     *
     * @return Data collection level, which was set in the constructor.
     */
    public DataCollectionLevel getDataCollectionLevel() {
        return dataCollectionLevel;
    }

    /**
     * Get the crash reporting level.
     *
     * @return Crash reporting level, which was set in the constructor.
     */
    public CrashReportingLevel getCrashReportingLevel() {
        return crashReportingLevel;
    }

    /**
     * Gives a boolean indicating whether sending the device identifier is allowed or not.
     *
     * @return {@code true} if sending device identifier is allowed, {@code false} otherwise.
     */
    public boolean isDeviceIDSendingAllowed() {
        return dataCollectionLevel == DataCollectionLevel.USER_BEHAVIOR;
    }

    /**
     * Gives a boolean indicating whether sending the session number is allowed or not.
     *
     * @return {@code true} if sending the session number is allowed, {@code false} otherwise.
     */
    public boolean isSessionNumberReportingAllowed() {
        return dataCollectionLevel == DataCollectionLevel.USER_BEHAVIOR;
    }

    /**
     * Gives a boolean indicating whether tracing web requests is allowed or not.
     *
     * @return {@code true} if web request tracing is allowed, {@code false} otherwise.
     */
    public boolean isWebRequestTracingAllowed() {
        return dataCollectionLevel != DataCollectionLevel.OFF;
    }

    /**
     * Gives a boolean indicating whether reporting ended sessions is allowed or not.
     *
     * @return {@code true} if ended sessions can be reported, {@code false} otherwise.
     */
    public boolean isSessionReportingAllowed() {
        return dataCollectionLevel != DataCollectionLevel.OFF;
    }

    /**
     * Gives a boolean indicating whether reporting actions is allowed or not.
     *
     * @return {@code true} if action reporting is allowed, {@code false} otherwise.
     */
    public boolean isActionReportingAllowed() {
        return dataCollectionLevel != DataCollectionLevel.OFF;
    }

    /**
     * Gives a boolean indicating whether reporting values is allowed or not.
     *
     * @return {@code true} if value reporting is allowed, {@code false} otherwise.
     */
    public boolean isValueReportingAllowed() {
        return dataCollectionLevel == DataCollectionLevel.USER_BEHAVIOR;
    }

    /**
     * Gives a boolean indicating whether reporting events is allowed or not.
     *
     * @return {@code true} if event reporting is allowed, {@code false} otherwise.
     */
    public boolean isEventReportingAllowed() {
        return dataCollectionLevel == DataCollectionLevel.USER_BEHAVIOR;
    }

    /**
     * Gives a boolean indicating whether reporting errors is allowed or not.
     *
     * @return {@code true} if error reporting is allowed, {@code false} otherwise.
     */
    public boolean isErrorReportingAllowed() {
        return dataCollectionLevel != DataCollectionLevel.OFF;
    }

    /**
     * Gives a boolean indicating whether reporting crashes is allowed or not.
     *
     * @return {@code true} if crash reporting is allowed, {@code false} otherwise.
     */
    public boolean isCrashReportingAllowed() {
        return crashReportingLevel == CrashReportingLevel.OPT_IN_CRASHES;
    }

    /**
     * Gives a boolean indicating whether identifying users is allowed or not.
     *
     * @return {@code true} if user identification is allowed, {@code false} otherwise.
     */
    public boolean isUserIdentificationAllowed() {
        return dataCollectionLevel == DataCollectionLevel.USER_BEHAVIOR;
    }
}
