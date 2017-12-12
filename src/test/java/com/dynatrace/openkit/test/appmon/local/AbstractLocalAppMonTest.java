/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
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
    public void setup() throws InterruptedException {
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