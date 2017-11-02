/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.AppMonConfiguration;
import com.dynatrace.openkit.core.configuration.DynatraceConfiguration;
import com.dynatrace.openkit.core.configuration.DynatraceManagedConfiguration;
import com.dynatrace.openkit.providers.DefaultHTTPClientProvider;

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
	 * Creates a Dynatrace SaaS instance of the OpenKit.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID (must be a valid application UUID)
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the beacon forwarder to send the data to
	 * @return					Dynatrace SaaS instance of the OpenKit
	 */
	public static OpenKit createDynatraceInstance(String applicationName, String applicationID, long visitorID, String endpointURL) {
		return createDynatraceInstance(applicationName, applicationID, visitorID, endpointURL, false);
	}

	/**
	 * Creates a Dynatrace SaaS instance of the OpenKit, optionally with verbose logging.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID (must be a valid application UUID)
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the beacon forwarder to send the data to
	 * @param verbose			if true, turn on verbose logging on stdout
	 * @return					Dynatrace SaaS instance of the OpenKit
	 */
	public static OpenKit createDynatraceInstance(String applicationName, String applicationID, long visitorID, String endpointURL, boolean verbose) {
		return new OpenKitImpl(
				new DynatraceConfiguration(applicationName, applicationID, visitorID, endpointURL, verbose));
	}

	/**
	 * Creates a Dynatrace Managed instance of the OpenKit, optionally with verbose logging.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID (must be a valid application UUID)
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the beacon forwarder to send the data to
	 * @param tenantID			the tenant ID
	 * @return					Dynatrace Managed instance of the OpenKit
	 */
	public static OpenKit createDynatraceManagedInstance(String applicationName, String applicationID, long visitorID, String endpointURL, String tenantID) {
		return createDynatraceManagedInstance(applicationName, applicationID, visitorID, endpointURL, tenantID, false);
	}

	/**
	 * Creates a Dynatrace Managed instance of the OpenKit, optionally with verbose logging.
	 *
	 * @param applicationName	the application name
	 * @param applicationID		the application ID (must be a valid application UUID)
	 * @param visitorID			unique visitor ID
	 * @param endpointURL		the URL of the beacon forwarder to send the data to
	 * @param tenantID			the id of the tenant
	 * @param verbose			if true, turn on verbose logging on stdout
	 * @return					Dynatrace Managed instance of the OpenKit
	 */
	public static OpenKit createDynatraceManagedInstance(String applicationName, String applicationID, long visitorID, String endpointURL, String tenantID, boolean verbose) {
		return new OpenKitImpl(
				new DynatraceManagedConfiguration(tenantID, applicationName, applicationID, visitorID, endpointURL, verbose));
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
		return createAppMonInstance(applicationName, applicationID, visitorID, endpointURL, false);
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
		return new OpenKitImpl(
				new AppMonConfiguration(applicationName, applicationID, visitorID, endpointURL, verbose));
	}

}
