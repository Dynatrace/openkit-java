/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.dynatrace.remote;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.dynatrace.openkit.test.AbstractDynatraceTest;
import com.dynatrace.openkit.test.OpenKitTestFactory;

@Ignore("Integration tests are ignored")
public class AbstractRemoteDynatraceTest extends AbstractDynatraceTest {

	@Before
	public void setup() throws InterruptedException {
		openKitTestImpl = OpenKitTestFactory.createDynatraceRemoteInstance(TEST_APPLICATION_NAME, TEST_APPLICATION_ID,new Random(System.currentTimeMillis()).nextLong(), TEST_ENDPOINT);
		openKit = openKitTestImpl;
		openKit.initialize();
	}

	@Before
	public void printStart() {
		System.out.println("Remote Dynatrace Test: " + this.getClass().getSimpleName() + " - Start");
	}

	@After
	public void printEnd() {
		System.out.println("Remote Dynatrace Test: " + this.getClass().getSimpleName() + " - End");
		openKit.shutdown();
	}
}