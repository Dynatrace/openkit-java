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

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BeaconConfigurationTest {

    @Test
    public void getDataCollectionLevel() {

        // then
        assertThat(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(DataCollectionLevel.OFF));
        assertThat(new BeaconConfiguration(1, DataCollectionLevel.PERFORMANCE, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(DataCollectionLevel.PERFORMANCE));
        assertThat(new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OFF).getDataCollectionLevel(),
            is(DataCollectionLevel.USER_BEHAVIOR));
    }

    @Test
    public void getCrashReportingLevel() {

        // then
        assertThat(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OFF).getCrashReportingLevel(),
            is(CrashReportingLevel.OFF));
        assertThat(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OPT_OUT_CRASHES).getCrashReportingLevel(),
            is(CrashReportingLevel.OPT_OUT_CRASHES));
        assertThat(new BeaconConfiguration(1, DataCollectionLevel.OFF, CrashReportingLevel.OPT_IN_CRASHES).getCrashReportingLevel(),
            is(CrashReportingLevel.OPT_IN_CRASHES));
    }

    @Test
    public void getMultiplicityReturnsMultiplicitySetInConstructor() {

        // when multiplicity is positive, then
        assertThat(new BeaconConfiguration(4,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .getMultiplicity(), is(equalTo(4)));

        // when multiplicity is zero, then
        assertThat(new BeaconConfiguration(0,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .getMultiplicity(), is(equalTo(0)));

        // when multiplicity is negative, then
        assertThat(new BeaconConfiguration(-3,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .getMultiplicity(), is(equalTo(-3)));
    }

    @Test
    public void capturingIsAllowedWhenMultiplicityIsGreaterThanZero() {

        // when, then
        assertThat(new BeaconConfiguration(1,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .isCapturingAllowed(), is(true));

        assertThat(new BeaconConfiguration(2,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .isCapturingAllowed(), is(true));

        assertThat(new BeaconConfiguration(Integer.MAX_VALUE,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .isCapturingAllowed(), is(true));
    }

    @Test
    public void capturingIsDisallowedWhenMultiplicityIsLessThanOrEqualToZero() {

        // when, then
        assertThat(new BeaconConfiguration(0,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .isCapturingAllowed(), is(false));

        assertThat(new BeaconConfiguration(-1,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .isCapturingAllowed(), is(false));

        assertThat(new BeaconConfiguration(Integer.MIN_VALUE,
            DataCollectionLevel.OFF,
            CrashReportingLevel.OFF)
            .isCapturingAllowed(), is(false));
    }
}
