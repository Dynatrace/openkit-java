/**
 * Copyright 2018 Dynatrace LLC
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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BeaconCacheConfigurationTest {

    @Test
    public void getMaxRecordAge() {

        // then
        assertThat(new BeaconCacheConfiguration(-100L, 1, 2).getMaxRecordAge(),
            is(-100L));
        assertThat(new BeaconCacheConfiguration(0L, 1, 2).getMaxRecordAge(),
            is(0L));
        assertThat(new BeaconCacheConfiguration(200L, 1, 2).getMaxRecordAge(),
            is(200L));
    }

    @Test
    public void getCacheSizeLowerBound() {

        // then
        assertThat(new BeaconCacheConfiguration(0L, -1, 2).getCacheSizeLowerBound(),
            is(-1L));
        assertThat(new BeaconCacheConfiguration(-1L, 0, 2).getCacheSizeLowerBound(),
            is(0L));
        assertThat(new BeaconCacheConfiguration(0L, 1, 2).getCacheSizeLowerBound(),
            is(1L));
    }

    @Test
    public void getCacheSizeUpperBound() {

        // then
        assertThat(new BeaconCacheConfiguration(0L, -1, -2).getCacheSizeUpperBound(),
            is(-2L));
        assertThat(new BeaconCacheConfiguration(-1L, 1, 0).getCacheSizeUpperBound(),
            is(0L));
        assertThat(new BeaconCacheConfiguration(0L, 1, 2).getCacheSizeUpperBound(),
            is(2L));
    }
}
