/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.appmon.remote;

import com.dynatrace.openkit.test.AbstractAppMonTest;
import com.dynatrace.openkit.test.OpenKitTestFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.util.Random;

@Ignore("Integration tests are ignored")
public class AbstractRemoteAppMonTest extends AbstractAppMonTest {

    @Before
    public void setUp() throws InterruptedException {
        openKitTestImpl = OpenKitTestFactory.createAppMonRemoteInstance(TEST_APPLICATION_NAME, new Random(System.currentTimeMillis())
            .nextLong(), TEST_ENDPOINT);
        openKit = openKitTestImpl;
    }

    @Before
    public void printStart() {
        System.out.println("Remote AppMon Test: " + this.getClass().getSimpleName() + " - Start");
    }

    @After
    public void printEnd() {
        System.out.println("Remote AppMon Test: " + this.getClass().getSimpleName() + " - End");
        openKit.shutdown();
    }
}