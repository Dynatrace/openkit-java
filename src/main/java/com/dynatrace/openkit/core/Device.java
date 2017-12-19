/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

/**
 * Class holding device specific information
 */
public class Device {

	// platform information
	private final String operatingSystem;
	private final String manufacturer;
	private final String modelID;

	public Device(String operatingSystem, String manufacturer, String modelID) {

		this.operatingSystem = operatingSystem;
		this.manufacturer = manufacturer;
		this.modelID = modelID;
	}

	// *** getter methods ***

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getModelID() {
		return modelID;
	}

}
