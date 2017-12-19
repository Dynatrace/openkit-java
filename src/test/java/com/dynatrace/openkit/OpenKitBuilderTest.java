package com.dynatrace.openkit;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.ssl.SSLBlindTrustManager;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class OpenKitBuilderTest {
    private static final String endpoint = "https://dynatrace.com";
    private static final String appId = "asdf123";
    private static final String appName = "myName";
    private static final long deviceId = 1234L;
    private static final String appVersion = "1.2.3.4";
    private static final String os = "custom OS";
    private static final String manufacturer = "custom manufacturer";
    private static final String modelId = "custom model id";


    @Test
    public void defaultsAreSetForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId).build();
        defaultsAreSet(openKit);
    }

    @Test
    public void defaultsAreSetForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId).build();
        defaultsAreSet(openKit);
    }

    public void defaultsAreSet(OpenKitImpl openKit) {
        Configuration target = openKit.getConfiguration();

        // default values
        assertThat(target.getApplicationVersion(), is(equalTo(AbstractOpenKitBuilder.DEFAULT_APPLICATION_VERSION)));
        assertThat(target.getDevice().getManufacturer(), is(equalTo(AbstractOpenKitBuilder.DEFAULT_MANUFACTURER)));
        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(AbstractOpenKitBuilder.DEFAULT_OPERATING_SYSTEM)));
        assertThat(target.getDevice().getModelID(), is(equalTo(AbstractOpenKitBuilder.DEFAULT_MODEL_ID)));
        assertThat(target.isVerbose(), is(false));

        // default trust manager
        validateTrustManager(openKit, SSLStrictTrustManager.class);
    }

    @Test
    public void applicationNameIsSetCorrectlyForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId).build();

        Configuration target = openKit.getConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(appName)));
        assertThat(target.getApplicationName(), is(equalTo(target.getApplicationID())));
    }

    @Test
    public void canOverrideTrustManagerForAppMon()
    {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withTrustManager(new SSLBlindTrustManager())
            .build();

        validateTrustManager(openKit, SSLBlindTrustManager.class);
    }

    @Test
    public void canOverrideTrustManagerForDynatrace()
    {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withTrustManager(new SSLBlindTrustManager())
            .build();

        validateTrustManager(openKit, SSLBlindTrustManager.class);
    }


    public void validateTrustManager(OpenKitImpl openKit, Class expectedClass) {
        Configuration target = openKit.getConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), instanceOf(expectedClass));
    }

    @Test
    public void canEnableVerboseForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .enableVerbose()
            .build();

        assertThat(openKit.getConfiguration().isVerbose(), is(true));
    }

    @Test
    public void canEnableVerboseForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .enableVerbose()
            .build();

        assertThat(openKit.getConfiguration().isVerbose(), is(true));
    }

    @Test
    public void canSetApplicationVersionForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withApplicationVersion(appVersion)
            .build();

        assertThat(openKit.getConfiguration().getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetApplicationVersionForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withApplicationVersion(appVersion)
            .build();

        assertThat(openKit.getConfiguration().getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetOperatingSystemForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withOperatingSystem(os)
            .build();

        assertThat(openKit.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetOperatingSystemForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withOperatingSystem(os)
            .build();

        assertThat(openKit.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetManufacturerForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withManufacturer(manufacturer)
            .build();

        assertThat(openKit.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetManufactureForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withManufacturer(manufacturer)
            .build();

        assertThat(openKit.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetModelIdForAppMon() {
        OpenKitImpl openKit = (OpenKitImpl) new AppMonOpenKitBuilder(endpoint, appName, deviceId)
            .withModelID(modelId)
            .build();

        assertThat(openKit.getDevice().getModelID(), is(equalTo(modelId)));
    }

    @Test
    public void canSetModelIdForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withModelID(modelId)
            .build();

        assertThat(openKit.getDevice().getModelID(), is(equalTo(modelId)));
    }

    @Test
    public void canSetAppNameForDynatrace() {
        OpenKitImpl openKit = (OpenKitImpl) new DynatraceOpenKitBuilder(endpoint, appId, deviceId)
            .withApplicationName(appName)
            .build();

        assertThat(openKit.getConfiguration().getApplicationName(), is(equalTo(appName)));
    }
}
