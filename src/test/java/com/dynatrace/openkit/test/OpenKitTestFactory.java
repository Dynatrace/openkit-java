/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.OpenKitImpl.OpenKitType;

public class OpenKitTestFactory {

	/**
	 * Default constructor is set to private for not allowing to instantiate this class and hiding the constructor from javadoc.
	 */
	private OpenKitTestFactory() {
	}

	public static OpenKitTestImpl createAppMonLocalInstance(String applicationName, String applicationID, String endpointURL, TestConfiguration testConfiguration) {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(OpenKitType.APPMON, applicationName, applicationID, testConfiguration.getVisitorID(), endpointURL, true, false);
		applyTestConfiguration(openKitTestImpl, testConfiguration);
		return openKitTestImpl;
	}

	public static OpenKitTestImpl createAppMonRemoteInstance(String applicationName, String applicationID, long visitorID, String endpointURL) {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(OpenKitType.APPMON, applicationName, applicationID, visitorID, endpointURL, true, true);
		return openKitTestImpl;
	}

	public static OpenKitTestImpl createDynatraceLocalInstance(String applicationName, String applicationID, String endpointURL, TestConfiguration testConfiguration) {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(OpenKitType.DYNATRACE, applicationName, applicationID, testConfiguration.getVisitorID(), endpointURL, true, false);
		applyTestConfiguration(openKitTestImpl, testConfiguration);
		return openKitTestImpl;
	}

	public static OpenKitTestImpl createDynatraceRemoteInstance(String applicationName, String applicationID, long visitorID, String endpointURL) {
		OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(OpenKitType.DYNATRACE, applicationName, applicationID, visitorID, endpointURL, true, true);
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

}
