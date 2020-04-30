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
package com.dynatrace.openkit.protocol;

import java.util.EnumSet;

/**
 * Implements {@link ResponseAttributes} providing all the attributes received from the server.
 */
public class ResponseAttributesImpl implements ResponseAttributes {

    /**
     * Represents the set of attributes which are set / were sent by the server.
     */
    private final EnumSet<ResponseAttribute> setAttributes;

    private final int maxBeaconSizeInBytes;
    private final int maxSessionDurationInMilliseconds;
    private final int maxEventsPerSession;
    private final int sessionTimeoutInMilliseconds;
    private final int sendIntervalInMilliseconds;
    private final int visitStoreVersion;

    private final boolean isCapture;
    private final boolean isCaptureCrashes;
    private final boolean isCaptureErrors;
    private final String applicationId;

    private final int multiplicity;
    private final int serverId;

    private final long timestampInMilliseconds;

    private ResponseAttributesImpl(Builder builder) {
        setAttributes = EnumSet.copyOf(builder.setAttributes);

        maxBeaconSizeInBytes = builder.maxBeaconSizeInBytes;
        maxSessionDurationInMilliseconds = builder.maxSessionDurationInMilliseconds;
        maxEventsPerSession = builder.maxEventsPerSession;
        sessionTimeoutInMilliseconds = builder.sessionTimeoutInMilliseconds;
        sendIntervalInMilliseconds = builder.sendIntervalInMilliseconds;
        visitStoreVersion = builder.visitStoreVersion;

        isCapture = builder.isCapture;
        isCaptureCrashes = builder.isCaptureCrashes;
        isCaptureErrors = builder.isCaptureErrors;
        applicationId = builder.applicationId;

        multiplicity = builder.multiplicity;
        serverId = builder.serverId;

        timestampInMilliseconds = builder.timestampInMilliseconds;
    }

    @Override
    public int getMaxBeaconSizeInBytes() {
        return maxBeaconSizeInBytes;
    }

    @Override
    public int getMaxSessionDurationInMilliseconds() {
        return maxSessionDurationInMilliseconds;
    }

    @Override
    public int getMaxEventsPerSession() {
        return maxEventsPerSession;
    }

    @Override
    public int getSessionTimeoutInMilliseconds() {
        return sessionTimeoutInMilliseconds;
    }

    @Override
    public int getSendIntervalInMilliseconds() {
        return sendIntervalInMilliseconds;
    }

    @Override
    public int getVisitStoreVersion() {
        return visitStoreVersion;
    }

    @Override
    public boolean isCapture() {
        return isCapture;
    }

    @Override
    public boolean isCaptureCrashes() {
        return isCaptureCrashes;
    }

    @Override
    public boolean isCaptureErrors() {
        return isCaptureErrors;
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public int getMultiplicity() {
        return multiplicity;
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public long getTimestampInMilliseconds() {
        return timestampInMilliseconds;
    }

    @Override
    public boolean isAttributeSet(ResponseAttribute attribute) {
        return setAttributes.contains(attribute);
    }

    @Override
    public ResponseAttributes merge(ResponseAttributes responseAttributes) {
        Builder builder = new Builder(this);

        applyBeaconSize(builder, responseAttributes);
        applySessionDuration(builder, responseAttributes);
        applyEventsPerSession(builder, responseAttributes);
        applySessionTimeout(builder, responseAttributes);
        applySendInterval(builder, responseAttributes);
        applyVisitStoreVersion(builder, responseAttributes);
        applyCapture(builder, responseAttributes);
        applyCaptureCrashes(builder, responseAttributes);
        applyCaptureErrors(builder, responseAttributes);
        applyApplicationId(builder, responseAttributes);
        applyMultiplicity(builder, responseAttributes);
        applyServerId(builder, responseAttributes);
        applyTimestamp(builder, responseAttributes);

        return builder.build();
    }

    private void applyBeaconSize(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.MAX_BEACON_SIZE)) {
            return;
        }
        builder.withMaxBeaconSizeInBytes(responseAttributes.getMaxBeaconSizeInBytes());
    }

    private void applySessionDuration(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.MAX_SESSION_DURATION)) {
            return;
        }
        builder.withMaxSessionDurationInMilliseconds(responseAttributes.getMaxSessionDurationInMilliseconds());
    }

    private void applyEventsPerSession(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.MAX_EVENTS_PER_SESSION)) {
            return;
        }
        builder.withMaxEventsPerSession(responseAttributes.getMaxEventsPerSession());
    }

    private void applySessionTimeout(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.SESSION_TIMEOUT)) {
            return;
        }
        builder.withSessionTimeoutInMilliseconds(responseAttributes.getSessionTimeoutInMilliseconds());
    }

    private void applySendInterval(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.SEND_INTERVAL)) {
            return;
        }
        builder.withSendIntervalInMilliseconds(responseAttributes.getSendIntervalInMilliseconds());
    }

    private void applyVisitStoreVersion(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.VISIT_STORE_VERSION)) {
            return;
        }
        builder.withVisitStoreVersion(responseAttributes.getVisitStoreVersion());
    }

    private void applyCapture(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.IS_CAPTURE)) {
            return;
        }
        builder.withCapture(responseAttributes.isCapture());
    }

    private void applyCaptureCrashes(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.IS_CAPTURE_CRASHES)) {
            return;
        }
        builder.withCaptureCrashes(responseAttributes.isCaptureCrashes());
    }

    private void applyCaptureErrors(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.IS_CAPTURE_ERRORS)) {
            return;
        }
        builder.withCaptureErrors(responseAttributes.isCaptureErrors());
    }

    private void applyApplicationId(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.APPLICATION_ID)) {
            return;
        }
        builder.withApplicationId(responseAttributes.getApplicationId());
    }

    private void applyMultiplicity(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.MULTIPLICITY)) {
            return;
        }
        builder.withMultiplicity(responseAttributes.getMultiplicity());
    }

    private void applyServerId(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.SERVER_ID)) {
            return;
        }
        builder.withServerId(responseAttributes.getServerId());
    }

    private void applyTimestamp(Builder builder, ResponseAttributes responseAttributes) {
        if (!responseAttributes.isAttributeSet(ResponseAttribute.TIMESTAMP)) {
            return;
        }
        builder.withTimestampInMilliseconds(responseAttributes.getTimestampInMilliseconds());
    }

    /**
     * Creates a new builder initialized with the defaults value for {@link KeyValueResponseParser key-value parsing}.
     */
    public static Builder withKeyValueDefaults() {
        return new Builder(ResponseAttributesDefaults.KEY_VALUE_RESPONSE);
    }

    /**
     * Creates a new builder initialized with the default values for {@link JsonResponseParser JSON parsing}.
     */
    public static Builder withJsonDefaults() {
        return new Builder(ResponseAttributesDefaults.JSON_RESPONSE);
    }

    /**
     * Creates a new builder instance with undefined default values.
     */
    public static Builder withUndefinedDefaults() {
        return new Builder(ResponseAttributesDefaults.UNDEFINED);
    }

    public static class Builder {
        private final EnumSet<ResponseAttribute> setAttributes = EnumSet.noneOf(ResponseAttribute.class);

        private int maxBeaconSizeInBytes;
        private int maxSessionDurationInMilliseconds;
        private int maxEventsPerSession;
        private int sessionTimeoutInMilliseconds;
        private int sendIntervalInMilliseconds;
        private int visitStoreVersion;

        private boolean isCapture;
        private boolean isCaptureCrashes;
        private boolean isCaptureErrors;
        private String applicationId;

        private int multiplicity;
        private int serverId;

        private long timestampInMilliseconds;

        private Builder(ResponseAttributes defaults) {
            maxBeaconSizeInBytes = defaults.getMaxBeaconSizeInBytes();
            maxSessionDurationInMilliseconds = defaults.getMaxSessionDurationInMilliseconds();
            maxEventsPerSession = defaults.getMaxEventsPerSession();
            sessionTimeoutInMilliseconds = defaults.getSessionTimeoutInMilliseconds();
            sendIntervalInMilliseconds = defaults.getSendIntervalInMilliseconds();
            visitStoreVersion = defaults.getVisitStoreVersion();

            isCapture = defaults.isCapture();
            isCaptureCrashes = defaults.isCaptureCrashes();
            isCaptureErrors = defaults.isCaptureErrors();
            applicationId = defaults.getApplicationId();

            multiplicity = defaults.getMultiplicity();
            serverId = defaults.getServerId();

            timestampInMilliseconds = defaults.getTimestampInMilliseconds();

            for (ResponseAttribute attribute : ResponseAttribute.values()) {
                if (defaults.isAttributeSet(attribute)) {
                    setAttribute(attribute);
                }
            }
        }

        /**
         * Sets the maximum beacon size in bytes
         *
         * @param maxBeaconSizeInBytes the maximum size in bytes when sending beacon data.
         * @return {@code this}
         */
        public Builder withMaxBeaconSizeInBytes(int maxBeaconSizeInBytes) {
            this.maxBeaconSizeInBytes = maxBeaconSizeInBytes;
            setAttribute(ResponseAttribute.MAX_BEACON_SIZE);

            return this;
        }

        /**
         * Sets the maximum duration after which a session is to be split.
         *
         * @param maxSessionDurationInMilliseconds maximum duration of a session in milliseconds.
         * @return {@code this}
         */
        public Builder withMaxSessionDurationInMilliseconds(int maxSessionDurationInMilliseconds) {
            this.maxSessionDurationInMilliseconds = maxSessionDurationInMilliseconds;
            setAttribute(ResponseAttribute.MAX_SESSION_DURATION);

            return this;
        }

        /**
         * Sets the maximum number of top level actions after which a session is to be split.
         *
         * @param maxEventsPerSession maximum number of top level actions
         * @return {@code this}
         */
        public Builder withMaxEventsPerSession(int maxEventsPerSession) {
            this.maxEventsPerSession = maxEventsPerSession;
            setAttribute(ResponseAttribute.MAX_EVENTS_PER_SESSION);

            return this;
        }

        /**
         * Sets the idle timeout after which a session is to be split.
         *
         * @param sessionTimeoutInMilliseconds the maximum idle timeout of a session in milliseconds
         * @return {@code this}
         */
        public Builder withSessionTimeoutInMilliseconds(int sessionTimeoutInMilliseconds) {
            this.sessionTimeoutInMilliseconds = sessionTimeoutInMilliseconds;
            setAttribute(ResponseAttribute.SESSION_TIMEOUT);

            return this;
        }

        /**
         * Sets the send interval in milliseconds.
         *
         * @param sendIntervalInMilliseconds send interval in milliseconds.
         * @return {@code this}
         */
        public Builder withSendIntervalInMilliseconds(int sendIntervalInMilliseconds) {
            this.sendIntervalInMilliseconds = sendIntervalInMilliseconds;
            setAttribute(ResponseAttribute.SEND_INTERVAL);

            return this;
        }

        /**
         * Sets the version of the visit store that should be used.
         *
         * @param visitStoreVersion version of the visit store
         * @return {@code this}
         */
        public Builder withVisitStoreVersion(int visitStoreVersion) {
            this.visitStoreVersion = visitStoreVersion;
            setAttribute(ResponseAttribute.VISIT_STORE_VERSION);

            return this;
        }

        /**
         * Sets whether capturing is enabled/disabled.
         *
         * @param isCapture capture state
         * @return {@code this}
         */
        public Builder withCapture(boolean isCapture) {
            this.isCapture = isCapture;
            setAttribute(ResponseAttribute.IS_CAPTURE);

            return this;
        }

        /**
         * Sets whether capturing of crashes is enabled/disabled.
         *
         * @param isCaptureCrashes crash capture state
         * @return {@code this}
         */
        public Builder withCaptureCrashes(boolean isCaptureCrashes) {
            this.isCaptureCrashes = isCaptureCrashes;
            setAttribute(ResponseAttribute.IS_CAPTURE_CRASHES);

            return this;
        }

        /**
         * Sets whether capturing of errors is enabled/disabled.
         *
         * @param isCaptureErrors error capture state.
         * @return {@code this}
         */
        public Builder withCaptureErrors(boolean isCaptureErrors) {
            this.isCaptureErrors = isCaptureErrors;
            setAttribute(ResponseAttribute.IS_CAPTURE_ERRORS);

            return this;
        }

        /**
         * Set application UUID to which this configuration belongs to.
         *
         * @param applicationId application's UUID
         * @return {@code this}
         */
        public Builder withApplicationId(String applicationId) {
            this.applicationId = applicationId;
            setAttribute(ResponseAttribute.APPLICATION_ID);

            return this;
        }

        /**
         * Sets the multiplicity
         *
         * @param multiplicity multiplicity
         * @return {@code this}
         */
        public Builder withMultiplicity(int multiplicity) {
            this.multiplicity = multiplicity;
            setAttribute(ResponseAttribute.MULTIPLICITY);

            return this;
        }

        /**
         * Sets the ID of the server to which data should be sent to.
         *
         * @param serverId the ID of the server to communicate with.
         * @return {@code this}
         */
        public Builder withServerId(int serverId) {
            this.serverId = serverId;
            setAttribute(ResponseAttribute.SERVER_ID);

            return this;
        }

        /**
         * Sets the timestamp of the configuration sent by the sever.
         *
         * @param timestampInMilliseconds the timestamp of the configuration in milliseconds.
         * @return {@code this}
         */
        public Builder withTimestampInMilliseconds(long timestampInMilliseconds) {
            this.timestampInMilliseconds = timestampInMilliseconds;
            setAttribute(ResponseAttribute.TIMESTAMP);

            return this;
        }

        /**
         * Creates a new {@link ResponseAttributes} instance with all the attributes set in this builder.
         */
        public ResponseAttributes build() {
            return new ResponseAttributesImpl(this);
        }

        private void setAttribute(ResponseAttribute attribute) {
            setAttributes.add(attribute);
        }
    }
}
