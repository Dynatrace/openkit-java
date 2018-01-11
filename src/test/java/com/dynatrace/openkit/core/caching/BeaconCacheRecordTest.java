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

package com.dynatrace.openkit.core.caching;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class BeaconCacheRecordTest {

    @Test
    public void getData() {

        // when passing null as argument, then
        assertThat(new BeaconCacheRecord(0L, null).getData(), is(nullValue()));

        // when passing an empty string as argument, then
        assertThat(new BeaconCacheRecord(0L, "").getData(), is(""));

        // and when passing string as argument, then
        assertThat(new BeaconCacheRecord(0L, "foobar").getData(), is("foobar"));
    }

    @Test
    public void getTimestamp() {

        // when passing negative timestamp, then
        assertThat(new BeaconCacheRecord(-1L, "a").getTimestamp(), is(-1L));

        // and when passing zero as timestamp, then
        assertThat(new BeaconCacheRecord(0L, "a").getTimestamp(), is(0L));

        // and when passing a positive timestamp, then
        assertThat(new BeaconCacheRecord(1L, "a").getTimestamp(), is(1L));
    }

    @Test
    public void getDataSizeInBytes() {

        // when data is null, then
        assertThat(new BeaconCacheRecord(0L, null).getDataSizeInBytes(), is(0));

        // and when data is an empty string, then
        assertThat(new BeaconCacheRecord(0L, "").getDataSizeInBytes(), is(0));

        // and when data is valid, then
        assertThat(new BeaconCacheRecord(0L, "a").getDataSizeInBytes(), is(2));
        assertThat(new BeaconCacheRecord(0L, "ab").getDataSizeInBytes(), is(4));
        assertThat(new BeaconCacheRecord(0L, "abc").getDataSizeInBytes(), is(6));
    }

    @Test
    public void markForSending() {

        // given
        BeaconCacheRecord target = new BeaconCacheRecord(0L, "abc");

        // then a newly created record is not marked for sending
        assertThat(target.isMarkedForSending(), is(false));

        // and when explicitly marked for sending
        target.markForSending();

        // then
        assertThat(target.isMarkedForSending(), is(true));

        // and when the sending mark is removed
        target.unsetSending();

        // then
        assertThat(target.isMarkedForSending(), is(false));
    }
}
