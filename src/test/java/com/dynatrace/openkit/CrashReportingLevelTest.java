/**
 *   Copyright 2018-2019 Dynatrace LLC
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

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CrashReportingLevelTest {

    @Parameterized.Parameter
    public int expectedValue;

    @Parameterized.Parameter(1)
    public CrashReportingLevel crashReportingLevel;

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                {0, CrashReportingLevel.OFF},
                {1, CrashReportingLevel.OPT_OUT_CRASHES},
                {2, CrashReportingLevel.OPT_IN_CRASHES}
        });
    }

    @Test
    public void hasCorrectValue() {
        assertThat(crashReportingLevel.getIntValue(), is(expectedValue));
        assertThat(crashReportingLevel.asBeaconValue(), is(String.valueOf(expectedValue)));
    }

}
