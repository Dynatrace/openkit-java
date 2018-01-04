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

package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.util.DefaultLogger;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultLoggerTest {

    @Test
    public void defaultLoggerWithVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isErrorEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesWarnLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isWarnEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesInfoLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isInfoEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithVerboseOutputWritesDebugLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(true);

        //then
        assertThat(log.isDebugEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesErrorLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isErrorEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesWarnLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isWarnEnabled(), is(true));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesInfoLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isInfoEnabled(), is(false));
    }

    @Test
    public void defaultLoggerWithoutVerboseOutputWritesDebugLevelMessages() {
        //given
        DefaultLogger log = new DefaultLogger(false);

        //then
        assertThat(log.isDebugEnabled(), is(false));
    }
}
