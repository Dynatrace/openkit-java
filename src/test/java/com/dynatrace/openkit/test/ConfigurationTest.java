package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
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
            false, new SSLStrictTrustManager(), new Device("", "", ""), "");
    }
}
