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

package com.dynatrace.openkit.test.appmon.local;

import com.dynatrace.openkit.test.AbstractAppMonTest;
import com.dynatrace.openkit.test.OpenKitTestFactory;
import com.dynatrace.openkit.test.TestConfiguration;
import org.junit.After;
import org.junit.Before;

import java.util.Random;

public abstract class AbstractLocalAppMonTest extends AbstractAppMonTest {

    protected TestConfiguration testConfiguration = null;

    public AbstractLocalAppMonTest() {
        testConfiguration = new TestConfiguration();
        testConfiguration.setDeviceID(new Random(System.currentTimeMillis()).nextLong());
        testConfiguration.setStatusResponse("type=m&si=120&bn=dynaTraceMonitor&id=1", 200);
        testConfiguration.setTimeSyncResponse("type=mts&t1=-1&t2=-1", 200);
    }

    @Before
    public void setUp() throws InterruptedException {
        openKitTestImpl = OpenKitTestFactory.createAppMonLocalInstance(TEST_APPLICATION_NAME, TEST_ENDPOINT, testConfiguration);
        openKit = openKitTestImpl;
        openKit.waitForInitCompletion();
    }

    @Before
    public void printStart() {
        System.out.println("Local AppMon Test: " + this.getClass().getSimpleName() + " - Start");
    }

    @After
    public void printEnd() {
        System.out.println("Local AppMon Test: " + this.getClass().getSimpleName() + " - End");
        openKit.shutdown();
    }

}