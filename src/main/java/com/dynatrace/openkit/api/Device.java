/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.api;

/**
 * This interface provides functionality to set basic device information, like operating system, manufacturer and model information.
 */
public interface Device {

	/**
	 * Sets operating system name.
	 *
	 * @param operatingSystem	name of operating system
	 */
	void setOperatingSystem(String operatingSystem);

	/**
	 * Sets manufacturer name.
	 *
	 * @param manufacturer		name of manufacturer
	 */
	void setManufacturer(String manufacturer);

	/**
	 * Sets a model identifier.
	 *
	 * @param modelID		model identifier
	 */
	void setModelID(String modelID);

}
