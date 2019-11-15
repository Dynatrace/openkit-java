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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseImplTest {

    @Test
    public void buildWithJsonDefaultsHasNoAttributesSetOnInstance() {
        // given
        Response target = ResponseImpl.withJsonDefaults().build();

        // when, then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(target.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void buildWithKeyValueDefaultsHasNoAttributeSetOnInstance() {
        // given
        ResponseDefaults defaults = ResponseDefaults.JSON_RESPONSE;
        Response target = ResponseImpl.withKeyValueDefaults().build();

        // when, then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(target.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void buildForwardsJsonDefaultsToInstance() {
        // given
        ResponseDefaults defaults = ResponseDefaults.JSON_RESPONSE;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.build();

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

        assertThat(obtained.getMultiplicity(), is(defaults.getMultiplicity()));
        assertThat(obtained.getServerId(), is(defaults.getServerId()));

        assertThat(obtained.getTimestampInMilliseconds(), is(defaults.getTimestampInMilliseconds()));
    }

    @Test
    public void buildForwardsKeyValueDefaultsToInstance() {
        // given
        ResponseDefaults defaults = ResponseDefaults.JSON_RESPONSE;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.build();

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

        assertThat(obtained.getMultiplicity(), is(defaults.getMultiplicity()));
        assertThat(obtained.getServerId(), is(defaults.getServerId()));

        assertThat(obtained.getTimestampInMilliseconds(), is(defaults.getTimestampInMilliseconds()));
    }

    @Test
    public void buildPropagatesMaxBeaconSizeToInstance() {
        // given
        int beaconSize = 73;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMaxBeaconSizeInBytes(beaconSize).build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void withMaxBeaconSizeSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_BEACON_SIZE;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMaxBeaconSizeInBytes(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMaxSessionDurationInMilliseconds(sessionDuration).build();

        // then
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void withMaxSessionDurationSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_SESSION_DURATION;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMaxSessionDurationInMilliseconds(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMaxEventsPerSession(eventsPerSession).build();

        // then
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void withMaxEventsPerSessionSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_EVENTS_PER_SESSION;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMaxEventsPerSession(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withSessionTimeoutInMilliseconds(sessionTimeout).build();

        // then
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void withSessionTimeoutSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SESSION_TIMEOUT;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withSessionTimeoutInMilliseconds(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withSendIntervalInMilliseconds(sendInterval).build();

        // then
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void withSendIntervalSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SEND_INTERVAL;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withSendIntervalInMilliseconds(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withVisitStoreVersion(visitStoreVersion).build();

        // then
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void withVisitStoreVersionSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.VISIT_STORE_VERSION;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withVisitStoreVersion(37).build();

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
        boolean isCapture = !ResponseDefaults.JSON_RESPONSE.isCapture();
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withCapture(isCapture).build();

        // then
        assertThat(obtained.isCapture(), is(isCapture));
    }

    @Test
    public void withCaptureSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withCapture(!ResponseDefaults.JSON_RESPONSE.isCapture()).build();

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
        boolean isCaptureCrashes = !ResponseDefaults.JSON_RESPONSE.isCaptureCrashes();
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withCaptureCrashes(isCaptureCrashes).build();

        // then
        assertThat(obtained.isCaptureCrashes(), is(isCaptureCrashes));
    }

    @Test
    public void withCaptureCrashesSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE_CRASHES;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withCaptureCrashes(!ResponseDefaults.JSON_RESPONSE.isCaptureCrashes()).build();

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
        boolean isCaptureErrors = !ResponseDefaults.JSON_RESPONSE.isCaptureErrors();
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withCaptureErrors(isCaptureErrors).build();

        // then
        assertThat(obtained.isCaptureErrors(), is(isCaptureErrors));
    }

    @Test
    public void withCaptureErrorsSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE_ERRORS;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withCaptureErrors(!ResponseDefaults.JSON_RESPONSE.isCaptureErrors()).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMultiplicity(multiplicity).build();

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void withMultiplicitySetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MULTIPLICITY;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withMultiplicity(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withServerId(serverId).build();

        // then
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void withServerIdSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SERVER_ID;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withServerId(37).build();

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
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withTimestampInMilliseconds(timestamp).build();

        // then
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void withTimestampSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.TIMESTAMP;
        ResponseImpl.Builder target = ResponseImpl.withJsonDefaults();

        // when
        Response obtained = target.withTimestampInMilliseconds(37L).build();

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
        Response toMerge = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withJsonDefaults().build();

        // when
        Response obtained = target.merge(toMerge);

        // then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(obtained.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void mergeResponseWithAllValuesSetToDefaultResponse() {
        // given
        Response toMerge = mock(Response.class);
        when(toMerge.isAttributeSet(any(ResponseAttribute.class))).thenReturn(true);
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(toMerge);

        // then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(obtained.isAttributeSet(attribute), is(true));
        }
    }

    @Test
    public void mergeTakesBeaconSizeFromMergeTargetIfNotSetInSource() {
        // given
        int beaconSize = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(beaconSize).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeTakesBeaconSizeFromMergeSourceIfSetInSource() {
        // given
        int beaconSize = 73;
        Response source = ResponseImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(beaconSize).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeTakesBeaconSizeFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int beaconSize = 73;
        Response source = ResponseImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(beaconSize).build();
        Response target = ResponseImpl.withUndefinedDefaults().withMaxBeaconSizeInBytes(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void mergeTakesSessionDurationFromMergeTargetIfNotSetInSource() {
        // given
        int sessionDuration = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesSessionDurationFromMergeSourceIfSetInSource() {
        // given
        int sessionDuration = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesSessionDurationFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int sessionDuration = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(sessionDuration).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withMaxSessionDurationInMilliseconds(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void mergeTakesEventsPerSessionFromMergeTargetIfNotSetInSource() {
        // given
        int eventsPerSession = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(eventsPerSession).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesEventsPerSessionFromMergeSourceIfSetInSource() {
        // given
        int eventsPerSession = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(eventsPerSession).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesEventsPerSessionFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int eventsPerSession = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(eventsPerSession).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withMaxEventsPerSession(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void mergeTakesSessionTimeoutFromMergeTargetIfNotSetInSource() {
        // given
        int sessionTimeout = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesSessionTimeoutFromMergeSourceIfSetInSource() {
        // given
        int sessionTimeout = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesSessionTimeoutFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int sessionTimeout = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(sessionTimeout).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withSessionTimeoutInMilliseconds(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void mergeTakesSendIntervalFromMergeTargetIfNotSetInSource() {
        // given
        int sendInterval = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesSendIntervalFromMergeSourceIfSetInSource() {
        // given
        int sendInterval = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesSendIntervalFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int sendInterval = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(sendInterval).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withSendIntervalInMilliseconds(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void mergeTakesVisitStoreVersionFromMergeTargetIfNotSetInSource() {
        // given
        int visitStoreVersion = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withVisitStoreVersion(visitStoreVersion).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void mergeTakesVisitStoreVersionFromMergeSourceIfSetInSource() {
        // given
        int visitStoreVersion = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withVisitStoreVersion(visitStoreVersion).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void mergeTakesVisitStoreVersionFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int visitStoreVersion = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withVisitStoreVersion(visitStoreVersion).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withVisitStoreVersion(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void mergeTakesCaptureFromMergeTargetIfNotSetInSource() {
        // given
        boolean capture = !ResponseDefaults.UNDEFINED.isCapture();
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withCapture(capture).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(capture));
    }

    @Test
    public void mergeTakesCaptureFromMergeSourceIfSetInSource() {
        // given
        boolean capture = !ResponseDefaults.UNDEFINED.isCapture();
        Response source = ResponseImpl.withUndefinedDefaults()
                .withCapture(capture).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(capture));
    }

    @Test
    public void mergeTakesCaptureFromMergeSourceIfSetInSourceAndTarget() {
        // given
        boolean capture = !ResponseDefaults.UNDEFINED.isCapture();
        Response source = ResponseImpl.withUndefinedDefaults()
                .withCapture(capture).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withCapture(!capture).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCapture(), is(capture));
    }

    @Test
    public void mergeTakesCaptureCrashesFromMergeTargetIfNotSetInSource() {
        // given
        boolean captureCrashes = !ResponseDefaults.UNDEFINED.isCaptureCrashes();
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withCaptureCrashes(captureCrashes).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(captureCrashes));
    }

    @Test
    public void mergeTakesCaptureCrashesFromMergeSourceIfSetInSource() {
        // given
        boolean captureCrashes = !ResponseDefaults.UNDEFINED.isCaptureCrashes();
        Response source = ResponseImpl.withUndefinedDefaults()
                .withCaptureCrashes(captureCrashes).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(captureCrashes));
    }

    @Test
    public void mergeTakesCaptureCrashesFromMergeSourceIfSetInSourceAndTarget() {
        // given
        boolean captureCrashes = !ResponseDefaults.UNDEFINED.isCaptureCrashes();
        Response source = ResponseImpl.withUndefinedDefaults()
                .withCaptureCrashes(captureCrashes).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withCaptureCrashes(!captureCrashes).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureCrashes(), is(captureCrashes));
    }

    @Test
    public void mergeTakesCaptureErrorsFromMergeTargetIfNotSetInSource() {
        // given
        boolean captureErrors = !ResponseDefaults.UNDEFINED.isCaptureErrors();
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withCaptureErrors(captureErrors).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(captureErrors));
    }

    @Test
    public void mergeTakesCaptureErrorsFromMergeSourceIfSetInSource() {
        // given
        boolean captureErrors = !ResponseDefaults.UNDEFINED.isCaptureErrors();
        Response source = ResponseImpl.withUndefinedDefaults()
                .withCaptureErrors(captureErrors).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(captureErrors));
    }

    @Test
    public void mergeTakesCaptureErrorsFromMergeSourceIfSetInSourceAndTarget() {
        // given
        boolean captureErrors = !ResponseDefaults.UNDEFINED.isCaptureErrors();
        Response source = ResponseImpl.withUndefinedDefaults()
                .withCaptureErrors(captureErrors).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withCaptureErrors(!captureErrors).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.isCaptureErrors(), is(captureErrors));
    }

    @Test
    public void mergeTakesMultiplicityFromMergeTargetIfNotSetInSource() {
        // given
        int multiplicity = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withMultiplicity(multiplicity).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeTakesMultiplicityFromMergeSourceIfSetInSource() {
        // given
        int multiplicity = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withMultiplicity(multiplicity).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeTakesMultiplicityFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int multiplicity = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withMultiplicity(multiplicity).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withMultiplicity(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void mergeTakesServerIdFromMergeTargetIfNotSetInSource() {
        // given
        int serverId = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withServerId(serverId).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void mergeTakesServerIdFromMergeSourceIfSetInSource() {
        // given
        int serverId = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withServerId(serverId).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void mergeTakesServerIdFromMergeSourceIfSetInSourceAndTarget() {
        // given
        int serverId = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withServerId(serverId).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withServerId(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void mergeTakesTimestampFromMergeTargetIfNotSetInSource() {
        // given
        long timestamp = 73;
        Response source = ResponseImpl.withUndefinedDefaults().build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void mergeTakesTimestampFromMergeSourceIfSetInSource() {
        // given
        long timestamp = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();
        Response target = ResponseImpl.withUndefinedDefaults().build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void mergeTakesTimestampFromMergeSourceIfSetInSourceAndTarget() {
        // given
        long timestamp = 73;
        Response source = ResponseImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(timestamp).build();
        Response target = ResponseImpl.withUndefinedDefaults()
                .withTimestampInMilliseconds(37).build();

        // when
        Response obtained = target.merge(source);

        // then
        assertThat(obtained, notNullValue());
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }
}
