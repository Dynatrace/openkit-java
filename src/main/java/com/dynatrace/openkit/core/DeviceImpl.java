/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Device;

/**
 * Actual implementation of the {@link Device} interface.
 */
public class DeviceImpl implements Device {

	// platform information
	private String operatingSystem = null;
	private String manufacturer = null;
	private String modelID = null;

	// *** Device interface methods ***

	@Override
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	@Override
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	@Override
	public void setModelID(String modelID) {
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
