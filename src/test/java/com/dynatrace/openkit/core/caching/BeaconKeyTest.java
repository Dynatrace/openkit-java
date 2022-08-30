/**
 *   Copyright 2018-2021 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.core.caching;

import org.junit.Test;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class BeaconKeyTest {

    @Test
    public void aBeaconKeyDoesNotEqualNull() {
        // given
        BeaconKey key = new BeaconKey(1, 0);

        // when
        boolean obtained = key.equals(null);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void aBeaconKeyDoesNotEqualObjectOfDifferentType() {
        // given
        BeaconKey key = new BeaconKey(1, 0);

        // when
        boolean obtained = key.equals(new Object());

        // when, then
        assertThat(obtained, is(false));
    }

    @Test
    public void instancesWithSameValuesAreEqual() {
        // given
        BeaconKey keyOne = new BeaconKey(17, 18);
        BeaconKey keyTwo = new BeaconKey(17, 18);

        // when
        boolean obtained = keyOne.equals(keyTwo);

        // then
        assertThat(obtained, is(true));
    }

    @Test
    public void instancesWithSameValuesHaveSameHash() {
        // given
        BeaconKey keyOne = new BeaconKey(17, 18);
        BeaconKey keyTwo = new BeaconKey(17, 18);

        // when, then
        assertThat(keyOne.hashCode(), is(keyTwo.hashCode()));
    }

    @Test
    public void instancesWithDifferentBeaconIdAreNotEqual() {
        // given
        BeaconKey keyOne = new BeaconKey(37, 18);
        BeaconKey keyTwo = new BeaconKey(38, 18);

        // when
        boolean obtained = keyOne.equals(keyTwo);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void instancesWithDifferentBeaconIdHaveDifferentHash() {
        // given
        BeaconKey keyOne = new BeaconKey(37, 18);
        BeaconKey keyTwo = new BeaconKey(38, 18);

        // when, then
        assertThat(keyOne.hashCode(), is(not(keyTwo.hashCode())));
    }

    @Test
    public void instancesWithDifferentBeaconSeqNoAreNotEqual() {
        // given
        BeaconKey keyOne = new BeaconKey(17, 37);
        BeaconKey keyTwo = new BeaconKey(17, 73);

        // when
        boolean obtained = keyOne.equals(keyTwo);

        // then
        assertThat(obtained, is(false));
    }

    @Test
    public void instancesWithDifferentBeaconSeqNoHaveDifferentHash() {
        // given
        BeaconKey keyOne = new BeaconKey(17, 37);
        BeaconKey keyTwo = new BeaconKey(17, 73);

        // when, then
        assertThat(keyOne.hashCode(), is(not(keyTwo.hashCode())));
    }
}
