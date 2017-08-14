/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.Device;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;

public class ApplicationAndDeviceTestShared {

	public static void test(OpenKit openKit, String ipAddress) {
		openKit.setApplicationVersion("2017.42.3141");

		Device device = openKit.getDevice();
		device.setManufacturer("Dynatrace");
		device.setModelID("OpenKitTester");
		device.setOperatingSystem("Windows 10");

		Session session = openKit.createSession(ipAddress);
		Action action = session.enterAction("ApplicationAndDeviceTestAction");
		action.leaveAction();
		session.end();

		openKit.shutdown();
	}

}
