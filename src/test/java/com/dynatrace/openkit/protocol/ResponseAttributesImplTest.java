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
package com.dynatrace.openkit.protocol;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseAttributesImplTest {

    @Test
    public void buildWithJsonDefaultsHasNoAttributesSetOnInstance() {
        // given
        ResponseAttributes target = ResponseAttributesImpl.withJsonDefaults().build();

        // when, then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(target.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void buildWithKeyValueDefaultsHasNoAttributeSetOnInstance() {
        // given
        ResponseAttributes target = ResponseAttributesImpl.withKeyValueDefaults().build();

        // when, then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(target.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void buildForwardsJsonDefaultsToInstance() {
        // given
        ResponseAttributesDefaults defaults = ResponseAttributesDefaults.JSON_RESPONSE;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(defaults.getMaxBeaconSizeInBytes()));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getMaxSessionDurationInMilliseconds()));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getMaxEventsPerSession()));
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(defaults.getSessionTimeoutInMilliseconds()));
        assertThat(obtained.getSendIntervalInMilliseconds(), is(defaults.getSendIntervalInMilliseconds()));
        assertThat(obtained.getVisitStoreVersion(), is(defaults.getVisitStoreVersion()));

        assertThat(obtained.isCapture(), is(defaults.isCapture()));
        assertThat(obtained.isCaptureCrashes(), is(defaults.isCaptureCrashes()));
        assertThat(obtained.isCaptureErrors(), is(defaults.isCaptureErrors()));
        assertThat(obtained.getApplicationId(), is(defaults.getApplicationId()));

        assertThat(obtained.getMultiplicity(), is(defaults.getMultiplicity()));
        assertThat(obtained.getServerId(), is(defaults.getServerId()));
        assertThat(obtained.getStatus(), is(defaults.getStatus()));

        assertThat(obtained.getTimestampInMilliseconds(), is(defaults.getTimestampInMilliseconds()));
    }

    @Test
    public void buildForwardsKeyValueDefaultsToInstance() {
        // given
        ResponseAttributesDefaults defaults = ResponseAttributesDefaults.JSON_RESPONSE;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(defaults.getMaxBeaconSizeInBytes()));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getMaxSessionDurationInMilliseconds()));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getMaxEventsPerSession()));
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(defaults.getSessionTimeoutInMilliseconds()));
        assertThat(obtained.getSendIntervalInMilliseconds(), is(defaults.getSendIntervalInMilliseconds()));
        assertThat(obtained.getVisitStoreVersion(), is(defaults.getVisitStoreVersion()));

        assertThat(obtained.isCapture(), is(defaults.isCapture()));
        assertThat(obtained.isCaptureCrashes(), is(defaults.isCaptureCrashes()));
        assertThat(obtained.isCaptureErrors(), is(defaults.isCaptureErrors()));
        assertThat(obtained.getApplicationId(), is(defaults.getApplicationId()));

        assertThat(obtained.getMultiplicity(), is(defaults.getMultiplicity()));
        assertThat(obtained.getServerId(), is(defaults.getServerId()));
        assertThat(obtained.getStatus(), is(defaults.getStatus()));

        assertThat(obtained.getTimestampInMilliseconds(), is(defaults.getTimestampInMilliseconds()));
    }

    @Test
    public void buildPropagatesMaxBeaconSizeToInstance() {
        // given
        int beaconSize = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMaxBeaconSizeInBytes(beaconSize).build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void withMaxBeaconSizeSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_BEACON_SIZE;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMaxBeaconSizeInBytes(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesMaxSessionDurationToInstance() {
        // given
        int sessionDuration = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMaxSessionDurationInMilliseconds(sessionDuration).build();

        // then
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void withMaxSessionDurationSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_SESSION_DURATION;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMaxSessionDurationInMilliseconds(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesMaxEventsPerSessionToInstance() {
        // given
        int eventsPerSession = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMaxEventsPerSession(eventsPerSession).build();

        // then
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void withMaxEventsPerSessionSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_EVENTS_PER_SESSION;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMaxEventsPerSession(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesSessionTimeoutToInstance() {
        // given
        int sessionTimeout = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withSessionTimeoutInMilliseconds(sessionTimeout).build();

        // then
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void withSessionTimeoutSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SESSION_TIMEOUT;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withSessionTimeoutInMilliseconds(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesSendIntervalToInstance() {
        // given
        int sendInterval = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withSendIntervalInMilliseconds(sendInterval).build();

        // then
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void withSendIntervalSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SEND_INTERVAL;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withSendIntervalInMilliseconds(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesVisitStoreVersionToInstance() {
        // given
        int visitStoreVersion = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withVisitStoreVersion(visitStoreVersion).build();

        // then
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void withVisitStoreVersionSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.VISIT_STORE_VERSION;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withVisitStoreVersion(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesIsCaptureToInstance() {
        // given
        boolean isCapture = !ResponseAttributesDefaults.JSON_RESPONSE.isCapture();
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withCapture(isCapture).build();

        // then
        assertThat(obtained.isCapture(), is(isCapture));
    }

    @Test
    public void withCaptureSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withCapture(!ResponseAttributesDefaults.JSON_RESPONSE.isCapture()).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesIsCaptureCrashesToInstance() {
        // given
        boolean isCaptureCrashes = !ResponseAttributesDefaults.JSON_RESPONSE.isCaptureCrashes();
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withCaptureCrashes(isCaptureCrashes).build();

        // then
        assertThat(obtained.isCaptureCrashes(), is(isCaptureCrashes));
    }

    @Test
    public void withCaptureCrashesSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE_CRASHES;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withCaptureCrashes(!ResponseAttributesDefaults.JSON_RESPONSE.isCaptureCrashes()).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesIsCaptureErrorsToInstance() {
        // given
        boolean isCaptureErrors = !ResponseAttributesDefaults.JSON_RESPONSE.isCaptureErrors();
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withCaptureErrors(isCaptureErrors).build();

        // then
        assertThat(obtained.isCaptureErrors(), is(isCaptureErrors));
    }

    @Test
    public void withCaptureErrorsSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE_ERRORS;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withCaptureErrors(!ResponseAttributesDefaults.JSON_RESPONSE.isCaptureErrors()).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesApplicationIdToInstance() {
        // given
        String applicationId = UUID.randomUUID().toString();
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withApplicationId(applicationId).build();

        // then
        assertThat(obtained.getApplicationId(), is(equalTo(applicationId)));
    }

    @Test
    public void withApplicationIdSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.APPLICATION_ID;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withApplicationId(UUID.randomUUID().toString()).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesMultiplicityToInstance() {
        // given
        int multiplicity = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMultiplicity(multiplicity).build();

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void withMultiplicitySetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MULTIPLICITY;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withMultiplicity(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesServerIdToInstance() {
        // given
        int serverId = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withServerId(serverId).build();

        // then
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void withServerIdSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SERVER_ID;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withServerId(37).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesStatusToInstance() {
        // given
        String status = "status";
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withStatus(status).build();

        // then
        assertThat(obtained.getStatus(), is(equalTo(status)));
    }

    @Test
    public void withStatusSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.STATUS;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withStatus("status").build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    @Test
    public void buildPropagatesTimestampToInstance() {
        // given
        long timestamp = 73;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withTimestampInMilliseconds(timestamp).build();

        // then
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void withTimestampSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.TIMESTAMP;
        ResponseAttributesImpl.Builder target = ResponseAttributesImpl.withJsonDefaults();

        // when
        ResponseAttributes obtained = target.withTimestampInMilliseconds(37L).build();

        // then
        assertThat(obtained.isAttributeSet(attribute), is(true));

        for (ResponseAttribute unsetAttribute : ResponseAttribute.values()) {
            if (attribute == unsetAttribute) {
                continue;
            }
            assertThat(obtained.isAttributeSet(unsetAttribute), is(false));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// merge response
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void mergingDefaultResponsesReturnsResponseWithoutAnyAttributeSet() {
        // given
        ResponseAttributes toMerge = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withJsonDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(toMerge);

        // then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(obtained.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void mergeResponseWithAllValuesSetToDefaultResponse() {
        // given
        ResponseAttributes toMerge = mock(ResponseAttributes.class);
        when(toMerge.isAttributeSet(any(ResponseAttribute.class))).thenReturn(true);
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(toMerge);

        // then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(obtained.isAttributeSet(attribute), is(true));
        }
    }

    @Test
    public void mergeTakesBeaconSizeFromMergeTargetIfNotSetInSource() {
        // given
        int beaconSize = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(beaconSize).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeTakesBeaconSizeFromMergeSourceIfSetInSource() {
        // given
        int beaconSize = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(beaconSize).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeTakesBeaconSizeFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int beaconSize = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(beaconSize).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeTakesSessionDurationFromMergeTargetIfNotSetInSource() {
        // given
        int sessionDuration = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesSessionDurationFromMergeSourceIfSetInSource() {
        // given
        int sessionDuration = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesSessionDurationFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int sessionDuration = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesEventsPerSessionFromMergeTargetIfNotSetInSource() {
        // given
        int eventsPerSession = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(eventsPerSession).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesEventsPerSessionFromMergeSourceIfSetInSource() {
        // given
        int eventsPerSession = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(eventsPerSession).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesEventsPerSessionFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int eventsPerSession = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(eventsPerSession).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesSessionTimeoutFromMergeTargetIfNotSetInSource() {
        // given
        int sessionTimeout = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesSessionTimeoutFromMergeSourceIfSetInSource() {
        // given
        int sessionTimeout = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesSessionTimeoutFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int sessionTimeout = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesSendIntervalFromMergeTargetIfNotSetInSource() {
        // given
        int sendInterval = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesSendIntervalFromMergeSourceIfSetInSource() {
        // given
        int sendInterval = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesSendIntervalFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int sendInterval = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesVisitStoreVersionFromMergeTargetIfNotSetInSource() {
        // given
        int visitStoreVersion = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withVisitStoreVersion(visitStoreVersion).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void mergeTakesVisitStoreVersionFromMergeSourceIfSetInSource() {
        // given
        int visitStoreVersion = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withVisitStoreVersion(visitStoreVersion).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void mergeTakesVisitStoreVersionFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int visitStoreVersion = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withVisitStoreVersion(visitStoreVersion).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withVisitStoreVersion(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void mergeTakesCaptureFromMergeTargetIfNotSetInSource() {
        // given
        boolean capture = !ResponseAttributesDefaults.UNDEFINED.isCapture();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withCapture(capture).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(capture));
    }

    @Test
    public void mergeTakesCaptureFromMergeSourceIfSetInSource() {
        // given
        boolean capture = !ResponseAttributesDefaults.UNDEFINED.isCapture();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withCapture(capture).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(capture));
    }

    @Test
    public void mergeTakesCaptureFromMergeSourceIfSetInSourceAndTarget() {
        // given
        boolean capture = !ResponseAttributesDefaults.UNDEFINED.isCapture();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withCapture(capture).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withCapture(!capture).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(capture));
    }

    @Test
    public void mergeTakesCaptureCrashesFromMergeTargetIfNotSetInSource() {
        // given
        boolean captureCrashes = !ResponseAttributesDefaults.UNDEFINED.isCaptureCrashes();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureCrashes(captureCrashes).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(captureCrashes));
    }

    @Test
    public void mergeTakesCaptureCrashesFromMergeSourceIfSetInSource() {
        // given
        boolean captureCrashes = !ResponseAttributesDefaults.UNDEFINED.isCaptureCrashes();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureCrashes(captureCrashes).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(captureCrashes));
    }

    @Test
    public void mergeTakesCaptureCrashesFromMergeSourceIfSetInSourceAndTarget() {
        // given
        boolean captureCrashes = !ResponseAttributesDefaults.UNDEFINED.isCaptureCrashes();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureCrashes(captureCrashes).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureCrashes(!captureCrashes).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(captureCrashes));
    }

    @Test
    public void mergeTakesCaptureErrorsFromMergeTargetIfNotSetInSource() {
        // given
        boolean captureErrors = !ResponseAttributesDefaults.UNDEFINED.isCaptureErrors();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureErrors(captureErrors).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(captureErrors));
    }

    @Test
    public void mergeTakesCaptureErrorsFromMergeSourceIfSetInSource() {
        // given
        boolean captureErrors = !ResponseAttributesDefaults.UNDEFINED.isCaptureErrors();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureErrors(captureErrors).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(captureErrors));
    }

    @Test
    public void mergeTakesCaptureErrorsFromMergeSourceIfSetInSourceAndTarget() {
        // given
        boolean captureErrors = !ResponseAttributesDefaults.UNDEFINED.isCaptureErrors();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureErrors(captureErrors).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withCaptureErrors(!captureErrors).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(captureErrors));
    }

    @Test
    public void mergeTakesApplicationIdFromMergeTargetIfNotSetInSource() {
        // given
        String applicationId = UUID.randomUUID().toString();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withApplicationId(applicationId).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getApplicationId(), is(equalTo(applicationId)));
    }

    @Test
    public void mergeTakesApplicationIdFromMergeSourceIfSetInSource() {
        // given
        String applicationId = UUID.randomUUID().toString();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withApplicationId(applicationId).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getApplicationId(), is(equalTo(applicationId)));
    }

    @Test
    public void mergeTakesApplicationIdFromMergeSourceIfSetInSourceAndTarget() {
        // given
        String applicationId = UUID.randomUUID().toString();
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withApplicationId(applicationId).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withApplicationId(UUID.randomUUID().toString()).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getApplicationId(), is(equalTo(applicationId)));
    }

    @Test
    public void mergeTakesMultiplicityFromMergeTargetIfNotSetInSource() {
        // given
        int multiplicity = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withMultiplicity(multiplicity).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeTakesMultiplicityFromMergeSourceIfSetInSource() {
        // given
        int multiplicity = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withMultiplicity(multiplicity).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeTakesMultiplicityFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int multiplicity = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withMultiplicity(multiplicity).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withMultiplicity(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeTakesServerIdFromMergeTargetIfNotSetInSource() {
        // given
        int serverId = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withServerId(serverId).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void mergeTakesServerIdFromMergeSourceIfSetInSource() {
        // given
        int serverId = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withServerId(serverId).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void mergeTakesServerIdFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int serverId = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withServerId(serverId).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withServerId(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void mergeTakesStatusFromMergeTargetIfNotSetInSource() {
        // given
        String status = "status";
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withStatus(status).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getStatus(), is(equalTo(status)));
    }

    @Test
    public void mergeTakesStatusFromMergeSourceIfSetInSource() {
        // given
        String status = "status";
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withStatus(status).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getStatus(), is(equalTo(status)));
    }

    @Test
    public void mergeTakesStatusFromMergeSourceIfSetInSourceAndTarget() {
        // given
        String status = "status";
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withStatus(status).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                                                          .withStatus("foobar").build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getStatus(), is(equalTo(status)));
    }

    @Test
    public void mergeTakesTimestampFromMergeTargetIfNotSetInSource() {
        // given
        long timestamp = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults().build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void mergeTakesTimestampFromMergeSourceIfSetInSource() {
        // given
        long timestamp = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults().build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void mergeTakesTimestampFromMergeSourceIfSetInSourceAndTarget() {
        // given
        long timestamp = 73;
        ResponseAttributes source = ResponseAttributesImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();
        ResponseAttributes target = ResponseAttributesImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(37).build();

        // when
        ResponseAttributes obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }
}
