/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;
import com.dynatrace.openkit.test.providers.TestSessionIDProvider;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;

public class OpenKitTestFactory {

	/**
	 * Default constructor is set to private for not allowing to instantiate this class and hiding the constructor from javadoc.
	 */
	private OpenKitTestFactory() {
	}

	public static OpenKitTestImpl createAppMonLocalInstance(String applicationName, String endpointURL, TestConfiguration testConfiguration)
        throws InterruptedException {

		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(getAppMonConfig(applicationName, endpointURL, testConfiguration), false);
		applyTestConfiguration(openKitTestImpl, testConfiguration);
		openKitTestImpl.initialize();
		return openKitTestImpl;
	}

	public static OpenKitTestImpl createAppMonRemoteInstance(String applicationName, long deviceID, String endpointURL)
        throws InterruptedException {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(getAppMonConfig(applicationName, endpointURL, deviceID), true);
		openKitTestImpl.initialize();
		return openKitTestImpl;
	}

	public static OpenKitTestImpl createDynatraceLocalInstance(String applicationName, String applicationID, String endpointURL, TestConfiguration testConfiguration)
        throws InterruptedException {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(getDynatraceConfig(applicationName, applicationID, endpointURL, testConfiguration.getDeviceID()),false);
		applyTestConfiguration(openKitTestImpl, testConfiguration);
		openKitTestImpl.initialize();
		return openKitTestImpl;
	}

	public static OpenKitTestImpl createDynatraceRemoteInstance(String applicationName, String applicationID, long deviceID, String endpointURL)
        throws InterruptedException {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(getDynatraceConfig(applicationName, applicationID, endpointURL, deviceID), true);
		openKitTestImpl.initialize();
		return openKitTestImpl;
	}

	private static void applyTestConfiguration(OpenKitTestImpl openKitTestImpl, TestConfiguration testConfiguration) {
		if (testConfiguration == null) {
			return;
		}
		if (testConfiguration.getStatusResponse() != null) {
			openKitTestImpl.setStatusResponse(testConfiguration.getStatusResponse(), testConfiguration.getStatusResponseCode());
		}
		if (testConfiguration.getTimeSyncResponse() != null) {
			openKitTestImpl.setTimeSyncResponse(testConfiguration.getTimeSyncResponse(), testConfiguration.getTimeSyncResponseCode());
		}
	}

	private static Configuration getAppMonConfig(String applicationName, String endpointURL, TestConfiguration testConfiguration) {
		return new Configuration(
			OpenKitType.APPMON,
			applicationName,
			applicationName,
			testConfiguration.getDeviceID(),
			endpointURL,
			true,
			new TestSessionIDProvider(),
			new SSLStrictTrustManager(),
			testConfiguration.getDevice(),
			testConfiguration.getApplicationVersion());
	}

	private static Configuration getAppMonConfig(String applicationName, String endpointURL, long deviceID) {
		return new Configuration(
			OpenKitType.APPMON,
			applicationName,
			applicationName,
			deviceID,
			endpointURL,
			true,
			new TestSessionIDProvider(),
			new SSLStrictTrustManager(),
			new Device("", "", ""),
			"");
	}

	private static Configuration getDynatraceConfig(String applicationName, String applicationID, String endpointURL, long deviceID) {
		return new Configuration(
			OpenKitType.DYNATRACE,
			applicationName,
			applicationID,
			deviceID,
			endpointURL,
			true,
			new TestSessionIDProvider(),
			new SSLStrictTrustManager(),
			new Device("", "", ""),
			"");
	}
}
