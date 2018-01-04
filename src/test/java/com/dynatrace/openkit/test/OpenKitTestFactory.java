/**
 * Copyright 2018 Dynatrace LLC
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

package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.test.providers.TestSessionIDProvider;

public class OpenKitTestFactory {

    /**
     * Default constructor is set to private for not allowing to instantiate this class and hiding the constructor from javadoc.
     */
    private OpenKitTestFactory() {
    }

    public static OpenKitTestImpl createAppMonLocalInstance(String applicationName, String endpointURL, TestConfiguration testConfiguration)
        throws InterruptedException {

        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getAppMonConfig(applicationName, endpointURL, testConfiguration), false);
        applyTestConfiguration(openKitTestImpl, testConfiguration);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    public static OpenKitTestImpl createAppMonRemoteInstance(String applicationName, long deviceID, String endpointURL)
        throws InterruptedException {
        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getAppMonConfig(applicationName, endpointURL, deviceID), true);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    public static OpenKitTestImpl createDynatraceLocalInstance(String applicationName, String applicationID, String endpointURL, TestConfiguration testConfiguration)
        throws InterruptedException {
        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getDynatraceConfig(applicationName, applicationID, endpointURL, testConfiguration
            .getDeviceID()), false);
        applyTestConfiguration(openKitTestImpl, testConfiguration);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    public static OpenKitTestImpl createDynatraceRemoteInstance(String applicationName, String applicationID, long deviceID, String endpointURL)
        throws InterruptedException {
        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getDynatraceConfig(applicationName, applicationID, endpointURL, deviceID), true);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    private static void applyTestConfiguration(OpenKitTestImpl openKitTestImpl, TestConfiguration testConfiguration) {
        if (testConfiguration == null) {
            return;
        }
        if (testConfiguration.getStatusResponse() != null) {
            openKitTestImpl.setStatusResponse(testConfiguration.getStatusResponse(), testConfiguration.getStatusResponseCode());
        }
        if (testConfiguration.getTimeSyncResponse() != null) {
            openKitTestImpl.setTimeSyncResponse(testConfiguration.getTimeSyncResponse(), testConfiguration.getTimeSyncResponseCode());
        }
    }

    private static Configuration getAppMonConfig(String applicationName, String endpointURL, TestConfiguration testConfiguration) {
        return new Configuration(
            OpenKitType.APPMON,
            applicationName,
            applicationName,
            testConfiguration.getDeviceID(),
            endpointURL,
            new TestSessionIDProvider(),
            new SSLStrictTrustManager(),
            testConfiguration.getDevice(),
            testConfiguration.getApplicationVersion());
    }

    private static Configuration getAppMonConfig(String applicationName, String endpointURL, long deviceID) {
        return new Configuration(
            OpenKitType.APPMON,
            applicationName,
            applicationName,
            deviceID,
            endpointURL,
            new TestSessionIDProvider(),
            new SSLStrictTrustManager(),
            new Device("", "", ""),
            "");
    }

    private static Configuration getDynatraceConfig(String applicationName, String applicationID, String endpointURL, long deviceID) {
        return new Configuration(
            OpenKitType.DYNATRACE,
            applicationName,
            applicationID,
            deviceID,
            endpointURL,
            new TestSessionIDProvider(),
            new SSLStrictTrustManager(),
            new Device("", "", ""),
            "");
    }
}
