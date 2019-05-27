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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PrivacyConfigurationTest {

    @Test
    public void getDataCollectionLevelReturnsLevelPassedInConstructor() {
        // when data collection level , then
        assertThat(new PrivacyConfiguration(DataCollectionLevel.OFF, CrashReportingLevel.OPT_IN_CRASHES).getDataCollectionLevel(),
            is(equalTo(DataCollectionLevel.OFF)));
        assertThat(new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(equalTo(DataCollectionLevel.PERFORMANCE)));
        assertThat(new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(equalTo(DataCollectionLevel.USER_BEHAVIOR)));
    }

    @Test
    public void getCrashReportingLevelReturnsLevelPassedInConstructor() {
        // when data collection level , then
        assertThat(new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF).getCrashReportingLevel(),
            is(equalTo(CrashReportingLevel.OFF)));
        assertThat(new PrivacyConfiguration(DataCollectionLevel.OFF, CrashReportingLevel.OPT_OUT_CRASHES).getCrashReportingLevel(),
            is(equalTo(CrashReportingLevel.OPT_OUT_CRASHES)));
        assertThat(new PrivacyConfiguration(DataCollectionLevel.OFF, CrashReportingLevel.OPT_IN_CRASHES).getCrashReportingLevel(),
            is(equalTo(CrashReportingLevel.OPT_IN_CRASHES)));
    }

    @Test
    public void sessionNumberReportingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isSessionNumberReportingAllowed(), is(true));
    }

    @Test
    public void sessionNumberReportingIsNotAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isSessionNumberReportingAllowed(), is(false));
    }

    @Test
    public void sessionNumberReportingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isSessionNumberReportingAllowed(), is(false));
    }

    @Test
    public void deviceIDSendingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
                                                               PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isDeviceIDSendingAllowed(), is(true));
    }

    @Test
    public void deviceIDSendingIsNotAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isDeviceIDSendingAllowed(), is(false));
    }

    @Test
    public void deviceIDSendingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isDeviceIDSendingAllowed(), is(false));
    }

    @Test
    public void webRequestTracingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isWebRequestTracingAllowed(), is(true));
    }

    @Test
    public void webRequestTracingIsAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isWebRequestTracingAllowed(), is(true));
    }

    @Test
    public void webRequestTracingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isWebRequestTracingAllowed(), is(false));
    }

    @Test
    public void sessionReportingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isSessionReportingAllowed(), is(true));
    }

    @Test
    public void sessionReportingIsAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isSessionReportingAllowed(), is(true));
    }

    @Test
    public void sessionReportingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isSessionReportingAllowed(), is(false));
    }

    @Test
    public void actionReportingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isActionReportingAllowed(), is(true));
    }

    @Test
    public void actionReportingIsAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isActionReportingAllowed(), is(true));
    }

    @Test
    public void actionReportingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isActionReportingAllowed(), is(false));
    }

    @Test
    public void valueReportingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isValueReportingAllowed(), is(true));
    }

    @Test
    public void valueReportingIsNotAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isValueReportingAllowed(), is(false));
    }

    @Test
    public void valueReportingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isValueReportingAllowed(), is(false));
    }

    @Test
    public void eventReportingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isEventReportingAllowed(), is(true));
    }

    @Test
    public void eventReportingIsNotAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isEventReportingAllowed(), is(false));
    }

    @Test
    public void eventReportingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isEventReportingAllowed(), is(false));
    }

    @Test
    public void errorReportingIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isErrorReportingAllowed(), is(true));
    }

    @Test
    public void errorReportingIsAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isErrorReportingAllowed(), is(true));
    }

    @Test
    public void errorReportingIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isErrorReportingAllowed(), is(false));
    }

    @Test
    public void crashReportingIsAllowedIfCrashReportingLevelIsEqualToOptInCrashes() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(PrivacyConfiguration.DEFAULT_DATA_COLLECTION_LEVEL,
            CrashReportingLevel.OPT_IN_CRASHES);

        // when, then
        assertThat(target.isCrashReportingAllowed(), is(true));
    }

    @Test
    public void crashReportingIsNotAllowedIfCrashReportingLevelIsEqualToOptOutCrashes() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(PrivacyConfiguration.DEFAULT_DATA_COLLECTION_LEVEL,
            CrashReportingLevel.OPT_OUT_CRASHES);

        // when, then
        assertThat(target.isCrashReportingAllowed(), is(false));
    }

    @Test
    public void crashReportingIsNotAllowedIfCrashReportingLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(PrivacyConfiguration.DEFAULT_DATA_COLLECTION_LEVEL,
            CrashReportingLevel.OFF);

        // when, then
        assertThat(target.isCrashReportingAllowed(), is(false));
    }

    @Test
    public void userIdentificationIsAllowedIfDataCollectionLevelIsEqualToUserBehavior() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.USER_BEHAVIOR,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isUserIdentificationAllowed(), is(true));
    }

    @Test
    public void userIdentificationIsNotAllowedIfDataCollectionLevelIsEqualToPerformance() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.PERFORMANCE,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isUserIdentificationAllowed(), is(false));
    }

    @Test
    public void userIdentificationIsNotAllowedIfDataCollectionLevelIsEqualToOff() {
        // given
        PrivacyConfiguration target = new PrivacyConfiguration(DataCollectionLevel.OFF,
            PrivacyConfiguration.DEFAULT_CRASH_REPORTING_LEVEL);

        // when, then
        assertThat(target.isUserIdentificationAllowed(), is(false));
    }
}
