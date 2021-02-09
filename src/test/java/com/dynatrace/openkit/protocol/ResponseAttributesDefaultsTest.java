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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ResponseAttributesDefaultsTest {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Json response tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void defaultJsonBeaconSize() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getMaxBeaconSizeInBytes(), is(150 * 1024)); // 150 kB
    }

    @Test
    public void defaultJsonSessionDuration() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getMaxSessionDurationInMilliseconds(), is(360 * 60 * 1000)); // 360 minutes
    }

    @Test
    public void defaultJsonEventsPerSession() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getMaxEventsPerSession(), is(200));
    }

    @Test
    public void defaultJsonSessionTimeout() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getSessionTimeoutInMilliseconds(), is(600 * 1000)); // 600 sec
    }

    @Test
    public void defaultJsonSendInterval() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getSendIntervalInMilliseconds(), is(120 * 1000)); // 120 sec
    }

    @Test
    public void defaultJsonVisitStoreVersion() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getVisitStoreVersion(), is(1));
    }

    @Test
    public void defaultJsonIsCapture() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.isCapture(), is(true));
    }

    @Test
    public void defaultJsonIsCaptureCrashes() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.isCaptureCrashes(), is(true));
    }

    @Test
    public void defaultJsonIsCaptureErrors() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.isCaptureErrors(), is(true));
    }

    @Test
    public void defaultJsonTrafficControlPercentage() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getTrafficControlPercentage(), is(100));
    }

    @Test
    public void defaultJsonApplicationId() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getApplicationId(), is(nullValue()));
    }

    @Test
    public void defaultJsonMultiplicity() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getMultiplicity(), is(1));
    }

    @Test
    public void defaultJsonServerId() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getServerId(), is(1));
    }

    @Test
    public void defaultJsonStatus() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getStatus(), is(nullValue()));
    }

    @Test
    public void defaultJsonTimestamp() {
        assertThat(ResponseAttributesDefaults.JSON_RESPONSE.getTimestampInMilliseconds(), is(0L));
    }

    @Test
    public void defaultJsonMergeReturnsPassedValue() {
        // given
        ResponseAttributes responseAttributes = mock(ResponseAttributes.class);

        // when
        ResponseAttributes obtained = ResponseAttributesDefaults.JSON_RESPONSE.merge(responseAttributes);

        // then
        assertThat(obtained, sameInstance(responseAttributes));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Key value pair response tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void defaultKeyValueBeaconSize() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getMaxBeaconSizeInBytes(), is(30 * 1024)); // 30 kB
    }

    @Test
    public void defaultKeyValueSessionDuration() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getMaxSessionDurationInMilliseconds(), is(-1)); // not set
    }

    @Test
    public void defaultKeyValueEventsPerSession() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getMaxEventsPerSession(), is(-1)); // not set
    }

    @Test
    public void defaultKeyValueSessionTimeout() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getSessionTimeoutInMilliseconds(), is(-1)); // not set
    }

    @Test
    public void defaultKeyValueSendInterval() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getSendIntervalInMilliseconds(), is(120000)); // 120 sec
    }

    @Test
    public void defaultKeyValueVisitStoreVersion() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getVisitStoreVersion(), is(1));
    }

    @Test
    public void defaultKeyValueIsCapture() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.isCapture(), is(true));
    }

    @Test
    public void defaultKeyValueIsCaptureCrashes() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.isCaptureCrashes(), is(true));
    }

    @Test
    public void defaultKeyValueIsCaptureErrors() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.isCaptureErrors(), is(true));
    }

    @Test
    public void defaultKeyValueTrafficControlPercentage() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getTrafficControlPercentage(), is(100));
    }

    @Test
    public void defaultKeyValueApplicationId() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getApplicationId(), is(nullValue()));
    }

    @Test
    public void defaultKeyValueMultiplicity() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getMultiplicity(), is(1));
    }

    @Test
    public void defaultKeyValueServerId() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getServerId(), is(1));
    }

    @Test
    public void defaultKeyValueStatus() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getStatus(), is(nullValue()));
    }

    @Test
    public void defaultKeyValueTimestamp() {
        assertThat(ResponseAttributesDefaults.KEY_VALUE_RESPONSE.getTimestampInMilliseconds(), is(0L));
    }

    @Test
    public void defaultKeyValueMergeReturnsPassedValue() {
        // given
        ResponseAttributes responseAttributes = mock(ResponseAttributes.class);

        // when
        ResponseAttributes obtained = ResponseAttributesDefaults.KEY_VALUE_RESPONSE.merge(responseAttributes);

        // then
        assertThat(obtained, sameInstance(responseAttributes));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Undefined defaults tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void defaultUndefinedBeaconSize() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getMaxBeaconSizeInBytes(), is(30 * 1024));
    }

    @Test
    public void defaultUndefinedSessionDuration() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getMaxSessionDurationInMilliseconds(), is(-1)); // not set
    }

    @Test
    public void defaultUndefinedEventsPerSession() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getMaxEventsPerSession(), is(-1)); // not set
    }

    @Test
    public void defaultUndefinedSessionTimeout() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getSessionTimeoutInMilliseconds(), is(-1)); // not set
    }

    @Test
    public void defaultUndefinedSendInterval() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getSendIntervalInMilliseconds(), is(120 * 1000)); // 120 sec
    }

    @Test
    public void defaultUndefinedVisitStoreVersion() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getVisitStoreVersion(), is(1));
    }

    @Test
    public void defaultUndefinedIsCapture() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.isCapture(), is(true));
    }

    @Test
    public void defaultUndefinedIsCaptureCrashes() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.isCaptureCrashes(), is(true));
    }

    @Test
    public void defaultUndefinedIsCaptureErrors() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.isCaptureErrors(), is(true));
    }

    @Test
    public void defaultUndefinedTrafficControlPercentage() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getTrafficControlPercentage(), is(100));
    }

    @Test
    public void defaultUndefinedApplicationId() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getApplicationId(), is(nullValue()));
    }

    @Test
    public void defaultUndefinedMultiplicity() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getMultiplicity(), is(1));
    }

    @Test
    public void defaultUndefinedServerId() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getServerId(), is(-1));
    }

    @Test
    public void defaultUndefinedStatus() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getStatus(), is(nullValue()));
    }

    @Test
    public void defaultUndefinedTimestamp() {
        assertThat(ResponseAttributesDefaults.UNDEFINED.getTimestampInMilliseconds(), is(0L));
    }

    @Test
    public void defaultUndefinedMergeReturnsPassedValue() {
        // given
        ResponseAttributes responseAttributes = mock(ResponseAttributes.class);

        // when
        ResponseAttributes obtained = ResponseAttributesDefaults.UNDEFINED.merge(responseAttributes);

        // then
        assertThat(obtained, sameInstance(responseAttributes));
    }
}

