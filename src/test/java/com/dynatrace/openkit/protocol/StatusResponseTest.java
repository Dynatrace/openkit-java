/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.protocol;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class StatusResponseTest {

    @Test
    public void canParseValidResponse() {
        // given
        String response = "type=m&cp=1&si=2&bn=someName&id=3&bl=4&er=1&cr=1";
        int responseCode = 418;

        // when
        StatusResponse statusResponse = new StatusResponse(response, responseCode);

        // then
        assertThat(statusResponse.getResponseCode(), is(responseCode));
        assertThat(statusResponse.isCapture(), is(true));
        assertThat(statusResponse.getSendInterval(), is(2 * 1000));
        assertThat(statusResponse.getMonitorName(), is("someName"));
        assertThat(statusResponse.getServerID(), is(3));
        assertThat(statusResponse.getMaxBeaconSize(), is(4 * 1024));
        assertThat(statusResponse.isCaptureErrors(), is(true));
        assertThat(statusResponse.isCaptureCrashes(), is(true));
    }

    @Test
    public void canParseEmptyResponse() {
        // given
        String response = "";
        int responseCode = 418;

        // when
        StatusResponse statusResponse = new StatusResponse(response, responseCode);

        // then
        assertThat(statusResponse.getResponseCode(), is(responseCode));
        assertThat(statusResponse.isCapture(), is(true));
        assertThat(statusResponse.getSendInterval(), is(-1));
        assertThat(statusResponse.getMonitorName(), is(nullValue()));
        assertThat(statusResponse.getServerID(), is(-1));
        assertThat(statusResponse.getMaxBeaconSize(), is(-1));
        assertThat(statusResponse.isCaptureErrors(), is(true));
        assertThat(statusResponse.isCaptureCrashes(), is(true));
    }

    @Ignore
    @Test
    public void canParseTruncatedResponse() {
        // given: truncated before the monitor name VALUE, therefore we have a KEY without a VALUE
        String response = "type=m&cp=1&si=2&bn=";
        int responseCode = 418;

        // when
        StatusResponse statusResponse = new StatusResponse(response, responseCode);

        // then
        assertThat(statusResponse.getResponseCode(), is(responseCode));
        assertThat(statusResponse.isCapture(), is(true));
        assertThat(statusResponse.getSendInterval(), is(2 * 1000));
        assertThat(statusResponse.getMonitorName(), is(nullValue()));
        assertThat(statusResponse.getServerID(), is(-1));
        assertThat(statusResponse.getMaxBeaconSize(), is(-1));
        assertThat(statusResponse.isCaptureErrors(), is(true));
        assertThat(statusResponse.isCaptureCrashes(), is(true));
    }

}
