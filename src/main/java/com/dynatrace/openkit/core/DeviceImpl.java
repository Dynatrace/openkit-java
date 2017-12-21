/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Device;
import com.dynatrace.openkit.api.OpenKitConstants;

/**
 * Actual implementation of the {@link Device} interface.
 */
public class DeviceImpl implements Device {

	// platform information
	private String operatingSystem = OpenKitConstants.DEFAULT_OPERATING_SYSTEM;
	private String manufacturer = OpenKitConstants.DEFAULT_MANUFACTURER;
	private String modelID = OpenKitConstants.DEFAULT_DEVICE_ID;

	// *** Device interface methods ***

	@Override
	public void setOperatingSystem(String operatingSystem) {
		if(operatingSystem != null && !operatingSystem.isEmpty()) {
			this.operatingSystem = operatingSystem;
		}
	}

	@Override
	public void setManufacturer(String manufacturer) {
		if(manufacturer != null && !manufacturer.isEmpty()) {
			this.manufacturer = manufacturer;
		}
	}

	@Override
	public void setModelID(String modelID) {
		if(modelID != null && !modelID.isEmpty()) {
			this.modelID = modelID;
		}
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
