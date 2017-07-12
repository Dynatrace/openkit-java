/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

/**
 * This interface allows tagging and timing of a web request.
 */
public interface WebRequestTag {

	/**
	 * Returns the Dynatrace tag which has to be set manually as Dynatrace HTTP header
	 * ({@link OpenKit#WEBREQUEST_TAG_HEADER}). <br>
	 * This is only necessary for tagging web requests via 3rd party HTTP clients.
	 *
	 * @return		the Dynatrace tag to be set as HTTP header value or an empty String if capture is off
	 */
	public String getTag();

	/**
	 * Sets the response code of this web request. Has to be called before {@link WebRequestTag#stopTiming()}.
	 *
	 * @param responseCode		response code of this web request
	 */
	public void setResponseCode(int responseCode);

	/**
	 * Starts the web request timing. Should be called when the web request is initiated.
	 */
	public void startTiming();

	/**
	 * Stops the web request timing. Should be called when the web request is finished.
	 */
	public void stopTiming();

}
