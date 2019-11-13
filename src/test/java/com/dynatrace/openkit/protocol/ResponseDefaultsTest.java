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

public class ResponseDefaultsTest {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Json response tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void defaultJsonBeaconSize() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getBeaconSizeInBytes(), is(150 * 1024)); // 150 kB
    }

    @Test
    public void defaultJsonSessionDuration() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getSessionDurationInMilliseconds(), is(360 * 60 * 1000)); // 360 minutes
    }

    @Test
    public void defaultJsonEventsPerSession() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getEventsPerSession(), is(200));
    }

    @Test
    public void defaultJsonSessionTimeout() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getSessionTimeoutInMilliseconds(), is(600 * 1000)); // 600 sec
    }

    @Test
    public void defaultJsonSendInterval() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getSendIntervalInMilliseconds(), is(120 * 1000)); // 120 sec
    }

    @Test
    public void defaultJsonVisitStoreVersion() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getVisitStoreVersion(), is(1));
    }

    @Test
    public void defaultJsonIsCapture() {
        assertThat(ResponseDefaults.JSON_RESPONSE.isCapture(), is(true));
    }

    @Test
    public void defaultJsonIsCaptureCrashes() {
        assertThat(ResponseDefaults.JSON_RESPONSE.isCaptureCrashes(), is(true));
    }

    @Test
    public void defaultJsonIsCaptureErrors() {
        assertThat(ResponseDefaults.JSON_RESPONSE.isCaptureErrors(), is(true));
    }

    @Test
    public void defaultJsonMultiplicity() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getMultiplicity(), is(1));
    }

    @Test
    public void defaultJsonServerId() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getServerId(), is(1));
    }

    @Test
    public void defaultJsonTimestamp() {
        assertThat(ResponseDefaults.JSON_RESPONSE.getTimestampInMilliseconds(), is(0L));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// Key value pair response tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void defaultKeyValueBeaconSize() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getBeaconSizeInBytes(), is(30 * 1024)); // 30 kB
    }

    @Test
    public void defaultKeyValueSessionDuration() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getSessionDurationInMilliseconds(), is(-1)); // not set
    }

    @Test
    public void defaultKeyValueEventsPerSession() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getEventsPerSession(), is(-1)); // not set
    }

    @Test
    public void defaultKeyValueSessionTimeout() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getSessionTimeoutInMilliseconds(), is(-1)); // not set
    }

    @Test
    public void defaultKeyValueSendInterval() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getSendIntervalInMilliseconds(), is(120000)); // 120 sec
    }

    @Test
    public void defaultKeyValueVisitStoreVersion() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getVisitStoreVersion(), is(1));
    }

    @Test
    public void defaultKeyValueIsCapture() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.isCapture(), is(true));
    }

    @Test
    public void defaultKeyValueIsCaptureCrashes() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.isCaptureCrashes(), is(true));
    }

    @Test
    public void defaultKeyValueIsCaptureErrors() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.isCaptureErrors(), is(true));
    }

    @Test
    public void defaultKeyValueMultiplicity() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getMultiplicity(), is(1));
    }

    @Test
    public void defaultKeyValueServerId() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getServerId(), is(1));
    }

    @Test
    public void defaultKeyValueTimestamp() {
        assertThat(ResponseDefaults.KEY_VALUE_RESPONSE.getTimestampInMilliseconds(), is(0L));
    }
}
