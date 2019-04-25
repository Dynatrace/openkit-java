/**
 * Copyright 2018-2019 Dynatrace LLC
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

package com.dynatrace.openkit;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.BeaconConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class OpenKitBuilderTest {
    private static final String ENDPOINT = "https://localhost:12345";
    private static final String APP_ID = "asdf123";
    private static final String APP_NAME = "myName";
    private static final long DEVICE_ID = 1234L;
    private static final String APP_VERSION = "1.2.3.4";
    private static final String OPERATING_SYSTEM = "custom OS";
    private static final String MANUFACTURER = "custom manufacturer";
    private static final String MODEL_ID = "custom model id";
    private static final DataCollectionLevel DATA_COLLECTION_LEVEL = DataCollectionLevel.PERFORMANCE;
    private static final CrashReportingLevel CRASH_REPORTING_LEVEL = CrashReportingLevel.OPT_IN_CRASHES;

    @Test
    public void defaultsAreSetForAppMon() {

        // when
        Configuration obtained = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID).buildConfiguration();

        // then
        assertThat(obtained.getEndpointURL(), is(equalTo(ENDPOINT)));
        assertThat(obtained.getDeviceID(), is(equalTo("1234")));
        assertThat(obtained.getApplicationName(), is(equalTo(APP_NAME)));
        assertThat(obtained.getApplicationID(), is(equalTo(APP_NAME)));

        // ensure remaining defaults
        verifyDefaultsAreSet(obtained);
    }

    @Test
    public void appMonOpenKitBuilderTakesStringDeviceID() {

        // given
        AbstractOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, "stringDeviceID");

        // when, then
        assertThat(target.buildConfiguration().getDeviceID(), is(equalTo("stringDeviceID")));
    }

    @Test
    public void defaultsAreSetForDynatrace() {

        // when
        Configuration obtained = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID).buildConfiguration();

        // then
        assertThat(obtained.getEndpointURL(), is(equalTo(ENDPOINT)));
        assertThat(obtained.getDeviceID(), is(equalTo("1234")));
        assertThat(obtained.getApplicationName(), is(equalTo("")));
        assertThat(obtained.getApplicationID(), is(equalTo(APP_ID)));

        // ensure remaining defaults
        verifyDefaultsAreSet(obtained);
    }

    @Test
    public void dynatraceOpenKitBuilderTakesStringDeviceID() {

        // given
        AbstractOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, "stringDeviceID");

        // when, then
        assertThat(target.buildConfiguration().getDeviceID(), is(equalTo("stringDeviceID")));
    }

    private void verifyDefaultsAreSet(Configuration configuration) {

        // default values
        assertThat(configuration.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
        assertThat(configuration.getDevice().getManufacturer(), is(equalTo(OpenKitConstants.DEFAULT_MANUFACTURER)));
        assertThat(configuration.getDevice()
                                .getOperatingSystem(), is(equalTo(OpenKitConstants.DEFAULT_OPERATING_SYSTEM)));
        assertThat(configuration.getDevice().getModelID(), is(equalTo(OpenKitConstants.DEFAULT_MODEL_ID)));

        // default trust manager
        assertThat(configuration.getHttpClientConfig().getSSLTrustManager(), instanceOf(SSLStrictTrustManager.class));

        // default values for beacon cache configuration
        assertThat(configuration.getBeaconCacheConfiguration(), is(notNullValue()));
        assertThat(configuration.getBeaconCacheConfiguration().getMaxRecordAge(), is(BeaconCacheConfiguration.DEFAULT_MAX_RECORD_AGE_IN_MILLIS));
        assertThat(configuration.getBeaconCacheConfiguration().getCacheSizeUpperBound(), is(BeaconCacheConfiguration.DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES));
        assertThat(configuration.getBeaconCacheConfiguration().getCacheSizeLowerBound(), is(BeaconCacheConfiguration.DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES));
        assertThat(configuration.getBeaconConfiguration().getDataCollectionLevel(), is(BeaconConfiguration.DEFAULT_DATA_COLLECTION_LEVEL));
        assertThat(configuration.getBeaconConfiguration().getCrashReportingLevel(), is(BeaconConfiguration.DEFAULT_CRASH_REPORTING_LEVEL));
    }

    @Test
    public void applicationNameIsSetCorrectlyForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID).buildConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(APP_NAME)));
        assertThat(target.getApplicationID(), is(equalTo(APP_NAME)));
    }

    @Test
    public void canOverrideTrustManagerForAppMon() {
        SSLTrustManager trustManager = mock(SSLTrustManager.class);

        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID)
            .withTrustManager(trustManager)
            .buildConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void canOverrideTrustManagerForDynatrace() {
        SSLTrustManager trustManager = mock(SSLTrustManager.class);

        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withTrustManager(trustManager)
            .buildConfiguration();

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void canSetApplicationVersionForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID)
            .withApplicationVersion(APP_VERSION)
            .buildConfiguration();

        assertThat(target.getApplicationVersion(), is(equalTo(APP_VERSION)));
    }

    @Test
    public void canSetApplicationVersionForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withApplicationVersion(APP_VERSION)
            .buildConfiguration();

        assertThat(target.getApplicationVersion(), is(equalTo(APP_VERSION)));
    }

    @Test
    public void canSetOperatingSystemForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID)
            .withOperatingSystem(OPERATING_SYSTEM)
            .buildConfiguration();

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(OPERATING_SYSTEM)));
    }

    @Test
    public void canSetOperatingSystemForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withOperatingSystem(OPERATING_SYSTEM)
            .buildConfiguration();

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(OPERATING_SYSTEM)));
    }

    @Test
    public void canSetManufacturerForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID)
            .withManufacturer(MANUFACTURER)
            .buildConfiguration();

        assertThat(target.getDevice().getManufacturer(), is(equalTo(MANUFACTURER)));
    }

    @Test
    public void canSetManufactureForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withManufacturer(MANUFACTURER)
            .buildConfiguration();

        assertThat(target.getDevice().getManufacturer(), is(equalTo(MANUFACTURER)));
    }

    @Test
    public void canSetModelIDForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_NAME, DEVICE_ID)
            .withModelID(MODEL_ID)
            .buildConfiguration();

        assertThat(target.getDevice().getModelID(), is(equalTo(MODEL_ID)));
    }

    @Test
    public void canSetModelIDForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withModelID(MODEL_ID)
            .buildConfiguration();

        assertThat(target.getDevice().getModelID(), is(equalTo(MODEL_ID)));
    }

    @Test
    public void canSetAppNameForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withApplicationName(APP_NAME)
            .buildConfiguration();

        assertThat(target.getApplicationName(), is(equalTo(APP_NAME)));
    }

    @Test
    public void canSetLogger() {
        // given
        Logger logger = mock(Logger.class);

        // when
        Logger target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID).withLogger(logger).getLogger();

        // then
        assertThat(target, is(sameInstance(logger)));
    }

    @Test
    public void defaultLoggerIsUsedByDefault() {
        // when
        Logger target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID).getLogger();

        // then
        assertThat(target, is(instanceOf(DefaultLogger.class)));
        assertThat(target.isDebugEnabled(), is(false));
        assertThat(target.isInfoEnabled(), is(false));
    }

    @Test
    public void verboseIsUsedInDefaultLogger() {
        // when
        Logger target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID).enableVerbose().getLogger();

        // then
        assertThat(target, is(instanceOf(DefaultLogger.class)));
        assertThat(target.isDebugEnabled(), is(true));
        assertThat(target.isInfoEnabled(), is(true));
    }

    @Test
    public void canSetCustomMaxBeaconRecordAgeForDynatrace() {

        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID);
        final long maxRecordAge = 123456L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheMaxRecordAge(maxRecordAge);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(DynatraceOpenKitBuilder.class)));
        assertThat((DynatraceOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void canSetCustomMaxBeaconRecordAgeForAppMon() {

        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID);
        final long maxRecordAge = 123456L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheMaxRecordAge(maxRecordAge);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(AppMonOpenKitBuilder.class)));
        assertThat((AppMonOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void canSetBeaconCacheLowerMemoryBoundaryForDynatrace() {

        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID);
        final long lowerMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheLowerMemoryBoundary(lowerMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(DynatraceOpenKitBuilder.class)));
        assertThat((DynatraceOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheLowerMemoryBoundary(), is(lowerMemoryBoundary));
    }

    @Test
    public void canSetBeaconCacheLowerMemoryBoundaryForAppMon() {

        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID);
        final long lowerMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheLowerMemoryBoundary(lowerMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(AppMonOpenKitBuilder.class)));
        assertThat((AppMonOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheLowerMemoryBoundary(), is(lowerMemoryBoundary));
    }

    @Test
    public void canSetBeaconCacheUpperMemoryBoundaryForDynatrace() {

        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID);
        final long upperMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheUpperMemoryBoundary(upperMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(DynatraceOpenKitBuilder.class)));
        assertThat((DynatraceOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheUpperMemoryBoundary(), is(upperMemoryBoundary));
    }

    @Test
    public void canSetBeaconCacheUpperMemoryBoundaryForAppMon() {

        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID);
        final long upperMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheUpperMemoryBoundary(upperMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(AppMonOpenKitBuilder.class)));
        assertThat((AppMonOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheUpperMemoryBoundary(), is(upperMemoryBoundary));
    }

    @Test
    public void canSetDataCollectionLevelForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withDataCollectionLevel(DATA_COLLECTION_LEVEL)
            .buildConfiguration();

        assertThat(target.getBeaconConfiguration().getDataCollectionLevel(),
            is(equalTo(DATA_COLLECTION_LEVEL)));
    }

    @Test
    public void canSetDataCollectionLevelForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withDataCollectionLevel(DATA_COLLECTION_LEVEL)
            .buildConfiguration();

        assertThat(target.getBeaconConfiguration().getDataCollectionLevel(),
            is(equalTo(DATA_COLLECTION_LEVEL)));
    }

    @Test
    public void canSetCrashReportingLevelForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withCrashReportingLevel(CRASH_REPORTING_LEVEL)
            .buildConfiguration();

        assertThat(target.getBeaconConfiguration().getCrashReportingLevel(),
            is(equalTo(CRASH_REPORTING_LEVEL)));
    }

    @Test
    public void canSetCrashReportingLevelForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(ENDPOINT, APP_ID, DEVICE_ID)
            .withCrashReportingLevel(CRASH_REPORTING_LEVEL)
            .buildConfiguration();

        assertThat(target.getBeaconConfiguration().getCrashReportingLevel(),
            is(equalTo(CRASH_REPORTING_LEVEL)));
    }

}
