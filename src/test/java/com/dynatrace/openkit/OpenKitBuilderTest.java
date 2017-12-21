package com.dynatrace.openkit;

import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.ssl.SSLBlindTrustManager;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class OpenKitBuilderTest {
    private static final String endpoint = "https://localhost:12345";
    private static final String appId = "asdf123";
    private static final String appName = "myName";
    private static final long deviceId = 1234L;
    private static final String appVersion = "1.2.3.4";
    private static final String os = "custom OS";
    private static final String manufacturer = "custom manufacturer";
    private static final String modelId = "custom model id";

    @Test
    public void defaultsAreSetForAppMon() {
        defaultsAreSet(new AppMonOpenKitBuilder(endpoint, appId, deviceId).buildConfiguration());
    }

    @Test
    public void defaultsAreSetForDynatrace() {
        defaultsAreSet(new DynatraceOpenKitBuilder(endpoint, appName, deviceId).buildConfiguration());
    }

    public void defaultsAreSet(Configuration configuration) {

        // default values
        assertThat(configuration.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
        assertThat(configuration.getDevice().getManufacturer(), is(equalTo(OpenKitConstants.DEFAULT_MANUFACTURER)));
        assertThat(configuration.getDevice().getOperatingSystem(), is(equalTo(OpenKitConstants.DEFAULT_OPERATING_SYSTEM)));
        assertThat(configuration.getDevice().getModelID(), is(equalTo(OpenKitConstants.DEFAULT_MODEL_ID)));
        assertThat(configuration.isVerbose(), is(false));

        // default trust manager
        assertThat(configuration.getHttpClientConfig().getSSLTrustManager(), instanceOf(SSLStrictTrustManager.class));
    }

    @Test
    public void applicationNameIsSetCorrectlyForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId).buildConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(appName)));
        assertThat(target.getApplicationName(), is(equalTo(target.getApplicationID())));
    }

    @Test
    public void canOverrideTrustManagerForAppMon()
    {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withTrustManager(new SSLBlindTrustManager())
            .buildConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), instanceOf(SSLBlindTrustManager.class));
    }

    @Test
    public void canOverrideTrustManagerForDynatrace()
    {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withTrustManager(new SSLBlindTrustManager())
            .buildConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), instanceOf(SSLBlindTrustManager.class));
    }

    @Test
    public void canEnableVerboseForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .enableVerbose()
            .buildConfiguration();

        assertThat(target.isVerbose(), is(true));
    }

    @Test
    public void canEnableVerboseForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .enableVerbose()
            .buildConfiguration();

        assertThat(target.isVerbose(), is(true));
    }

    @Test
    public void canSetApplicationVersionForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withApplicationVersion(appVersion)
            .buildConfiguration();

        assertThat(target.getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetApplicationVersionForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withApplicationVersion(appVersion)
            .buildConfiguration();

        assertThat(target.getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetOperatingSystemForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withOperatingSystem(os)
            .buildConfiguration();

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetOperatingSystemForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withOperatingSystem(os)
            .buildConfiguration();

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetManufacturerForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withManufacturer(manufacturer)
            .buildConfiguration();

        assertThat(target.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetManufactureForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withManufacturer(manufacturer)
            .buildConfiguration();

        assertThat(target.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetModelIdForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withModelID(modelId)
            .buildConfiguration();

        assertThat(target.getDevice().getModelID(), is(equalTo(modelId)));
    }

    @Test
    public void canSetModelIdForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withModelID(modelId)
            .buildConfiguration();

        assertThat(target.getDevice().getModelID(), is(equalTo(modelId)));
    }

    @Test
    public void canSetAppNameForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withApplicationName(appName)
            .buildConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(appName)));
    }
}
