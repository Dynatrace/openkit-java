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
package com.dynatrace.openkit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class DataCollectionLevelTest {

    @Parameter
    public int expectedValue;

    @Parameter(1)
    public DataCollectionLevel dataCollectionLevel;

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                {0, DataCollectionLevel.OFF},
                {1, DataCollectionLevel.PERFORMANCE},
                {2, DataCollectionLevel.USER_BEHAVIOR}
        });
    }

    @Test
    public void hasCorrectValue() {
        assertThat(dataCollectionLevel.getIntValue(), is(expectedValue));
        assertThat(dataCollectionLevel.asBeaconValue(), is(String.valueOf(expectedValue)));
    }

    @Test
    public void asBeaconValueReturnsCorrectValue() {
        // when, then
        assertThat(DataCollectionLevel.OFF.asBeaconValue(), is("0"));
        assertThat(DataCollectionLevel.PERFORMANCE.asBeaconValue(), is("1"));
        assertThat(DataCollectionLevel.USER_BEHAVIOR.asBeaconValue(), is("2"));
    }

    @Test
    public void defaultValueIsUserBehavior() {
        // when, then
        assertThat(DataCollectionLevel.defaultValue(), is(DataCollectionLevel.USER_BEHAVIOR));
    }
}
