/**
 * Copyright 2018-2021 Dynatrace LLC
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

public class EventPayloadAttributes {
    public static final String TIMESTAMP = "timestamp";
    public static final String DT_TYPE = "dt.type";
    public static final String DT_AGENT_VERSION = "dt.agent.version";
    public static final String DT_AGENT_TECHNOLOGY_TYPE = "dt.agent.technology_type";
    public static final String DT_AGENT_FLAVOR = "dt.agent.flavor";
    public static final String APP_VERSION = "app.version";
    public static final String OS_NAME = "os.name";
    public static final String DEVICE_MANUFACTURER = "device.manufacturer";
    public static final String DEVICE_MODEL_IDENTIFIER = "device.model.identifier";
    public static final String WINDOW_ORIENTATION = "window.orientation";

    public static final String DT_TYPE_CUSTOM = "custom";
    public static final String DT_TYPE_BIZ = "biz";

}
