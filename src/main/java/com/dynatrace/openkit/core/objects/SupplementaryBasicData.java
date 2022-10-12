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

/**
 * Specifies supplementary basic data which will be written to the {@link com.dynatrace.openkit.protocol.Beacon}
 */
public interface SupplementaryBasicData {
	/**
	 * Sets the network technology used by the device
	 */
	void setNetworkTechnology(String technology);

	/**
	 * Returns the network technology used by the device
	 * @return network technology
	 */
	String getNetworkTechnology();

	/**
	 * Sets the connection type used by the device
	 */
	void setConnectionType(ConnectionType connectionType);

	/**
	 * Returns the connection type used by the device
	 * @return connection type
	 */
	ConnectionType getConnectionType();

	/**
	 * Sets the carrier used by the device
	 */
	void setCarrier(String carrier);

	/**
	 * Returns the carrier used by the device
	 * @return carrier
	 */
	String getCarrier();
}