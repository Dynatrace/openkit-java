package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.core.configuration.AppMonConfiguration;
import com.dynatrace.openkit.core.configuration.DynatraceConfiguration;
import com.dynatrace.openkit.core.configuration.DynatraceManagedConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ConfigurationTest {
    private static final String host = "localhost:9999";
    private static final String tenantId = "asdf";
    private static final String applicationName = "testApp";
    private static final String applicationVersion = "0.3";

    @Test
    public void saasUrlIsCorrect() {
        String tenantUrl = String.format("https://%s.%s", tenantId, host);

        AbstractConfiguration configuration =
            new DynatraceConfiguration("", "", 17, tenantUrl, false);

        String expected = String.format("%s/mbeacon", tenantUrl);

        assertEquals(expected, configuration.getHttpClientConfig().getBaseUrl());
    }

    @Test
    public void mangedUrlIsCorrect() {
        String managedHost = String.format("http://%s", host);

        AbstractConfiguration configuration =
            new DynatraceManagedConfiguration(tenantId, "", "",17, managedHost, false);

        String expected = String.format("%s/mbeacon/%s", managedHost, tenantId);

        assertEquals(expected, configuration.getHttpClientConfig().getBaseUrl());
    }

    @Test
    public void appMonUrlIsCorrect() {
        String appMonHost = String.format("https://%s", host);

        AbstractConfiguration configuration = new AppMonConfiguration(applicationName,17,appMonHost,false);

        String expected = String.format("%s/dynaTraceMonitor", appMonHost);

        assertEquals(expected, configuration.getHttpClientConfig().getBaseUrl());
    }

    @Test
    public void applicationIdAndApplicationNameIdenticalForAppMonConfig() {
        AbstractConfiguration configuration = new AppMonConfiguration(applicationName,17, "", false);

        assertThat(applicationName, is(configuration.getApplicationID()));
        assertThat(applicationName, is(configuration.getApplicationName()));
    }

    @Test
    public void defaultApplicationVersionIsCorrect() {
        AbstractConfiguration configuration = new AppMonConfiguration(applicationName,17, "", false);

        assertThat(applicationVersion, is(configuration.getApplicationVersion()));
    }
}
