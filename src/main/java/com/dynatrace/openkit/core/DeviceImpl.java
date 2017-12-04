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

	public static final String DEFAULT_OPERATING_SYSTEM = "OpenKit 0.3";		// 'OpenKit 0.3'
	public static final String DEFAULT_MANUFACTURER = "Dynatrace";				// default: 'Dynatrace'
	public static final String DEFAULT_DEVICE_ID = "OpenKitDevice";			// default: 'OpenKitDevice'

	// platform information
	private String operatingSystem = DEFAULT_OPERATING_SYSTEM;
	private String manufacturer = DEFAULT_MANUFACTURER;
	private String modelID = DEFAULT_DEVICE_ID;

	// *** Device interface methods ***

	@Override
	public void setOperatingSystem(String operatingSystem) {
		if(operatingSystem != null && operatingSystem.length() > 0) {
			this.operatingSystem = operatingSystem;
		}
	}

	@Override
	public void setManufacturer(String manufacturer) {
		if(manufacturer != null && manufacturer.length() > 0) {
			this.manufacturer = manufacturer;
		}
	}

	@Override
	public void setModelID(String modelID) {
		if(modelID != null && modelID.length() > 0) {
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
