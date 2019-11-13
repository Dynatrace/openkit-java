/**
 * Copyright 2018-2019 Dynatrace LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dynatrace.openkit.protocol;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResponseImplTest {

    @Test
    public void buildWithJsonDefaultsHasNoAttributesSetOnInstance() {
        // given
        Response target = ResponseImpl.Builder.withJsonDefaults().build();

        // when, then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(target.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void buildWithKeyValueDefaultsHasNoAttributeSetOnInstance() {
        // given
        ResponseDefaults defaults = ResponseDefaults.JSON_RESPONSE;
        Response target = ResponseImpl.Builder.withKeyValueDefaults().build();

        // when, then
        for (ResponseAttribute attribute : ResponseAttribute.values()) {
            assertThat(target.isAttributeSet(attribute), is(false));
        }
    }

    @Test
    public void buildForwardsJsonDefaultsToInstance() {
        // given
        ResponseDefaults defaults = ResponseDefaults.JSON_RESPONSE;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(defaults.getBeaconSizeInBytes()));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getSessionDurationInMilliseconds()));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getEventsPerSession()));
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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(defaults.getBeaconSizeInBytes()));
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(defaults.getSessionDurationInMilliseconds()));
        assertThat(obtained.getMaxEventsPerSession(), is(defaults.getEventsPerSession()));
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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withMaxBeaconSizeInBytes(beaconSize).build();

        // then
        assertThat(obtained.getMaxBeaconSizeInBytes(), is(beaconSize));
    }

    @Test
    public void withMaxBeaconSizeSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_BEACON_SIZE;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withMaxSessionDurationInMilliseconds(sessionDuration).build();

        // then
        assertThat(obtained.getMaxSessionDurationInMilliseconds(), is(sessionDuration));
    }

    @Test
    public void withMaxSessionDurationSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_SESSION_DURATION;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withMaxEventsPerSession(eventsPerSession).build();

        // then
        assertThat(obtained.getMaxEventsPerSession(), is(eventsPerSession));
    }

    @Test
    public void withMaxEventsPerSessionSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MAX_EVENTS_PER_SESSION;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withSessionTimeoutInMilliseconds(sessionTimeout).build();

        // then
        assertThat(obtained.getSessionTimeoutInMilliseconds(), is(sessionTimeout));
    }

    @Test
    public void withSessionTimeoutSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SESSION_TIMEOUT;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withSendIntervalInMilliseconds(sendInterval).build();

        // then
        assertThat(obtained.getSendIntervalInMilliseconds(), is(sendInterval));
    }

    @Test
    public void withSendIntervalSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SEND_INTERVAL;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withVisitStoreVersion(visitStoreVersion).build();

        // then
        assertThat(obtained.getVisitStoreVersion(), is(visitStoreVersion));
    }

    @Test
    public void withVisitStoreVersionSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.VISIT_STORE_VERSION;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withCapture(isCapture).build();

        // then
        assertThat(obtained.isCapture(), is(isCapture));
    }

    @Test
    public void withCaptureSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withCaptureCrashes(isCaptureCrashes).build();

        // then
        assertThat(obtained.isCaptureCrashes(), is(isCaptureCrashes));
    }

    @Test
    public void withCaptureCrashesSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE_CRASHES;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withCaptureErrors(isCaptureErrors).build();

        // then
        assertThat(obtained.isCaptureErrors(), is(isCaptureErrors));
    }

    @Test
    public void withCaptureErrorsSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.IS_CAPTURE_ERRORS;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withMultiplicity(multiplicity).build();

        // then
        assertThat(obtained.getMultiplicity(), is(multiplicity));
    }

    @Test
    public void withMultiplicitySetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.MULTIPLICITY;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withServerId(serverId).build();

        // then
        assertThat(obtained.getServerId(), is(serverId));
    }

    @Test
    public void withServerIdSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.SERVER_ID;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

        // when
        Response obtained = target.withTimestampInMilliseconds(timestamp).build();

        // then
        assertThat(obtained.getTimestampInMilliseconds(), is(timestamp));
    }

    @Test
    public void withTimestampSetsAttributeOnInstance() {
        // given
        ResponseAttribute attribute = ResponseAttribute.TIMESTAMP;
        ResponseImpl.Builder target = ResponseImpl.Builder.withJsonDefaults();

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
}
