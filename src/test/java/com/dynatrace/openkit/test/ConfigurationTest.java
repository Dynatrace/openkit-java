package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.configuration.AbstractConfiguration;
import com.dynatrace.openkit.core.configuration.AppMonConfiguration;
import com.dynatrace.openkit.core.configuration.DynatraceConfiguration;
import com.dynatrace.openkit.core.configuration.DynatraceManagedConfiguration;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.providers.DefaultSessionIDProvider;
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
    public void saasURLIsCorrect() {
        String tenantURL = String.format("https://%s.%s", tenantId, host);

        AbstractConfiguration configuration =
            new DynatraceConfiguration("", "", 17, tenantURL, false, new SSLStrictTrustManager(), new DefaultSessionIDProvider());

        String expected = String.format("%s/mbeacon", tenantURL);

        assertEquals(expected, configuration.getHttpClientConfig().getBaseURL());
    }

    @Test
    public void mangedURLIsCorrect() {
        String managedHost = String.format("http://%s", host);

        AbstractConfiguration configuration =
            new DynatraceManagedConfiguration(tenantId, "", "",17, managedHost, false, new SSLStrictTrustManager(), new DefaultSessionIDProvider());

        String expected = String.format("%s/mbeacon/%s", managedHost, tenantId);

        assertEquals(expected, configuration.getHttpClientConfig().getBaseURL());
    }

    @Test
    public void appMonURLIsCorrect() {
        String appMonHost = String.format("https://%s", host);

        AbstractConfiguration configuration = new AppMonConfiguration(applicationName, 17, appMonHost, false,  new SSLStrictTrustManager(), new DefaultSessionIDProvider());

        String expected = String.format("%s/dynaTraceMonitor", appMonHost);

        assertEquals(expected, configuration.getHttpClientConfig().getBaseURL());
    }

    @Test
    public void applicationIdAndApplicationNameIdenticalForAppMonConfig() {
        AbstractConfiguration configuration = new AppMonConfiguration(applicationName, 17, "", false, new SSLStrictTrustManager(), new DefaultSessionIDProvider());

        assertThat(applicationName, is(configuration.getApplicationID()));
        assertThat(applicationName, is(configuration.getApplicationName()));
    }

    @Test
    public void defaultApplicationVersionIsCorrect() {
        AbstractConfiguration configuration = new AppMonConfiguration(applicationName, 17, "", false, new SSLStrictTrustManager(), new DefaultSessionIDProvider());

        assertThat(applicationVersion, is(configuration.getApplicationVersion()));
    }
}
