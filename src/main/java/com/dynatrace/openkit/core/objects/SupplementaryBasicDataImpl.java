/**
 * Copyright 2018-2022 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.objects;

import com.dynatrace.openkit.api.ConnectionType;

public class SupplementaryBasicDataImpl implements SupplementaryBasicData {
	private String networkTechnology;
	private ConnectionType connectionType;
	private String carrier;

	@Override
	public synchronized void setNetworkTechnology(String technology) {
		this.networkTechnology = technology;
	}

	public synchronized String getNetworkTechnology() {
		return networkTechnology;
	}

	@Override
	public synchronized void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public synchronized ConnectionType getConnectionType() {
		return connectionType;
	}

	@Override
	public synchronized void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public synchronized String getCarrier() {
		return carrier;
	}
}
