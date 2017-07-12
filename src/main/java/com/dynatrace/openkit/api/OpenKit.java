/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

/**
 * This interface provides basic OpenKit functionality, like creating a Session and shutting down the OpenKit.
 */
public interface OpenKit {

	/**
	 * Name of Dynatrace HTTP header which is used for tagging web requests.
	 */
	public String WEBREQUEST_TAG_HEADER = "X-dynaTrace";

	/**
	 * Initializes the OpenKit, which includes waiting for the OpenKit to receive its initial settings from the Dynatrace/Appmon server.
	 * Must be done before any other calls to the OpenKit, otherwise those calls to the OpenKit will do nothing.
	 */
	public void initialize();

	/**
	 * Returns the Device used by this OpenKit instance. This can be used to provide basic information, like operating system,
	 * manufacturer and model information.
	 *
	 * @return	Device used by this OpenKit instance
	 */
	public Device getDevice();

	/**
	 * Creates a Session instance which can then be used to create Actions.
	 *
	 * @param clientIPAddress	client IP address where this Session is coming from
	 * @return					Session instance to work with
	 */
	public Session createSession(String clientIPAddress);

	/**
	 *	Shuts down the OpenKit, ending all open Sessions and waiting for them to be sent.
	 */
	public void shutdown();

}
