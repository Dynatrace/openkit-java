/***************************************************
 * (c) 2016-2017 Dynatrace LLC
 *
 * @author: Christian Schwarzbauer
 */
package com.dynatrace.openkit.test;

import com.dynatrace.openkit.AbstractOpenKitBuilder;
import com.dynatrace.openkit.core.Device;

public class TestConfiguration {

	private long deviceID = -1;
	private String statusResponse = null;
	private int statusResponseCode = -1;
	private String timeSyncResponse = null;
	private int timeSyncResponseCode = -1;

	// set defaults for device
	private Device device = new Device(
		AbstractOpenKitBuilder.DEFAULT_OPERATING_SYSTEM,
		AbstractOpenKitBuilder.DEFAULT_MANUFACTURER,
		AbstractOpenKitBuilder.DEFAULT_MODEL_ID);
	// set default for version
	private String applicationVersion = AbstractOpenKitBuilder.DEFAULT_APPLICATION_VERSION;

	public void setDeviceID(long deviceID) {
		this.deviceID = deviceID;
	}

	public void setStatusResponse(String response, int responseCode) {
		statusResponse = response;
		statusResponseCode = responseCode;
	}

	public void setTimeSyncResponse(String response, int responseCode) {
		timeSyncResponse = response;
		timeSyncResponseCode = responseCode;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public long getDeviceID() {
		return deviceID;
	}

	public String getStatusResponse() {
		return statusResponse;
	}

	public int getStatusResponseCode() {
		return statusResponseCode;
	}

	public String getTimeSyncResponse() {
		return timeSyncResponse;
	}

	public int getTimeSyncResponseCode() {
		return timeSyncResponseCode;
	}

	public Device getDevice() {
		return device;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}
}
