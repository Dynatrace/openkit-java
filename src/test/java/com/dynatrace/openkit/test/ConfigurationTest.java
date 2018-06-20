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

import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;
import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.*;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.providers.DefaultSessionIDProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigurationTest {
    private static final String host = "localhost:9999";
    private static final String tenantId = "asdf";
    private static final String applicationName = "testApp";
    private static final String applicationVersion = "0.3";

    @Test
    public void urlIsSetCorrectly() {
        String tenantURL = String.format("https://%s.%s/mbeacon", tenantId, host);

        Configuration configuration = getDynatraceConfig(tenantURL);

        assertEquals(tenantURL, configuration.getHttpClientConfig().getBaseURL());
    }

    private Configuration getDynatraceConfig(String tenantURL) {
        return new Configuration(OpenKitType.DYNATRACE, "", "", 17, tenantURL,
            new DefaultSessionIDProvider(), new SSLStrictTrustManager(), new Device("", "", ""), "",
            new BeaconCacheConfiguration(-1, -1, -1),
            new BeaconConfiguration(1, DataCollectionLevel.USER_BEHAVIOR, CrashReportingLevel.OPT_IN_CRASHES));
    }
}
