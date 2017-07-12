/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.OpenKitImpl.OpenKitType;

/**
 * This factory creates instances of the OpenKit to work with.
 * It can be used to create instances for both Dynatrace AppMon and Dynatrace SaaS/Managed.
 */
public class OpenKitFactory {

	/**
	 * Default constructor is set to private for not allowing to instantiate this class and hiding the constructor from javadoc.
	 */
	private OpenKitFactory() {
	}

	/**
	 * Creates a Dynatrace SaaS/Managed instance of the OpenKit.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID (must be a valid application UUID)
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the beacon forwarder to send the data to
	 * @return					Dynatrace SaaS/Managed instance of the OpenKit
	 */
	public static OpenKit createDynatraceInstance(String applicationName, String applicationID, long visitorID, String endpointURL) {
		return new OpenKitImpl(OpenKitType.DYNATRACE, applicationName, applicationID, visitorID, endpointURL, false);
	}

	/**
	 * Creates a Dynatrace SaaS/Managed instance of the OpenKit, optionally with verbose logging.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID (must be a valid application UUID)
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the beacon forwarder to send the data to
	 * @param verbose			if true, turn on verbose logging on stdout
	 * @return					Dynatrace SaaS/Managed instance of the OpenKit
	 */
	public static OpenKit createDynatraceInstance(String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
		return new OpenKitImpl(OpenKitType.DYNATRACE, applicationName, applicationID, visitorID, endpointURL, verbose);
	}

	/**
	 * Creates a Dynatrace AppMon instance of the OpenKit.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the Java/Webserver Agent to send the data to
	 * @return					Dynatrace AppMon instance of the OpenKit
	 */
	public static OpenKit createAppMonInstance(String applicationName, String applicationID, long visitorID, String endpointURL) {
		return new OpenKitImpl(OpenKitType.APPMON, applicationName, applicationID, visitorID, endpointURL, false);
	}

	/**
	 * Creates a Dynatrace AppMon instance of the OpenKit, optionally with verbose logging.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the Java/Webserver Agent to send the data to
	 * @param verbose			if true, turn on verbose logging on stdout
	 * @return					Dynatrace AppMon instance of the OpenKit
	 */
	public static OpenKit createAppMonInstance(String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
		return new OpenKitImpl(OpenKitType.APPMON, applicationName, applicationID, visitorID, endpointURL, verbose);
	}

}
